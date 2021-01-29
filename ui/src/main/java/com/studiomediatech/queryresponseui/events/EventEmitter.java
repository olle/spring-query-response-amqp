package com.studiomediatech.queryresponseui.events;

/**
 * Declares the contract provided to clients, on how an event can be emitted.
 */
@FunctionalInterface
public interface EventEmitter {

    void emitEvent(Object event);
}
