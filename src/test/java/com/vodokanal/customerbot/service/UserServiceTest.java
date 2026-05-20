package com.vodokanal.customerbot.service;

import com.vodokanal.customerbot.enums.UserState;
import com.vodokanal.customerbot.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;
    private final long CHAT_ID = 98765432;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    @DisplayName("Should create new user if absent in map")
    void shouldCreateNewUserIfAbsent() {
        User user = userService.findOrCreateUser(CHAT_ID);

        assertNotNull(user);
        assertEquals(CHAT_ID, user.getChatID());
        assertEquals(UserState.START, user.getUserState());
        assertTrue(userService.isUserExist(CHAT_ID));
    }

    @Test
    @DisplayName("Should return existing user instead creating new")
    void shouldReturnExistingUser() {
        User firstInstance = userService.findOrCreateUser(CHAT_ID);
        firstInstance.setEmail("test@test.com");

        User secondInstance = userService.findOrCreateUser(CHAT_ID);

        assertSame(firstInstance, secondInstance, "Should return the same user");
        assertEquals("test@test.com", secondInstance.getEmail());
    }

    @Test
    @DisplayName("Should check user existence")
    void shouldCheckUserExistence() {
        assertFalse(userService.isUserExist(CHAT_ID));

        userService.findOrCreateUser(CHAT_ID);

        assertTrue(userService.isUserExist(CHAT_ID));
    }
}
