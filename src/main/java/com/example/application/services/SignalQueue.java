package com.example.application.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class SignalQueue extends EventQueue<JsonEvent> {
    public enum RootType {
        LIST, VALUE;
    }

    private static class Entry {
        private final UUID id;
        private UUID prev;
        private UUID next;
        private JsonNode value;

        public Entry(UUID id, UUID prev, UUID next, JsonNode value) {
            this.id = id;
            this.prev = prev;
            this.next = next;
            this.value = value;
        }

        @Override
        public String toString() {
            return id + ": " + value;
        }
    }

    private final Map<UUID, Entry> entries = new HashMap<>();
    private final ObjectMapper mapper;

    public SignalQueue(ObjectMapper mapper, RootType rootType) {
        this.mapper = mapper;

        JsonNode rootValue = switch(rootType) {
            case RootType.LIST -> createListRootValue(null, null);
            case RootType.VALUE -> TextNode.valueOf(""); // Good enough for now
            default -> throw new RuntimeException(rootType.name());
        };
        entries.put(EventQueue.ROOT, new Entry(EventQueue.ROOT, null, null, rootValue));
    }

    @Override
    protected void processEvent(JsonEvent event) {
        UUID id = event.getId();
        ObjectNode json = event.getJson();

        if (json.has("conditions")) {
            ArrayNode conditions = (ArrayNode) json.get("conditions");
            for(int i = 0; i < conditions.size(); i++) {
                JsonNode condition = conditions.get(i);
                Entry entry = entry(condition.get("id"));
                if (entry == null) {
                    // Condition not satisfied if it references a missing node
                    return;
                }

                if (condition.has("value") && !Objects.equals(condition.get("value"), entry.value)) {
                    return;
                }
            }
        }

        if (json.has("set")) {
            Entry entry = entry(json.get("set"));
            entry.value = json.get("value");
        } else if (json.has("remove")) {
            Entry parent = entry(json.get("parent"));
            Entry entry = entry(json.get("remove"));

            if (parent == null || entry == null) {
                return;
            }

            if (entry.prev != null) {
                entries.get(entry.prev).next = entry.next;
            } else {
                ObjectNode listEntry = (ObjectNode) parent.value;
                listEntry.put("head",toStringOrNull(entry.next));
            }

            if (entry.next != null) {
                entries.get(entry.next).prev = entry.prev;
            } else {
                ObjectNode listEntry = (ObjectNode) parent.value;
                listEntry.put("tail",toStringOrNull(entry.prev));
            }

            // XXX: Also detach any children
            entries.remove(entry.id);
        } else if (json.has("direction")) {
            // Insert event
            Entry listEntry = entry(json.get("entry"));
            String direction = json.get("direction").asText();
            UUID referenceId = uuidOrNull(json.get("reference"));
            JsonNode value = json.get("value");

            if (listEntry == null) {
                return;
            }

            ObjectNode listRoot = (ObjectNode) listEntry.value;

            UUID prev = null;
            UUID next = null;
            if("AFTER".equals(direction)) {
                prev = referenceId != null ? referenceId : uuidOrNull(listRoot.get("tail"));
                if (prev != null) {
                    Entry prevEntry = entries.get(prev);
                    if (prevEntry == null) {
                        return;
                    }
                    next = prevEntry.next;
                }
            } else {
                next = referenceId != null ? referenceId : uuidOrNull(listRoot.get("head"));
                if (next != null) {
                    Entry nextEntry = entries.get(next);
                    if (nextEntry == null) {
                        return;
                    }
                    prev = nextEntry.prev;
                }
            }

            Entry newEntry = new Entry(id, prev, next, value);
            entries.put(id, newEntry);

            if (next != null) {
                entries.get(next).prev = id;
            } else {
                listRoot.put("tail", id.toString());
            }

            if (prev != null) {
                entries.get(prev).next = id;
            } else {
                listRoot.put("head", id.toString());
            }
        } else {
            throw new RuntimeException("Unsupported JSON: " + json.toString());
        }

        List<Entry> state = new ArrayList<>();
        UUID key = uuidOrNull(entries.get(EventQueue.ROOT).value.get("head"));
        while(key != null) {
            Entry entry = entries.get(key);
            state.add(entry);
            key = entry.next;
        }
    }

    private Entry entry(JsonNode jsonNode) {
        return entries.get(UUID.fromString(jsonNode.asText()));
    }

    private static UUID uuidOrNull(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        } else {
            return UUID.fromString(jsonNode.asText());
        }
    }

    @Override
    public JsonEvent createSnapshot() {
        ArrayNode snapshotEntries = mapper.createArrayNode();
        entries.values().forEach(entry -> {
            ObjectNode entryNode = snapshotEntries.addObject();
            entryNode.put("id", entry.id.toString());
            entryNode.put("next", toStringOrNull(entry.next));
            entryNode.put("prev", toStringOrNull(entry.prev));
            entryNode.set("value", entry.value);
        });
        
        ObjectNode snapshotData = mapper.createObjectNode();
        snapshotData.set("entries", snapshotEntries);
        return new JsonEvent(null, snapshotData);
    }

    private static String toStringOrNull(UUID uuid) {
        return Objects.toString(uuid, null);
    }

    private ObjectNode createListRootValue(UUID head, UUID tail) {
        ObjectNode node = mapper.createObjectNode();
        node.put("head", toStringOrNull(head));
        node.put("tail", toStringOrNull(tail));
        return node;
    }
}
