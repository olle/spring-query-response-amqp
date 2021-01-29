package com.studiomediatech.queryresponse.amqp;

import org.springframework.amqp.core.FanoutExchange;

/**
 * A specific type of {@link FanoutExchange} that can be declared and identified
 * (by type) in the application context.
 * 
 * This exchange is meant to be used as the control-plane, allowing statistics
 * and other information a side-channel, from the
 * {@link QueryResponseTopicExchange} in the data-plane.
 */
public final class QueryResponseFanoutExchange extends FanoutExchange {

    private static final boolean DURABLE = true;
    private static final boolean AUTO_DELETE = false;

    public QueryResponseFanoutExchange(String name) {

        super(name, DURABLE, AUTO_DELETE);
    }
}
