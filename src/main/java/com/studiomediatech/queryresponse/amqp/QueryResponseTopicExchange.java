package com.studiomediatech.queryresponse.amqp;

import org.springframework.amqp.core.TopicExchange;

/**
 * A specific type of {@link TopicExchange} that we can declare and use,
 * uniquely, in the application context.
 * 
 * This exchange encapsulates the data-plane for Query/Response.
 */
public class QueryResponseTopicExchange extends TopicExchange {

    public QueryResponseTopicExchange(String name) {

        super(name, false, true);
    }
}
