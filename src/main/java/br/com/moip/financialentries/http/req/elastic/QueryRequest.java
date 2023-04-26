package br.com.moip.financialentries.http.req.elastic;

import br.com.moip.financialentries.http.req.elastic.BoolRequest;

public record QueryRequest(BoolRequest boolRequest) {
}
