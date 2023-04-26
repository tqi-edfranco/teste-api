package br.com.moip.financialentries.http.req.metabase;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

public record CommandRequest(@JsonProperty("database") Integer dataBase, @JsonProperty("native") NativeRequest nativeRequest, String type) {

    @Builder(toBuilder = true)
    public CommandRequest {}
}
