package com.studiomediatech.queryresponse.amqp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class QueryResponseTopicExchangeTest {

    @Test
    void ensureHasGivenNameAndDefinedProperties() {

        QueryResponseTopicExchange exchange = new QueryResponseTopicExchange("some-name");

        assertThat(exchange.getName()).isEqualTo("some-name");
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
    }

}
