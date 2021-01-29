package com.studiomediatech.queryresponseui.queries;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.studiomediatech.queryresponse.QueryBuilder;
import com.studiomediatech.queryresponse.util.Logging;
import com.studiomediatech.queryresponseui.SimpleWebSocketHandler;
import com.studiomediatech.queryresponseui.events.QueryIssuedEvent;

/**
 * An adapter component, that handles issued user query-events, and publishes
 * them.
 */
@Component
public class UserQueries implements Logging {

    private final SimpleWebSocketHandler handler;
    private final QueryBuilder queryBuilder;

    public UserQueries(SimpleWebSocketHandler handler, QueryBuilder queryBuilder) {

        this.handler = handler;
        this.queryBuilder = queryBuilder;
    }

    @EventListener
    void on(QueryIssuedEvent event) {

        log().info("HANDLING {}", event);

        String query = event.getQuery();
        long timeout = event.getTimeout();

        Optional<Integer> maybe = event.getLimit();

        List<Object> orEmptyResponse = Arrays.asList("No responses");

        if (maybe.isPresent()) {
            int limit = maybe.get();

            handler.handleResponse(queryBuilder.queryFor(query, Object.class).waitingFor(timeout).takingAtMost(limit)
                    .orDefaults(orEmptyResponse), event.getPublisherId());
        } else {
            handler.handleResponse(
                    queryBuilder.queryFor(query, Object.class).waitingFor(timeout).orDefaults(orEmptyResponse),
                    event.getPublisherId());
        }
    }

}
