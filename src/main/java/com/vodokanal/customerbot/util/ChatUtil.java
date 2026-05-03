package com.vodokanal.customerbot.util;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class ChatUtil {

    public boolean isMeterValueFormatValid(String value) {
        return value.matches("^\\d+(\\.\\d{1,3})?$");
    }

    public boolean isAccountNumberFormatValid(String accountNumber) {
        return accountNumber.matches("\\d{4}-\\d{3}-\\d");
    }

    public boolean isEmailFormatValid(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    public SendMessage buildMessage(String text, long chatID) {

        return SendMessage.builder()
                .text(text)
                .chatId(chatID)
                .build();
    }

    private InlineKeyboardButton buildButton(String text, String callbackData) {

        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    public InlineKeyboardMarkup buildKeyboard(String[] buttonData) {
        List<InlineKeyboardRow> keyboardRowList = IntStream.iterate(0, i -> i < buttonData.length, i -> i + 2)
                .mapToObj(i -> new InlineKeyboardRow(buildButton(buttonData[i], buttonData[i + 1])))
                .toList();

        return new InlineKeyboardMarkup(keyboardRowList);
    }

    public boolean isDateValid() {
        int currentDay = LocalDate.now().getDayOfMonth();

        return currentDay >= Constants.READING_START_DAY && currentDay <= Constants.READING_END_DAY;
    }

    public boolean isCurrentValueValid(BigDecimal currentValue, BigDecimal lastReadingValue) {
        return currentValue.compareTo(lastReadingValue) >= 0;
    }
}
