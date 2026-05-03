package com.vodokanal.customerbot.model;

import com.vodokanal.customerbot.enums.UserState;

public class User {
    private long chatID;
    private UserState userState;
    private String email;
    private String meterNumber;
    private String lastReadingValue;
    private String currentReadingValue;
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
}
