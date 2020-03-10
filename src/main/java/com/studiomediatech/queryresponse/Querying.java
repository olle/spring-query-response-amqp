package com.studiomediatech.queryresponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;


class Querying<T> implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(Querying.class);

    private final Queries<T> queries;
    private final AtomicReference<Results<T>> response;
    private final ObjectMapper objectMapper;

    public Querying(Queries<T> queries) {

        this.queries = queries;
        this.response = new AtomicReference<>(Results.empty());

        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void onMessage(Message message) {

        try {
            LOG.info("|--> Received response: {}", message);

            Envelope<T> envelope = objectMapper.readValue(message.getBody(),
                    TypeFactory.defaultInstance()
                        .constructParametricType(Envelope.class, queries.getType()));

            if (envelope.elements == null) {
                LOG.warn("Received empty response", envelope);

                return;
            }

            response.set(new Results<>(envelope.elements));
        } catch (RuntimeException | IOException e) {
            LOG.error("Failed to consume response", e);
        }
    }


    public Collection<T> publish(RabbitTemplate rabbit, String queue) {

        try {
            var message = MessageBuilder.withBody("{}".getBytes()).setReplyTo(queue).build();

            rabbit.send("queries", queries.getQueryForTerm(), message);
            LOG.info("|<-- Published query: {}", queries.getQueryForTerm());
        } catch (RuntimeException e) {
            // TODO: Apply to provided onError-handler
            e.printStackTrace();
        }

        try {
            Thread.sleep(queries.getWaitingFor().toMillis());
        } catch (InterruptedException e) {
            // TODO: Apply to provided onError-handler
            e.printStackTrace();
        }

        return response.get().accept(queries);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Envelope<R> {

        @JsonProperty
        public int count;
        @JsonProperty
        public int total;
        @JsonProperty
        public Collection<R> elements;

        @Override
        public String toString() {

            return "Envelope [count=" + count + ", total=" + total + ", elements=" + elements + "]";
        }
    }
}
