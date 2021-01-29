package com.studiomediatech.queryresponseui.config;

import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.studiomediatech.queryresponse.QueryBuilder;
import com.studiomediatech.queryresponseui.SimpleWebSocketHandler;
import com.studiomediatech.queryresponseui.events.AsyncEventEmitter;
import com.studiomediatech.queryresponseui.queries.ScheduledStats;

@Configuration
@EnableScheduling
@EnableWebSocket
public class QueryResponseUIConfig {

    @Bean
    ConnectionNameStrategy connectionNameStrategy(Environment env) {

        return connectionFactory -> env.getProperty("spring.application.name", "query-response-ui");
    }

    @Bean
    @Primary
    TaskScheduler taskScheduler(TaskSchedulerBuilder builder) {

        return builder.build();
    }

    @Bean
    AsyncEventEmitter asyncEventEmitter(TaskScheduler scheduler, ApplicationEventPublisher publisher) {

        return new AsyncEventEmitter(scheduler, publisher);
    }

    @Bean
    SimpleWebSocketHandler handler(AsyncEventEmitter asyncEventEmitter) {

        return new SimpleWebSocketHandler(asyncEventEmitter);
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
