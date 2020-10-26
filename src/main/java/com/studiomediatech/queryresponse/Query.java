package com.studiomediatech.queryresponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.type.TypeFactory;

import com.studiomediatech.queryresponse.util.Logging;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;

import java.io.IOException;

import java.time.Duration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.LongPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Represents an active published query, and the registered message listener for any responses, handling aggregation,
 * fallback, failure delegation and throwing, all depending on the configuration of this query.
 *
 * @param  <T>  expected type of the coerced result elements.
 */
class Query<T> implements MessageListener, Logging {

    private static final String HEADER_X_QR_PUBLISHED = QueryResponseConfigurationProperties.HEADER_X_QR_PUBLISHED;
    private static final long ONE_MILLIS = 1L;

    private static final ObjectReader reader = new ObjectMapper().reader();

    private final String queueName;

    /*
     * For the taking-at-most/least feature, the concurrent append from a
     * consumer, would cause structural change to a list type collection, so we
     * need to use a more more modern queue collection type.
     */
    protected ConcurrentLinkedQueue<T> elements;

    protected String queryTerm;
    protected Class<T> responseType;
    protected Duration waitingFor;
    protected Supplier<Collection<T>> orDefaults;
    protected Consumer<Throwable> onError;
    protected int atLeast;
    protected int atMost;
    protected Supplier<RuntimeException> orThrows;

    /*
     * A protected boolean function, tested with an integer, and always
     * returning false, by default. This enables testability, as it's protected
     * and may compute other results.
     */
    protected LongPredicate fail = l -> false;

    private Statistics stats;

    // Declared protected, for access in unit tests.
    protected Query() {

        this.queueName = UUID.randomUUID().toString();
        this.elements = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void onMessage(Message message) {

        MessageProperties properties = message.getMessageProperties();
        log().info("|--> Received response message: {}", properties);
        measureLatency(properties.getHeader(HEADER_X_QR_PUBLISHED), System.currentTimeMillis());
        handleResponseEnvelope(parseMessage(message));
    }


    private void measureLatency(Long published, Long now) {

        if (stats != null) {
            this.stats.measureLatency(published, now);
        }
    }


    private ConsumedResponseEnvelope<T> parseMessage(Message message) {

        try {
            JavaType type = TypeFactory.defaultInstance()
                    .constructParametricType(ConsumedResponseEnvelope.class, responseType);
            ConsumedResponseEnvelope<T> response = reader.forType(type).readValue(message.getBody());
            log().debug("Received response: {}", response);

            return response;
        } catch (IOException ex) {
            if (onError != null) {
                String errorMessage = "Failed to parse response to elements of type " + responseType.getSimpleName();
                onError.accept(new IllegalArgumentException(errorMessage, ex));
            }

            log().error("Failed to parse received response.", ex);
        }

        return ConsumedResponseEnvelope.empty();
    }


    void handleResponseEnvelope(ConsumedResponseEnvelope<T> envelope) {

        if (envelope.elements.isEmpty()) {
            log().warn("Received empty response: {}", envelope);

            return;
        }

        elements.addAll(envelope.elements);
    }


    static <T> Query<T> from(ChainingQueryBuilder<T> queryBuilder) {

        Query<T> query = new Query<>();

        query.queryTerm = queryBuilder.getQueryForTerm();
        query.responseType = queryBuilder.getType();
        query.waitingFor = queryBuilder.getWaitingFor();
        query.orDefaults = queryBuilder.getOrDefaults();
        query.onError = queryBuilder.getOnError();
        query.atLeast = queryBuilder.getTakingAtLeast();
        query.atMost = queryBuilder.getTakingAtMost();
        query.orThrows = queryBuilder.getOrThrows();

        return query;
    }


    public String getQueueName() {

        return this.queueName;
    }


    /**
     * Accepts the RabbitMQ facade, in order to start the life-cycle, and publish this query to the broker. This method
     * always blocks on the calling thread, and the query configuration requires users (programmers) to <strong>
     * always</strong> declare a timeout.
     *
     * @param  facade  to the broker methods, used for publishing, never {@code null}
     * @param  stats  service, allows reporting of success or failure to queries and response.
     *
     * @return  the consumed response elements collection. May be empty, if the query was configured that way, but this
     *          method call never returns {@code null}.
     *
     * @throws  RuntimeException  if the query was configured that way. Please note that the internals of query
     *                            publishing, and response consumption <strong>never throws</strong>. Exceptions or
     *                            failures will be caught and logged.
     */
    public Collection<T> accept(RabbitFacade facade, Statistics stats) throws RuntimeException {

        this.stats = stats;

        publishQuery(facade);

        /*
         * In this iteration of the Query/Response library, we block on the
         * calling thread. This is the most simple thing I could think of.
         */
        long wait = this.waitingFor.toMillis();

        /*
         * We yield on the thread only to check if the consuming side, has
         * filled up our collection. If we have to bail for other reasons, that
         * could be added to this loop.
         */
        while (wait-- > 0) {
            /*
             * Check the current aggregate. Shady thread safety on the elements
             * collection, but we know it's append only and we pick out the
             * sub-list even if overrun in size.
             */
            if (atMost > 0 && elements.size() >= atMost) {
                stats.incrementConsumedResponsesCounter();

                return this.elements.stream().limit(atMost).collect(Collectors.toList());
            }

            try {
                if (this.fail.test(wait)) {
                    throw new InterruptedException();
                }

                Thread.sleep(ONE_MILLIS);
            } catch (InterruptedException e) {
                // Soft handler for the exception, may be informed.
                if (this.onError != null) {
                    onError.accept(e);
                }

                // Reset interrupted state, before moving on.
                log().error("Sleep interrupted with still {} ms to go", wait, e);
                Thread.currentThread().interrupt();

                /*
                 * If we're configured for it, we may throw an unchecked runtime
                 * exception.
                 */
                if (this.orThrows != null) {
                    stats.incrementFallbacksCounter();

                    throw this.orThrows.get();
                }

                // Otherwise we break and accept this as a failure.
                break;
            }
        }

        /*
         * Enforced only if set to a positive integer, and not 0.
         */
        boolean notEnoughResponses = atLeast > 0 && elements.size() < atLeast;

        /*
         * Queries always either returns with the collected response elements,
         * defaults or throws by configuration of the user.
         */
        if (elements.isEmpty() || notEnoughResponses) {
            stats.incrementFallbacksCounter();

            if (this.orDefaults != null) {
                return this.orDefaults.get();
            } else if (this.orThrows != null) {
                throw this.orThrows.get();
            }
        }

        stats.incrementConsumedResponsesCounter();

        return elements;
    }


    private void publishQuery(RabbitFacade facade) {

        try {
            facade.publishQuery(this.queryTerm,
                MessageBuilder.withBody("{}".getBytes()).setReplyTo(this.queueName).build());
            incrementPublishedQueriesCounterStats();
        } catch (RuntimeException ex) {
            if (this.onError != null) {
                this.onError.accept(ex);
            }

            log().error("Failed to publish query message", ex);
        }
    }


    private void incrementPublishedQueriesCounterStats() {

        if (stats != null) {
            this.stats.incrementPublishedQueriesCounter();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ConsumedResponseEnvelope<R> {

        @JsonProperty
        public Collection<R> elements = new ArrayList<>();

        public static <R> ConsumedResponseEnvelope<R> empty() {

            return new ConsumedResponseEnvelope<>();
        }


        @Override
        public String toString() {

            return "ConsumedResponseEnvelope [elements.size=" + elements.size() + "]";
        }
    }
}
