package uk.co.autotrader.springpractice.service;

import net.minidev.json.JSONObject;

public enum Messages {
    TODO_ITEM_NOT_FOUND("Status", "Todo item not found for given ID"),
    FAILED_TO_PROCESS_JSON("Status", "Failed to process JSON"),
    SUCCESSFULLY_ADDED_TODO_ITEM("Status", "Successfully added Todo Item"),
    SUCCESSFULLY_DELETED_TODO_ITEM("Status", "Successfully deleted Todo Item"),
    SUCCESSFULLY_COMPLETED_TODO_ITEM("Status", "Successfully completed Todo Item"),
    INVALID_FIELDS_TODO_ITEM_REQUEST("Status", "Invalid fields sent for Todo Item");

    private final String jsonKey;
    private final String jsonValue;

    Messages(String jsonKey, String jsonValue ) {
        this.jsonKey = jsonKey;
        this.jsonValue = jsonValue;

    }
    public JSONObject getJsonValue() {
        JSONObject message = new JSONObject();
        message.put(jsonKey, jsonValue);
        return message;
    }
}

