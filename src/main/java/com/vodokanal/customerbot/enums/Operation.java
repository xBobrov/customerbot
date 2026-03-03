package com.vodokanal.customerbot.enums;

public enum Operation {
    BIND_ID("binding_id"),
    START("start"),
    METER_INFO("meter_info");

    private final String operation;

    Operation(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}
