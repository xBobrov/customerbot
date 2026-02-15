package com.vodokanal.customerbot.enums;

public enum Operation {
    BINDING_ID("binding_id"),
    START("start"),
    ADD_METER("add_meter");

    private final String operation;

    Operation(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}
