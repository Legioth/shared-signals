package com.example.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import dev.hilla.Nullable;
import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

@BrowserCallable
@AnonymousAllowed
@Service
public class HelloWorldService {
    private final ObjectMapper mapper;

    private final SignalQueue cursors;
    private final SignalQueue list;
    private final SignalQueue value;
    private final SignalQueue todos;
    private final SignalQueue counter;

    public HelloWorldService(ObjectMapper mapper) {
        Objects.requireNonNull(mapper);
        this.mapper = mapper;

        cursors = new SignalQueue(mapper, null);
        list = new SignalQueue(mapper, null);
        value = new SignalQueue(mapper, TextNode.valueOf(""));
        todos = new SignalQueue(mapper, null);
        counter = new SignalQueue(mapper, IntNode.valueOf(0));
    }

    public Flux<String> subscribeValue(@Nullable UUID continueFrom) {
        return value.subscribe(continueFrom).map(event -> event.toJson(mapper));
    }

    public void updateValue(String event) {
        value.submit(JsonEvent.fromJson(event, mapper));
    }

    public Flux<String> subscribeList(@Nullable UUID continueFrom) {
        return list.subscribe(continueFrom).map(event -> event.toJson(mapper));
    }

    public void updateList(String event) {
        list.submit(JsonEvent.fromJson(event, mapper));
    }
    
    public Flux<String> subscribeCursors(@Nullable UUID continueFrom) {
        return cursors.subscribe(continueFrom).map(event -> event.toJson(mapper));
    }

    public void updateCursors(String event) {
        cursors.submit(JsonEvent.fromJson(event, mapper));
    }

    public Flux<String> subscribeTodos(@Nullable UUID continueFrom) {
        return todos.subscribe(continueFrom).map(event -> event.toJson(mapper));
    }

    public void updateTodos(String event) {
        todos.submit(JsonEvent.fromJson(event, mapper));
    }

    public Flux<String> subscribeCounter(@Nullable UUID continueFrom) {
        return counter.subscribe(continueFrom).map(event -> event.toJson(mapper));
    }

    public void updateCounter(String event) {
        counter.submit(JsonEvent.fromJson(event, mapper));
    }
}
