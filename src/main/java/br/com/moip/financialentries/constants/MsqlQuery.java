package br.com.moip.financialentries.constants;

public interface MsqlQuery {
    String MSQL_GET_ENTRIES = "SELECT * FROM entry e WHERE e.moip_account = '%s' and settled_at between '%s' and '%s' and id >= %d";
    String MSQL_GET_ENTRIES_NO_ID = "SELECT * FROM entry e WHERE e.moip_account = '%s' and settled_at between '%s' and '%s'";
    String MSQL_LIMITS = "SELECT  min(id), max(id) FROM entry e WHERE e.moip_account = '%s' and settled_at between '%s' and '%s' order by id";

}
