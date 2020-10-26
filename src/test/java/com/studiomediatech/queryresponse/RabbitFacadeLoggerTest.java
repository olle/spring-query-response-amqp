package com.studiomediatech.queryresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.slf4j.Logger;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class RabbitFacadeLoggerTest {

    @Mock
    Logger log;

    @Captor
    ArgumentCaptor<String> stringCaptor;

    @Test
    void ensureDebugLoggingFormattedWithFullMessage() {

        when(log.isDebugEnabled()).thenReturn(true);

        LogMockingRabbitFacade sut = new LogMockingRabbitFacade(log);

        Message message = MessageBuilder.withBody("{\"foo\": 1337}".getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        sut.logPublished("type", "routingKey", message);

        verify(log).debug("|<-- Published {}: {} - {}", "type", "routingKey", message);
    }


    @Test
    void ensureInfoLoggingFormattedWithCustomStringWithoutBodyJson() throws Exception {

        when(log.isDebugEnabled()).thenReturn(false);

        LogMockingRabbitFacade sut = new LogMockingRabbitFacade(log);

        Message message = MessageBuilder.withBody("{\"foo\": 1337}".getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        sut.logPublished("type", "routingKey", message);

        verify(log).info(eq("|<-- Published {}: {} - {}"), eq("type"), eq("routingKey"), stringCaptor.capture());

        assertThat(stringCaptor.getValue()).doesNotContain("{\"foo\": 1337}");
    }

    static class LogMockingRabbitFacade extends RabbitFacade {

        private Logger log;

        public LogMockingRabbitFacade(Logger log) {

            super(null, null, null, null, null);
            this.log = log;
        }

        @Override
        public Logger log() {

            return this.log;
        }
    }
}
