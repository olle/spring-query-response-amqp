package com.studiomediatech.queryresponse;

import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Configuration properties for Query/Response.
 */
@ConfigurationProperties(prefix = "queryresponse")
public class QueryResponseConfigurationProperties {

    public static final String QUERY_RESPONSE_PREFIX = "query-response";
    public static final String QUERY_RESPONSE_STATS_ROUTING_KEY = QUERY_RESPONSE_PREFIX + "/stats";
    public static final String HEADER_X_QR_PUBLISHED = "x-qr-published";

    private ExchangeProperties exchange = new ExchangeProperties();

    public ExchangeProperties getExchange() {

        return exchange;
    }


    public void setExchange(ExchangeProperties exchange) {

        this.exchange = exchange;
    }

    public static class ExchangeProperties {

        /**
         * Name of the shared topic exchange for queries.
         */
        private String name = QUERY_RESPONSE_PREFIX;

        public String getName() {

            return name;
        }


        public void setName(String name) {

            this.name = name;
        }
    }
}
