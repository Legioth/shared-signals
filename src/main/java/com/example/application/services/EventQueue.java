package com.example.application.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

public abstract class EventQueue<T extends StateEvent> {
    private final ReentrantLock lock = new ReentrantLock();

    public static final UUID ROOT = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF");

    private static class Entry<T extends StateEvent> {
        private final T value;
        private Entry<T> next;

        private Entry(T value) {
            this.value = value;
        }
    }
    private Entry<T> head;
    private Entry<T> tail;
    private final Map<UUID, Entry<T>> idToEntry = new HashMap<>();

    private final Set<Many<T>> subscribers = new HashSet<>();

    public Flux<T> subscribe(UUID continueFrom) {
        System.out.println("Continue from " + continueFrom);
        Many<T> sink = Sinks.many().multicast().onBackpressureBuffer();

        return sink.asFlux().doOnSubscribe(ignore -> {
            System.out.println("New Flux subscription");

            lock.lock();
            try {
                Entry<T> entry;
                if (continueFrom != null && (entry = idToEntry.get(continueFrom)) != null) {
                        entry = entry.next;
                        // TODO maybe some heuristic to determine whether it would be more efficient to restart from a snapshot instead of replaying lots of events?
                        while (entry != null) {
                            System.out.println("Replay " + entry.value.getId());
                            sink.tryEmitNext(entry.value);
                            entry = entry.next;
                        };
                } else {
                    T snapshot = createSnapshot();
                    if (snapshot != null) {
                        sink.tryEmitNext(snapshot);
                    }
                }

                subscribers.add(sink);
            } finally {
                lock.unlock();
            }
        }).doFinally(ignore -> {
            System.out.println("doFinally");
            lock.lock();
            try {
                subscribers.remove(sink);
            } finally {
                lock.unlock();
            }
        });
    }

    public void submit(T event) {
        // Thread.ofVirtual().start(() -> append(event));
        append(event);
    }

    private void append(T event) {
        lock.lock();
        try {
            processEvent(event);
            Entry<T> entry = new Entry<>(event);

            // Add to linked list
            idToEntry.put(event.getId(), entry);
            if (head == null) {
                head = tail = entry;
            } else {
                tail.next = tail = entry;
            }

            // Truncate list
            // TODO configurable or dynamic limit?
            if (idToEntry.size() > 100) {
                Entry<T> removed = idToEntry.remove(head.value.getId());
                head = removed.next;
            }

            // Notify subscribers
            subscribers.removeIf(sink -> {
                boolean failure = sink.tryEmitNext(event).isFailure();
                if (failure) {
                    System.out.println("Failed push");
                }
                return failure;
            });
        } finally {
            lock.unlock();
        }
    }

    protected abstract void processEvent(T newEvent);
    protected abstract T createSnapshot();
 }
