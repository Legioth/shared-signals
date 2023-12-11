package com.example.application.services;

import java.util.UUID;

public class StateEvent {
    private final UUID id;

    public StateEvent(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
