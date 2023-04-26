package br.com.moip.financialentries.service;

import br.com.moip.financialentries.constants.Elasticsearch;
import br.com.moip.financialentries.domain.Entry;
import br.com.moip.financialentries.domain.EntryRecord;
import br.com.moip.financialentries.http.EntryElasticSearchFeignClient;
import br.com.moip.financialentries.http.res.ElasticSearchResponse;
import br.com.moip.financialentries.http.res.HitsItemResponse;
import br.com.moip.financialentries.repositories.EntryRepository;
import br.com.moip.financialentries.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EntryService implements ApplicationRunner {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String MPA = "MPA-C68B580C97D0";

    private final EntryElasticSearchFeignClient entryElasticSearchFeignClient;

    private final EntryRepository entryRepository;

    private final FileUtil fileUtil;

    @Value("${entry.begin-date}")
    private String beginDate;

    @Value("${entry.end-date}")
    private String endDate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        long startTime = System.currentTimeMillis();
        log.info("=== START FIND DIFF BETWEEN MSQL AND ELASTIC SEARCH ===");

        var totalEntriesMySQL = this.entryRepository.getTotalEntries(MPA);
        var totalEntriesElastic = this.entryElasticSearchFeignClient.getEntries(Elasticsearch.getTotalEntries.formatted(MPA)).hits().total();

        if (totalEntriesMySQL.equals(totalEntriesElastic)) {
            log.info("Nenhuma divergência encontrada para o {} no range de datas {} - {}", MPA, beginDate, endDate);
            log.info("Total de entries no MySQL {} - Total de entries no Elastic {}", totalEntriesMySQL, totalEntriesElastic);
            return;
        }

        CompletableFuture<Set<EntryRecord>> entryFuture = CompletableFuture
                .supplyAsync(() -> this.findEntriesMsql(MPA,
                        LocalDateTime.parse(beginDate, FORMAT_DATE),
                        LocalDateTime.parse(endDate, FORMAT_DATE)));

        CompletableFuture<Set<EntryRecord>> entryElastic = CompletableFuture
                .supplyAsync(() -> this.findEntriesElasticSearch(MPA,
                        LocalDateTime.parse(beginDate, FORMAT_DATE),
                        LocalDateTime.parse(endDate, FORMAT_DATE)));


        CompletableFuture.allOf(entryFuture, entryElastic).get();

        var entriesMsql = entryFuture.get();
        var entriesElastic = entryElastic.get();

        var diffMysql = getDiffEntries(entriesMsql, entriesElastic);
        if (entriesElastic.size() > entriesMsql.size()) {
            var diffElastic = getDiffEntries(entriesElastic, entriesMsql);
            log.warn("Encontrado {} {} divergentes", diffElastic.size(), diffElastic.size() > 1 ? "entries" : "entry");
            diffElastic.stream().map(EntryRecord::externalId).forEach(log::warn);
            this.fileUtil.writeFile(MPA, diffElastic, "Elastic");
        }

        if (!diffMysql.isEmpty()) {
            diffMysql.stream().map(EntryRecord::externalId).forEach(log::warn);
            log.warn("Encontrado {} {} divergentes", diffMysql.size(), diffMysql.size() > 1 ? "entries" : "entry");
            this.fileUtil.writeFile(MPA, diffMysql, "MySQL");
        } else {
            log.info("Nenhuma divergência encontrada para o {} no range de datas {} - {}", MPA, beginDate, endDate);
        }

        log.info("=== FINISH FIND DIFF BETWEEN MSQL AND ELASTIC SEARCH ===");
        long endTime = System.currentTimeMillis();
        long tempoDeExecucao = endTime - startTime;
        log.info("Tempo de execução do método: " + (tempoDeExecucao / 1000) + " segundos");
        entryElastic.complete(Set.of());
        entryFuture.complete(Set.of());
    }

    private Set<EntryRecord> getDiffEntries(final Set<EntryRecord> firstEntries, final Set<EntryRecord> lastEntries) {
        var newFirstEntries = new HashSet<>(firstEntries);
        var newLastEntries = new  HashSet<>(lastEntries);
        newFirstEntries.removeAll(newLastEntries);
        return newFirstEntries;
    }

    private Set<EntryRecord> findEntriesElasticSearch(final String mpa, final LocalDateTime beginDate, final LocalDateTime endDate) {
        log.info("=== START FIND ENTRY INTO ElasticSearch ===");
        final String firstPattern = "yyyy-MM-dd 00:00:00";
        final String LastPattern = "yyyy-MM-dd 23:59:59";
        List<HitsItemResponse> hitsItemResponse = new ArrayList<>();
        LocalDateTime newBeginDate = beginDate;
        LocalDateTime newEndDate = beginDate.plusDays(1).minusSeconds(1);
        boolean hasDays = true;

        while (hasDays) {
            var size = this.entryElasticSearchFeignClient.getEntries(Elasticsearch.query.formatted(0, mpa, newBeginDate.
                            format(DateTimeFormatter.ofPattern(firstPattern)),
                    newEndDate.format(DateTimeFormatter.ofPattern(LastPattern)),
                    newEndDate.format(DateTimeFormatter.ofPattern(LastPattern)),
                    newBeginDate.format(DateTimeFormatter.ofPattern(firstPattern)),
                    newEndDate.format(DateTimeFormatter.ofPattern(LastPattern))
            ));

            var result = this.entryElasticSearchFeignClient.getEntries(Elasticsearch.query.formatted(size.hits().total(), mpa, newBeginDate.
                            format(DateTimeFormatter.ofPattern(firstPattern)),
                    newEndDate.format(DateTimeFormatter.ofPattern(LastPattern)),
                    newEndDate.format(DateTimeFormatter.ofPattern(LastPattern)),
                    newBeginDate.format(DateTimeFormatter.ofPattern(firstPattern)),
                    newEndDate.format(DateTimeFormatter.ofPattern(LastPattern))
            ));
            hasDays = !newEndDate.equals(endDate);

            newBeginDate = newBeginDate.plusDays(Duration.between(newBeginDate, newEndDate.plusSeconds(1)).toDays());
            newEndDate = newEndDate.plusDays(1).isAfter(endDate) ? endDate : newEndDate.plusDays(1);

            hitsItemResponse.addAll(result.hits().hits());

        }
        log.info("=== FINISH FIND ENTRY INTO ElasticSearch ===");
        return this.fillEntriesElasticSearch(hitsItemResponse);
    }

    private Set<EntryRecord> findEntriesMsql(final String mpa, final LocalDateTime beginDate, final LocalDateTime endDate) {
        log.info("=== START FIND ENTRY INTO MSQL ===");

        var entries = new ArrayList<EntryRecord>();
        var entriesFilter = filter(mpa, beginDate, endDate, entries);
        log.info("=== FINISH FIND ENTRY INTO MSQL ===");

        return new HashSet<>(entriesFilter);
    }

    private List<EntryRecord> filter(final String mpa, final LocalDateTime beginDate, final LocalDateTime endDate, List<EntryRecord> entries) {
        LocalDateTime newBeginDate = beginDate;
        LocalDateTime newEndDate = beginDate.plusDays(1).minusSeconds(1);
        boolean hasDays = true;

        while (hasDays) {
            log.info("Buscando as datas: {} - {}", newBeginDate, newEndDate);

            var result = this.entryRepository.findEntries(mpa, newBeginDate, newEndDate);
            log.info("Quantidade de entries: {}", result.size());
            hasDays = !newEndDate.equals(endDate);

            if (result.size() <= 6000) {
                newBeginDate = newBeginDate.plusDays(Duration.between(newBeginDate, newEndDate.plusSeconds(1)).toDays());
                newEndDate = newEndDate.plusDays(2).isAfter(endDate) ? endDate : newEndDate.plusDays(2);
            }  else {
                newBeginDate = newBeginDate.plusDays(Duration.between(newBeginDate, newEndDate.plusSeconds(1)).toDays());
                newEndDate = newEndDate.plusDays(1).isAfter(endDate) ? endDate : newEndDate.plusDays(1);
            }

            entries.addAll(fillEntriesMysql(result));
        }
        return entries;
    }

    private List<EntryRecord> fillEntriesMysql(final List<Entry> entriesResponse) {
        List<EntryRecord> entries = new ArrayList<>();
        for (Entry row : entriesResponse) {
            entries.add(split(row));
        }
        return entries;
    }

    private Set<EntryRecord> fillEntriesElasticSearch(final List<HitsItemResponse> rows) {
        return rows.stream()
                .map(item -> EntryRecord.builder()
                        .externalId(item.source().externalId())
                        .build())
                .collect(Collectors.toSet());
    }

    private EntryRecord split(Entry line) {
        return EntryRecord.builder()
                .id(line.getId())
                .externalId(line.getExternalId())
                .build();
    }

}
