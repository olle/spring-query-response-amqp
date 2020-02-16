package com.studiomediatech.responses;

import com.studiomediatech.Responses;

import org.springframework.amqp.core.Address;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;


public class Responder<T> implements MessageListener {

    private final RabbitTemplate rabbitTemplate;

    public Responder(RabbitTemplate rabbitTemplate, Responses<T> responses) {

        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void onMessage(Message message) {

        System.out.println("|<------------------ RECEIVED QUERY: " + message);

        Address replyToAddress = message.getMessageProperties().getReplyToAddress();

        Message response = MessageBuilder.withBody("HELO".getBytes()).build();
        rabbitTemplate.send(replyToAddress.getExchangeName(), replyToAddress.getRoutingKey(), response);

        System.out.println("------------------>| PUBLISHED RESPONSE: " + response);
    }
}
