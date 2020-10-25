package com.studiomediatech.queryresponse;

import com.studiomediatech.queryresponse.Statistics.Stat;
import com.studiomediatech.queryresponse.util.Logging;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.NamingStrategy;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;

import org.springframework.context.support.GenericApplicationContext;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Provides an abstraction between the use of RabbitMQ and the capabilities in Spring Boot AMQP, and the structured
 * registry code in this library.
 */
class RabbitFacade implements Logging {

    public static final String HEADER_X_QR_PUBLISHED = "x-qr-published";

    private final RabbitAdmin admin;
    private final ConnectionFactory connectionFactory;
    private final RabbitTemplate template;
    private final GenericApplicationContext ctx;

    private TopicExchange queriesExchange;

    protected final Map<String, DirectMessageListenerContainer> containers = new ConcurrentHashMap<>();

    public RabbitFacade(RabbitAdmin admin, RabbitTemplate template, ConnectionFactory connectionFactory,
        TopicExchange queriesExchange, GenericApplicationContext ctx) {

        this.admin = admin;
        this.template = template;
        this.connectionFactory = connectionFactory;
        this.queriesExchange = queriesExchange;
        this.ctx = ctx;
    }

    public void declareQueue(Response<?> response) {

        declareAndRegisterQueue(response::getQueueName);
    }


    public void declareQueue(Query<?> query) {

        declareAndRegisterQueue(query::getQueueName);
    }


    private void declareAndRegisterQueue(NamingStrategy name) {

        AnonymousQueue queue = log(new AnonymousQueue(name));
        admin.declareQueue(queue);
        ctx.registerBean(queue.getActualName(), AnonymousQueue.class, () -> queue);
    }


    public void declareBinding(Response<?> response) {

        String queueName = response.getQueueName();
        String routingKey = response.getRoutingKey();

        declareAndRegisterBinding(queueName, routingKey);
    }


    private void declareAndRegisterBinding(String queueName, String routingKey) {

        Binding binding = log(new Binding(queueName, DestinationType.QUEUE, queriesExchange.getName(), routingKey,
                    null));
        admin.declareBinding(binding);
        ctx.registerBean(queueName + "-binding", Binding.class, () -> binding);
    }


    private DirectMessageListenerContainer createNewListenerContainer() {

        DirectMessageListenerContainer container = new DirectMessageListenerContainer(connectionFactory);

        container.setAcknowledgeMode(AcknowledgeMode.NONE);
        container.setConsumersPerQueue(1);
        container.setPrefetchCount(12);
        container.setMessagesPerAck(12);

        return container;
    }


    public void addListener(Response<?> response) {

        createMessageListenerContainer(response, response.getQueueName());
    }


    public void addListener(Query<?> query) {

        createMessageListenerContainer(query, query.getQueueName());
    }


    public void removeListener(Query<?> query) {

        doRemoveListener(query.getQueueName());
    }


    public void removeListener(Response<?> response) {

        doRemoveListener(response.getQueueName());
    }


    private void doRemoveListener(String queueName) {

        DirectMessageListenerContainer container = containers.remove(queueName);

        if (container != null) {
            container.removeQueueNames(queueName);
            container.stop();
        }
    }


    private DirectMessageListenerContainer createMessageListenerContainer(MessageListener listener, String queueName) {

        return containers.computeIfAbsent(queueName,
                key -> {
                    DirectMessageListenerContainer container = createNewListenerContainer();

                    container.addQueueNames(key);
                    container.setMessageListener(listener);
                    container.start();

                    return container;
                });
    }


    public void removeQueue(Query<?> query) {

        doRemoveQueue(query.getQueueName());
    }


    public void removeQueue(Response<?> response) {

        doRemoveQueue(response.getQueueName());
    }


    private void doRemoveQueue(String queueName) {

        admin.deleteQueue(queueName);
    }


    /**
     * Publishes a query to the Query/Response exchange, with the given routing key and message.
     *
     * @param  routingKey  query, or query-term to publish
     * @param  message  to publish
     *
     * @throws  RuntimeException  if publishing failed
     */
    public void publishQuery(String routingKey, Message message) {

        Message m = decorateMessage(message);

        this.template.send(queriesExchange.getName(), routingKey, m);
        logPublished("query", routingKey, m);
    }


    /**
     * Publishes a response to the given exchange and routing key, with the provided response message.
     *
     * <p>Any {@link RuntimeException} failures are caught, logged and ignored.</p>
     *
     * @param  exchange  address
     * @param  routingKey  address
     * @param  message  to publish
     */
    public void publishResponse(String exchange, String routingKey, Message message) {

        Message m = decorateMessage(message);

        try {
            this.template.send(exchange, routingKey, m);
            logPublished("response", routingKey, m);
        } catch (RuntimeException e) {
            log().error("Failed to publish response", e);
        }
    }


    protected void logPublished(String type, String routingKey, Message message) {

        if (log().isDebugEnabled()) {
            log().debug("|<-- Published {}: {} - {}", type, routingKey, message);
        } else {
            log().info("|<-- Published {}: {} - {}", type, routingKey, toStringRedacted(message));
        }
    }


    private String toStringRedacted(Message message) {

        StringBuilder buffer = new StringBuilder();
        buffer.append("(");
        buffer.append("Body:[").append(message.getBody().length).append("]");

        if (message.getMessageProperties() != null) {
            buffer.append(" ").append(message.getMessageProperties().toString());
        }

        buffer.append(")");

        return buffer.toString();
    }


    private Message decorateMessage(Message message) {

        return MessageBuilder.fromMessage(message)
            .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
            .setContentLength(message.getBody().length)
            .setHeader(HEADER_X_QR_PUBLISHED, System.currentTimeMillis())
            .build();
    }


    public void publishStatistics(Collection<Stat> stats) {

        log().warn("NOT YET PUBLISHING! {}", stats);
    }
}
