package br.com.moip.financialentries.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Entry {

    @Id
    private Long id;
    private String externalId;

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

}
