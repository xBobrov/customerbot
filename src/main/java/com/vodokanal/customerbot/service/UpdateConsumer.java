package com.vodokanal.customerbot.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {
    private final ChatService chatService;

    public UpdateConsumer(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            chatService.handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            chatService.handleCallbackQuery(update.getCallbackQuery());
        }
    }
}
