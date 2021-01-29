package com.studiomediatech.queryresponseui.events;

import org.springframework.context.ApplicationEventPublisher;

import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;


/**
 * An implementation that ensures decoupling from the calling thread.
 */
public class AsyncEventEmitter implements EventEmitter {

    private final TaskScheduler scheduler;
    private final ApplicationEventPublisher publisher;

    public AsyncEventEmitter(TaskScheduler scheduler, ApplicationEventPublisher publisher) {

        this.scheduler = scheduler;
        this.publisher = publisher;
    }

    @Override
    public void emitEvent(Object event) {

        // NOTE: Scheduled ASAP for any instant in the past.
        scheduler.schedule(() -> publisher.publishEvent(event), Instant.EPOCH);
    }
}
