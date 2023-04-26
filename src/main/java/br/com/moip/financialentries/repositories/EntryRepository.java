package br.com.moip.financialentries.repositories;

import br.com.moip.financialentries.domain.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {

    @Query(value = "SELECT id, external_id FROM entry e WHERE e.moip_account = :mpa and settled_at between :beginDate and :endDate", nativeQuery = true)
    List<Entry> findEntries(@Param("mpa") final String mpa,
                            @Param("beginDate") final LocalDateTime beginDate,
                            @Param("endDate") final LocalDateTime endDate
    );

    @Query(value = "SELECT count(id) from entry e WHERE e.moip_account = :mpa", nativeQuery = true)
    Integer getTotalEntries(@Param("mpa") final String mpa);
}
