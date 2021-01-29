package com.studiomediatech.queryresponse;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class QueryResponseConfigurationPropertiesTest {

    @Test
    void ensureDefaultExchangeName() {

        QueryResponseConfigurationProperties props = new QueryResponseConfigurationProperties();

        assertThat(props.getExchange()).isNotNull();
        assertThat(props.getExchange().getName()).isEqualTo("query-response");
    }

    @Test
    void ensureDefaultQueuePrefix() throws Exception {

        QueryResponseConfigurationProperties props = new QueryResponseConfigurationProperties();

        assertThat(props.getQueue()).isNotNull();
        assertThat(props.getQueue().getPrefix()).isEqualTo("query-response-");
    }

    @Test
    void ensureDefaultFanoutExchangeName() throws Exception {

        QueryResponseConfigurationProperties props = new QueryResponseConfigurationProperties();

        assertThat(props.getFanoutExchange()).isNotNull();
        assertThat(props.getFanoutExchange().getName()).isEqualTo("query-response.fanout");
    }

    @Test
    void ensureNoFoobarWithTheSetters() throws Exception {

        QueryResponseConfigurationProperties props = new QueryResponseConfigurationProperties();
        props.getExchange().setName("foo");
        props.getQueue().setPrefix("bar");
        props.getFanoutExchange().setName("baz");

        assertThat(props.getExchange().getName()).isEqualTo("foo");
        assertThat(props.getQueue().getPrefix()).isEqualTo("bar");
        assertThat(props.getFanoutExchange().getName()).isEqualTo("baz");
    }
}
