package com.vodokanal.customerbot.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * A component for receiving and filtration of Telegram API updates.
 * <p>
 * Implements {@link LongPollingSingleThreadUpdateConsumer} interface providing
 * consistent processing of incoming events in a single thread.
 * Allocate updates between text messages and callback-queries.
 * </p>
 */
@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {
    private final ChatService chatService;

    public UpdateConsumer(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Perform checking of event type.
     * <ul>
     *     <li>In case of text message transmits to {@link ChatService#handleMessage}.</li>
     *     <li>In case of button press (CallbackQuery) — transmits to {@link ChatService#handleCallbackQuery}.</li>
     * </ul>
     * </p>
     *
     * @param update an object containing chat event.
     */
    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            chatService.handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            chatService.handleCallbackQuery(update.getCallbackQuery());
        }
    }
}
