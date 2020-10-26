package com.studiomediatech.queryresponse;

import com.studiomediatech.queryresponse.util.Logging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

import org.springframework.core.env.Environment;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


/**
 * Configures the required components for a Query/Response client, ensuring the availability of the necessary AMQP
 * resources as well as a {@link QueryRegistry} and a {@link ResponseRegistry}.
 */
@Configuration
@ConditionalOnClass(RabbitAutoConfiguration.class)
@AutoConfigureAfter(RabbitAutoConfiguration.class)
@Import(RabbitAutoConfiguration.class)
@EnableConfigurationProperties(QueryResponseConfigurationProperties.class)
class QueryResponseConfiguration implements Logging {

    public static final String QUERY_RESPONSE_TOPIC_EXCHANGE_BEAN_NAME = "queryResponseTopicExchange";

    private final QueryResponseConfigurationProperties props;

    public QueryResponseConfiguration(QueryResponseConfigurationProperties props) {

        this.props = props;
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryBuilder queryBuilder(QueryRegistry registry) {

        return new QueryBuilder(registry);
    }


    @Bean
    @ConditionalOnMissingBean
    public ResponseBuilder responseBuilder(ResponseRegistry registry) {

        return new ResponseBuilder(registry);
    }


    @Bean
    @ConditionalOnMissingBean
    RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {

        return new RabbitAdmin(connectionFactory);
    }


    @Bean
    RabbitFacade rabbitFacade(RabbitAdmin rabbitAdmin, ConnectionFactory connectionFactory,
        @Qualifier(QUERY_RESPONSE_TOPIC_EXCHANGE_BEAN_NAME) TopicExchange queryResponseTopicExchange,
        GenericApplicationContext ctx) {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        return new RabbitFacade(rabbitAdmin, rabbitTemplate, connectionFactory, queryResponseTopicExchange, ctx);
    }


    @Bean(QUERY_RESPONSE_TOPIC_EXCHANGE_BEAN_NAME)
    TopicExchange queryResponseTopicExchange() {

        String name = props.getExchange().getName();

        boolean durable = false;
        boolean autoDelete = true;

        return log(new TopicExchange(name, durable, autoDelete));
    }


    @Bean
    Statistics statistics(Environment env, ApplicationContext ctx, RabbitFacade rabbitFacade,
        TaskScheduler taskScheduler) {

        return new Statistics(env, ctx, rabbitFacade, taskScheduler);
    }


    @Bean
    ResponseRegistry responseRegistry(RabbitFacade facade, Statistics stats) {

        return new ResponseRegistry(facade, stats);
    }


    @Bean
    QueryRegistry queryRegistry(RabbitFacade facade, Statistics stats) {

        return new QueryRegistry(facade, stats);
    }


    @Bean
    @ConditionalOnMissingBean
    public TaskScheduler queryResponseTaskScheduler() {

        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

        taskScheduler.setThreadNamePrefix("queryResponseTaskScheduler");
        taskScheduler.setPoolSize(5);

        return taskScheduler;
    }
}
