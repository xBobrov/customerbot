package com.vodokanal.customerbot.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ChatUtilTest {
    private final ChatUtil chatUtil = new ChatUtil();

    @ParameterizedTest
    @ValueSource(strings = {"9878", "9878.1", "9878.123", "0.1", "0"})
    void shouldReturnTrueIfMeterValueFormatValid(String value) {
        assertTrue(chatUtil.isMeterValueFormatValid(value), "Should be true for: " + value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"xz78", "9878.1243", ".1243", "9878.", "9878,123", "-9878,123", ""})
    void shouldReturnFalseIfMeterValueFormatInvalid(String value) {
        assertFalse(chatUtil.isMeterValueFormatValid(value), "Should be false for: " + value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0123-456-7"})
    void shouldReturnTrueIfAccountNumberFormatValid(String value) {
        assertTrue(chatUtil.isAccountNumberFormatValid(value), "Should be true for: " + value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123-456-7",
            "12345-678-9",
            "1234-5678-9",
            "1234-567-12",
            "1234.567.8",
            "1234-567-A",
            "1234-567-8 ",
            ""})
    void shouldReturnFalseIfAccountNumberFormatInvalid(String value) {
        assertFalse(chatUtil.isAccountNumberFormatValid(value), "Should be false for: " + value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "simple@example.com",
            "user.name@domain.co.jp",
            "plus+style@gmail.com",
            "12345@domain.org"
    })
    void shouldReturnTrueForValidEmails(String email) {
        assertTrue(chatUtil.isEmailFormatValid(email), "Should be true for: " + email);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plainaddress",
            "@missing-login.com",
            "user@example",
            "user@example.c",
            "user@.com",
            "user@example.123",
            "user name@example.com",
            "email@://domain.com"
    })
    void shouldReturnFalseForInvalidEmails(String email) {
        assertFalse(chatUtil.isEmailFormatValid(email), "Should be false for: " + email);
    }

    @ParameterizedTest
    @CsvSource({
            "100.50, 100.49, true",
            "100.50, 100.50, true",
            "100.5, 100.500, true",
            "50.00, 100.00, false",
            "0, 0, true"
    })
    void testCurrentValueValid(String current, String last, boolean expected) {
        BigDecimal val1 = new BigDecimal(current);
        BigDecimal val2 = new BigDecimal(last);

        assertEquals(expected, chatUtil.isCurrentValueValid(val1, val2));
    }
}
