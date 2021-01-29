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

    public QueryResponseFanoutExchange(String name) {

        super(name, false, true);
    }
}
