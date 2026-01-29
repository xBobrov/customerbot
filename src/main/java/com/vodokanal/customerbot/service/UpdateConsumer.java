package com.vodokanal.customerbot.service;

import com.vodokanal.customerbot.dto.DatabaseRequestDto;
import com.vodokanal.customerbot.dto.DatabaseResponseDto;
import com.vodokanal.customerbot.enums.Operation;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
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
