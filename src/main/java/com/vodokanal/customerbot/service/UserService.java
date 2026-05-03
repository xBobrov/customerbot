package com.vodokanal.customerbot.service;

import com.vodokanal.customerbot.enums.UserState;
import com.vodokanal.customerbot.model.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {
    private final Map<Long, User> userMap = new ConcurrentHashMap<>();

    public void setEmail(long chatID, String email) {
        userMap.get(chatID).setEmail(email);
    }

    public void setState(long chatID, UserState userState) {
        userMap.computeIfAbsent(chatID, User::new).setUserState(userState);
    }

    public boolean isUserExist(long chatID) {
        return userMap.containsKey(chatID);
    }

    public UserState getUserState(long chatID) {
        return userMap.get(chatID).getUserState();
    }

    public void setMeterNumber(long chatID, String meterNumber) {
        userMap.get(chatID).setMeterNumber(meterNumber);
    }

    public void setLastReading(long chatID, String lastReading) {
        userMap.get(chatID).setLastReadingValue(lastReading);
    }

    public String getLastReading(long chatID) {
        return userMap.get(chatID).getLastReadingValue();
    }

    public void setCurrentReading(long chatID, String currentReading) {
        userMap.get(chatID).setCurrentReadingValue(currentReading);
    }

    public String getMeterNumber(long chatID) {
        return userMap.get(chatID).getMeterNumber();
    }

    public String getCurrentReading(long chatID) {
        return userMap.get(chatID).getCurrentReadingValue();
    }

    public String getConsumption(long chatID) {
        return userMap.get(chatID).getConsumption();
    }

    public void setConsumption(long chatID, String consumption) {
        userMap.get(chatID).setConsumption(consumption);
    }
}
