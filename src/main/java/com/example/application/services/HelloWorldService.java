package com.example.application.services;

import com.example.application.services.SignalQueue.RootType;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public HelloWorldService(ObjectMapper mapper) {
        Objects.requireNonNull(mapper);
        this.mapper = mapper;

        cursors = new SignalQueue(mapper, RootType.LIST);
        list = new SignalQueue(mapper, RootType.LIST);
        value = new SignalQueue(mapper, RootType.VALUE);
        todos = new SignalQueue(mapper, RootType.LIST);
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
}
