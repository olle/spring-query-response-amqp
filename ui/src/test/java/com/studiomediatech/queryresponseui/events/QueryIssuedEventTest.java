package com.studiomediatech.queryresponseui.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class QueryIssuedEventTest {

    private static final long DEFAULT_TIMEOUT_MS = QueryIssuedEvent.DEFAULT_TIMEOUT_MS;

    @Test
    void ensureRetainsPublisher() throws Exception {

        assertThat(QueryIssuedEvent.valueOf("query", "publisher").getPublisherId()).isEqualTo("publisher");
    }

    @Test
    void ensureGetsQueryTermFromQueryWithoutWhitespace() {

        assertThat(QueryIssuedEvent.valueOf("some-query", "").getQuery()).isEqualTo("some-query");
    }

    @Test
    void ensureGetsQueryTermFromQueryWithTrailingWhitespace() {

        assertThat(QueryIssuedEvent.valueOf("some-query  ", "").getQuery()).isEqualTo("some-query");
    }

    @Test
    void ensureGetsQueryTermFromQueryWithLeadingWhitespace() {

        assertThat(QueryIssuedEvent.valueOf(" some-query", "").getQuery()).isEqualTo("some-query");
    }

    @Test
    void ensureGetsQueryTermFromQueryWithArgsSeparatedByWhitespace() {

        assertThat(QueryIssuedEvent.valueOf("some-query 343", "").getQuery()).isEqualTo("some-query");
    }

    @Test
    void ensureGetsTimeoutFromSecondArgAfterWhitespace() throws Exception {

        assertThat(QueryIssuedEvent.valueOf("some-query 343", "").getTimeout()).isEqualTo(343L);
    }

    @Test
    void ensureGetsTimeoutFromSecondArgAfterWhitespaceWithTrailingWhitespace() throws Exception {

        assertThat(QueryIssuedEvent.valueOf("some-query 343  ", "").getTimeout()).isEqualTo(343L);
    }

    @Test
    void ensureGetsDefaultTimeoutIfNoSecondArgAfterWhitespace() throws Exception {

        assertThat(QueryIssuedEvent.valueOf("some-query  ", "").getTimeout()).isEqualTo(DEFAULT_TIMEOUT_MS);
    }

    @Test
    void ensureGetsDefaultTimeoutIfNoSecondArg() throws Exception {

        assertThat(QueryIssuedEvent.valueOf("some-query", "").getTimeout()).isEqualTo(DEFAULT_TIMEOUT_MS);
    }

    @Test
    void ensureGetsDefaultTimeoutIfSecondArgNotNumberParseable() throws Exception {

        assertThat(QueryIssuedEvent.valueOf("some-query foobar", "").getTimeout()).isEqualTo(DEFAULT_TIMEOUT_MS);
    }

    @Test
    void ensureHasNoLimitIfNoThirdArgument() throws Exception {

        assertThat(QueryIssuedEvent.valueOf("some-query 343  ", "").getLimit()).isEmpty();
    }

    @Test
    void ensureGetsNonEmptyLimitIfValidThirdArgument() throws Exception {

        assertThat(QueryIssuedEvent.valueOf("some-query 343 12", "").getLimit()).isNotEmpty();
    }

    @Test
    void ensureGetsGivenLimitIfValidThirdArgument() throws Exception {

        assertThat(QueryIssuedEvent.valueOf("some-query 343 12", "").getLimit()).hasValue(12);
    }

    @Test
    void ensureGetsEmptyLimitIfNegativeIsGiven() throws Exception {

        assertThat(QueryIssuedEvent.valueOf("some-query 343 -3334", "").getLimit()).isEmpty();
    }
}
