package com.studiomediatech.queryresponseui.events;

import java.util.Optional;

/**
 * An event object, emitted after the fact that a query was issued (in the UI).
 */
public final class QueryIssuedEvent {

    protected static final int DEFAULT_TIMEOUT_MS = 789;

    private final String query;
    private final Long timeout;
    private final Optional<Integer> limit;
    private final String publisherId;

    private QueryIssuedEvent(String query, Long timeout, Optional<Integer> limit, String publisherId) {

        this.query = query;
        this.timeout = timeout;
        this.limit = limit;
        this.publisherId = publisherId;
    }

    public String getQuery() {

        return query;
    }

    public Long getTimeout() {

        return timeout;
    }

    public Optional<Integer> getLimit() {

        return limit;
    }

    public String getPublisherId() {

        return publisherId;
    }

    public static QueryIssuedEvent valueOf(String queryString, String publisher) {

        String q = queryString.trim();

        String query = parseQuery(q);
        long timeout = parseTimeout(q);
        Optional<Integer> limit = parseLimit(q);

        return new QueryIssuedEvent(query, timeout, limit, publisher);
    }

    private static String parseQuery(String queryString) {

        return queryString.split(" ")[0];
    }

    private static long parseTimeout(String queryString) {

        try {
            return queryString.contains(" ") ? Long.parseLong(queryString.split(" ")[1]) : DEFAULT_TIMEOUT_MS;
        } catch (RuntimeException e) {
            return DEFAULT_TIMEOUT_MS;
        }
    }

    private static Optional<Integer> parseLimit(String queryString) {

        if (!queryString.contains(" ")) {
            return Optional.empty();
        }

        try {
            int limit = Integer.parseInt(queryString.split(" ")[2]);

            return limit > 0 ? Optional.of(limit) : Optional.empty();
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
