package br.com.moip.financialentries.domain;

import lombok.Builder;

import java.util.Objects;

public record EntryRecord(
        Long id,
        String externalId
) {
    @Builder
    public EntryRecord {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntryRecord entryRecord = (EntryRecord) o;
        return externalId.equals(entryRecord.externalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(externalId);
    }
}
