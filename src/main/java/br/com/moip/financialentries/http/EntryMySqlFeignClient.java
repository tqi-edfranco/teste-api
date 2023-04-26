package br.com.moip.financialentries.http;


import br.com.moip.financialentries.http.req.metabase.CommandRequest;
import br.com.moip.financialentries.http.req.metabase.SessionRequest;
import br.com.moip.financialentries.http.req.metabase.TokenRequest;
import br.com.moip.financialentries.http.res.EntryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(value = "entryMySqlFeignClient", url = "https://metabase.moip.com.br/api")
public interface EntryMySqlFeignClient {

    @PostMapping("/dataset")
    EntryResponse getEntries(CommandRequest commandRequest);

    @PostMapping("/session")
    TokenRequest getToken(SessionRequest sessionRequest);

}
