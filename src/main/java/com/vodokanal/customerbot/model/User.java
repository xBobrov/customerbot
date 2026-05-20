package com.vodokanal.customerbot.model;

import com.vodokanal.customerbot.enums.UserState;

/**
 * User data model representing its current state and session.
 * <p>
 * The Class is used for temporary data storage inputted by user in dialog
 * (meter number, meter reading, account number) and also for tracking the
 * current scenario step via {@link UserState}.
 * </p>
 */
public class User {
    /** Unique identifier of user chat in Telegram */
    private long chatID;

    /** Current user state in chat */
    private UserState userState;

    /** Email bound to user account */
    private String email;

    /** Serial number of the metering device */
    private String meterNumber;

    /** Value of previously submitted reading */
    private String lastReadingValue;

    /** Value of current reading */
    private String currentReadingValue;

    /** Value of resource consumed */
    private String consumption;

    public User(long chatID) {
        this.chatID = chatID;
    }

    public UserState getUserState() {
        return userState;
    }

    public void setUserState(UserState userState) {
        this.userState = userState;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
    }

    public String getCurrentReadingValue() {
        return currentReadingValue;
    }

    public void setCurrentReadingValue(String currentReadingValue) {
        this.currentReadingValue = currentReadingValue;
    }

    public String getLastReadingValue() {
        return lastReadingValue;
    }

    public void setLastReadingValue(String lastReadingValue) {
        this.lastReadingValue = lastReadingValue;
    }

    public String getConsumption() {
        return consumption;
    }

    public void setConsumption(String consumption) {
        this.consumption = consumption;
    }

    public long getChatID() {
        return chatID;
    }
}
