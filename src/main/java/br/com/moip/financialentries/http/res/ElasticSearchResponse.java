package br.com.moip.financialentries.http.res;

public record ElasticSearchResponse(Integer took, HitsResponse hits) {
}
