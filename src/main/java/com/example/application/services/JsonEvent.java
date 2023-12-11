package com.example.application.services;

import java.util.Map.Entry;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonEvent extends StateEvent {

    private final ObjectNode json;

    public JsonEvent(UUID id, ObjectNode json) {
        super(id);
        this.json = json;
    }

    public ObjectNode getJson() {
        return json;
    }

    public String toJson(ObjectMapper mapper) {
        ObjectNode root = mapper.createObjectNode();
        for (Entry<String,JsonNode> entry : json.properties()) {
            root.set(entry.getKey(), entry.getValue());
        }
        UUID id = getId();
        root.put("id", id != null ? id.toString() : null);
        return root.toString();
    }

    public static JsonEvent fromJson(String json, ObjectMapper mapper) {
        try {
            ObjectNode root = (ObjectNode) mapper.readTree(json);
            UUID id = UUID.fromString(root.get("id").asText());
            root.remove("id");
            return new JsonEvent(id, root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
