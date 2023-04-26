package br.com.moip.financialentries.http.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SourceResponse(
        String eventId,
        @JsonProperty("external_id")
        String externalId,
        String description,

        Integer liquidAmount,

        String createdAt,

        String settledAt,

        MoipAccountResponse moipAccount


) {
}
