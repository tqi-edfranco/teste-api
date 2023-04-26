package br.com.moip.financialentries.http.req.elastic;

import lombok.Builder;

public record SearchRequest(
        Integer from,
        Integer size,
        QueryRequest query) {

    @Builder(toBuilder = true)
    public SearchRequest {}
}
