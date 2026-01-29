package com.vodokanal.customerbot.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class ReplyProducer {
    private final TelegramClient telegramClient;

    public ReplyProducer() {
        this.telegramClient = new OkHttpTelegramClient(System.getenv("TELEGRAM_BOT_TOKEN"));
    }

    public void sendReply(boolean isUserSignedUp, long chatID) {
        String text = isUserSignedUp ? "" : " не";

        SendMessage message = SendMessage.builder()
                .text("Ваш аккаунт%s привязан к лицевому счету.".formatted(text))
                .chatId(chatID)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendSignUpReply(long chatID) {
        SendMessage message = buildMessage("Добро пожаловать в телеграм бот АО \"Водоканал\"", chatID);

        String[] buttonData = new String[]{"Привязать аккаунт к лицевому счету", "signup"};
        message.setReplyMarkup(buildKeyboard(buttonData));

        executeReply(message);
    }

    public void executeReply(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private SendMessage buildMessage(String text, long chatID) {

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

    private InlineKeyboardMarkup buildKeyboard(String[] buttonData) {
        List<InlineKeyboardRow> keyboardRowList = IntStream.iterate(0, i -> i < buttonData.length, i -> i + 2)
        .mapToObj(i -> new InlineKeyboardRow(buildButton(buttonData[i], buttonData[i + 1])))
        .toList();

        return new InlineKeyboardMarkup(keyboardRowList);
    }
}
