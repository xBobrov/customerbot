package com.vodokanal.customerbot.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class ReplyProducer {
    private final TelegramClient telegramClient;

    public ReplyProducer() {
        this.telegramClient = new OkHttpTelegramClient(System.getenv("TELEGRAM_BOT_TOKEN"));
    }

    public void executeReply(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
