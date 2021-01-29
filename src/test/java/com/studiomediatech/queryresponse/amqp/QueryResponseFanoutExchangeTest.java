package com.studiomediatech.queryresponse.amqp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class QueryResponseFanoutExchangeTest {

    @Test
    void ensureHasGivenNameAndDefinedProperties() {

        QueryResponseFanoutExchange exchange = new QueryResponseFanoutExchange("some-exchange-name");

        assertThat(exchange.getName()).isEqualTo("some-exchange-name");
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
    }

}
