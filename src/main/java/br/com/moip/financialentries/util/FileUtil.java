package br.com.moip.financialentries.util;

import br.com.moip.financialentries.domain.EntryRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Component
public class FileUtil {

    public void write(final Set<EntryRecord> entries, final String name) {
        ClassLoader classLoader = getClass().getClassLoader();
        var file = new File(classLoader.getResource(".").getFile() + "/" + "mysql" + "-entries-" + LocalDate.now());
        var content = this.converter(entries);
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String converter(final Set<EntryRecord> entries) {
        final var join = new StringBuilder();
        entries.forEach(item -> join.append(item.externalId()).append("\r\n"));
        return join.toString();
    }
    public void writeFile(String mpa, Set<EntryRecord> entries, String dataName) {
        if (!entries.isEmpty()) {
            log.info("Gerando arquivo...");
            File file = new File("Entries_"+ dataName +"_" + mpa + ".txt");

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(this.converter(entries));
                log.info("Arquivo gerado com sucesso!");
            } catch (IOException e) {
                log.error("Ocorreu um erro ao gerar o arquivo: " + e.getMessage());
            }
        }
    }

}
