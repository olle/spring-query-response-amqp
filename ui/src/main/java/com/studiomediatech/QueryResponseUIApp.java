package com.studiomediatech;

import com.studiomediatech.events.AsyncEventEmitter;
import com.studiomediatech.events.EventEmitter;

import com.studiomediatech.queryresponse.EnableQueryResponse;
import com.studiomediatech.queryresponse.QueryBuilder;

import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@SpringBootApplication
@EnableQueryResponse
@EnableScheduling
@EnableWebSocket
public class QueryResponseUIApp {

    public static void main(String[] args) {

        SpringApplication.run(QueryResponseUIApp.class);
    }

    @Order(10)
    @Configuration
    static class AppConfig {

        @Bean
        ConnectionNameStrategy connectionNameStrategy(Environment env) {

            return connectionFactory -> env.getProperty("spring.application.name", "query-response-ui");
        }


        @Bean
        @Primary
        TaskScheduler taskScheduler() {

            ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
            taskScheduler.setPoolSize(7);

            return taskScheduler;
        }


        @Bean
        EventEmitter eventEmitter(TaskScheduler scheduler, ApplicationEventPublisher publisher) {

            return new AsyncEventEmitter(scheduler, publisher);
        }


        @Bean
        SimpleWebSocketHandler handler(EventEmitter emitter) {

            return new SimpleWebSocketHandler(emitter);
        }


        @Bean
        QueryPublisher querier(SimpleWebSocketHandler handler, QueryBuilder queryBuilder) {

            return new QueryPublisher(handler, queryBuilder);
        }
    }

    @Order(100)
    @Configuration
    static class WebSocketConfig implements WebSocketConfigurer {

        private final SimpleWebSocketHandler webSocketHandler;

        public WebSocketConfig(SimpleWebSocketHandler webSocketHandler) {

            this.webSocketHandler = webSocketHandler;
        }

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

            registry.addHandler(webSocketHandler, "/ws");
        }
    }
}
