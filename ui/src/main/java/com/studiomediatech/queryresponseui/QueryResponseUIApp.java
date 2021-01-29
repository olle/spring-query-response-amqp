package com.studiomediatech.queryresponseui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.studiomediatech.queryresponse.EnableQueryResponse;

@SpringBootApplication
@EnableQueryResponse
public class QueryResponseUIApp {

    public static void main(String[] args) {

        SpringApplication.run(QueryResponseUIApp.class);
    }

}
