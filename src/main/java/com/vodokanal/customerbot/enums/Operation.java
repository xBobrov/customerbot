package com.vodokanal.customerbot.enums;

public enum Operation {
    BIND_ID("binding_id"),
    START("start"),
    METER_INFO("meter_info"),
    EMAIL_INFO("email_info"),
    METER_VALIDATION("meter_validation"),
    READING_TRANSMIT("reading_transmit"),
    CHANGE_EMAIL("change_email");

    private final String operation;

    Operation(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}
