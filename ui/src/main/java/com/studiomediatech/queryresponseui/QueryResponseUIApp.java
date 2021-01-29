package com.studiomediatech.queryresponseui;

import com.studiomediatech.queryresponse.EnableQueryResponse;
import com.studiomediatech.queryresponse.QueryBuilder;
import com.studiomediatech.queryresponseui.events.AsyncEventEmitter;
import com.studiomediatech.queryresponseui.events.EventEmitter;

import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.task.TaskSchedulerBuilder;
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
public class QueryResponseUIApp {

	public static void main(String[] args) {

		SpringApplication.run(QueryResponseUIApp.class);
	}

}
