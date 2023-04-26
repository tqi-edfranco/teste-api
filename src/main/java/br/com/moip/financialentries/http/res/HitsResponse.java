package br.com.moip.financialentries.http.res;

import java.util.List;

public record HitsResponse(Integer total, List<HitsItemResponse> hits) {
}
