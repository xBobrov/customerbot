package com.vodokanal.customerbot.enums;

/**
 * Types of operation submitting into external system via RabbitMQ.
 */
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
