package br.com.moip.financialentries;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FinancialEntriesApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialEntriesApplication.class, args);
    }

}
