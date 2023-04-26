package br.com.moip.financialentries.http.req.metabase;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SessionRequest(@JsonProperty("username") String userName, String password) {
}
