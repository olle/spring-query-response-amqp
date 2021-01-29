package com.studiomediatech.queryresponse;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = QueryResponseConfigurationProperties.class)
@TestPropertySource("classpath:queryresponse-test.properties")
class QueryResponseConfigurationPropertiesIT {

    @Autowired
    QueryResponseConfigurationProperties props;

    @Test
    void ensureQueryResponseConfigurationPropertiesBound() {

        assertThat(props.getExchange().getName()).isEqualTo("qr");
        assertThat(props.getQueue().getPrefix()).isEqualTo("qr-");
        assertThat(props.getFanoutExchange().getName()).isEqualTo("qr.fanout");
    }

}
