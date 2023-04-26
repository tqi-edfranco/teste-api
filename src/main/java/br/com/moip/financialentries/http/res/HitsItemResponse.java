package br.com.moip.financialentries.http.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HitsItemResponse(@JsonProperty("_source") SourceResponse source) {
}
