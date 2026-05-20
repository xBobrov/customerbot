package com.vodokanal.customerbot.enums;

/**
 * Enumeration of possible user's states.
 * <p>
 * Used for control of current step of dialogue and determination
 * of user's text input.
 * </p>
 */
public enum UserState {
    START,
    ASKED_EMAIL,
    ASKED_METER_NUMBER,
    ASKED_METER_CURRENT_READING,
    ASKED_METER_SERVICE,
    ASKED_CHEK,
    ASKED_ACCOUNT_NUMBER
}
