package com.vodokanal.customerbot.util;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import com.vodokanal.customerbot.service.ChatService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A util service designed for data validation and subsidiary functions for main
 * {@link ChatService}.
 * <p>
 * Includes subsidiary methods to validate input format (Email, accounts, readings)
 * and also encapsulates Telegram messages and keyboards building logic.
 * <p>
 */
@Service
public class ChatUtil {

    /**
     * Validates format of metering device reading.
     * <p>
     * Valid format is a {@code String} containing integer
     * or decimal up to three decimal places. {@code .} as
     * a delimiter
     *
     * @param value reading string
     * @return {@code true} in case of valid reading format.
     */
    public boolean isMeterValueFormatValid(String value) {
        return value.matches("^\\d+(\\.\\d{1,3})?$");
    }

    /**
     * Validates format of account number.
     * <p>
     * Valid format is ####-###-# where # - any digit.
     *
     * @param accountNumber account number string
     * @return {@code true} in case of valid account number format.
     */
    public boolean isAccountNumberFormatValid(String accountNumber) {
        return accountNumber.matches("\\d{4}-\\d{3}-\\d");
    }

    /**
     * Validates Email format with the regex.
     *
     * @param email email address string.
     * @return {@code true} if email address format is correct.
     */
    public boolean isEmailFormatValid(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Builds base object {@link SendMessage} for user message sending.
     *
     * @param text   message text.
     * @param chatID Telegram chat ID.
     * @return built message object.
     */
    public SendMessage buildMessage(String text, long chatID) {

        return SendMessage.builder()
                .text(text)
                .chatId(chatID)
                .build();
    }

    /**
     * Builds a single button object for Inline-keyboard.
     *
     * @param text text of a button to build.
     * @param callbackData name of the event to perform when button pressed.
     * @return button object {@link InlineKeyboardButton}.
     */
    private InlineKeyboardButton buildButton(String text, String callbackData) {

        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    /**
     * Dynamically builds a vertical Inline-keyboard from strings array.
     *
     * @param buttonData strings array: [button_text_1, callback_data_1, button_text_2, ...].
     * @return keyboard object {@link InlineKeyboardMarkup}.
     */
    public InlineKeyboardMarkup buildKeyboard(String[] buttonData) {
        List<InlineKeyboardRow> keyboardRowList = IntStream.iterate(0, i -> i < buttonData.length, i -> i + 2)
                .mapToObj(i -> new InlineKeyboardRow(buildButton(buttonData[i], buttonData[i + 1])))
                .toList();

        return new InlineKeyboardMarkup(keyboardRowList);
    }

    /**
     * Validates that current day of month is in regulated
     * period for metering devices readings receiving.
     *
     * @return {@code true} if date is correct.
     */
    public boolean isDateValid() {
        int currentDay = LocalDate.now().getDayOfMonth();

        return currentDay >= Constants.READING_START_DAY && currentDay <= Constants.READING_END_DAY;
    }

    /**
     * Validates that value of current reading is equal higher than one previously received.
     *
     * @param currentValue current reading.
     * @param lastReadingValue previously transmitted and saved reading.
     * @return {@code true} if current reading >= previous reading
     */
    public boolean isCurrentValueValid(BigDecimal currentValue, BigDecimal lastReadingValue) {
        return currentValue.compareTo(lastReadingValue) >= 0;
    }
}
