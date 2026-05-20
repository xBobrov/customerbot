package com.vodokanal.customerbot.service;

import com.vodokanal.customerbot.enums.UserState;
import com.vodokanal.customerbot.model.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A service for managing user data and states in RAM. It usees {@code ConcurrentHashMap}
 * to ensure thread safety when interacting with the Telegram API.
 */
@Service
public class UserService {
    private final Map<Long, User> userMap = new ConcurrentHashMap<>();

    /**
     * Returns existing user or create a new one with the default state.
     * <p>
     * Using {@code computeIfAbsent} ensures operation atomicity and
     * prevent duplication during concurrent requests.
     * New users are automatically assigned the {@link UserState#START}.
     * </p>
     *
     * @param chatID unique user identifier in Telegram.
     * @return object {@link User}, never returns {@code null}.
     */
    public User findOrCreateUser(long chatID) {
        return userMap.computeIfAbsent(chatID, id -> {
            User user = new User(id);
            user.setUserState(UserState.START); // Состояние по умолчанию
            return user;
        });
    }

    public boolean isUserExist(long chatID) {
        return userMap.containsKey(chatID);
    }
}
