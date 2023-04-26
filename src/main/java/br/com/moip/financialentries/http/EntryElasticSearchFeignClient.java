package br.com.moip.financialentries.http;


import br.com.moip.financialentries.http.res.ElasticSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(value = "entryElasticSearchFeignClient", url = "http://search-financial-bkg3ra6pfxbdhzwnbh4oavqcuq.us-east-1.es.amazonaws.com:80")
public interface EntryElasticSearchFeignClient {
    @GetMapping("/financial/entries/_search")
    ElasticSearchResponse getEntries(@RequestBody Object query);






}
