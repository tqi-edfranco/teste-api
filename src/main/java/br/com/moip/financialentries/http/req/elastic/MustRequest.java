package br.com.moip.financialentries.http.req.elastic;

import java.util.List;

public record MustRequest(List<MatchRequest> match) {
}
