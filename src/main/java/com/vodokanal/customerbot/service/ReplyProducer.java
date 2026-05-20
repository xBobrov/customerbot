package com.vodokanal.customerbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * A component responsible for sending user replies to Telegram.
 * <p>
 * Acts as a wrapper for {@link TelegramClient}, encapsulating
 * networking logic and initial exception handling during transmission.
 * </p>
 */
@Component
public class ReplyProducer {
    private final TelegramClient telegramClient;
    private static final Logger logger = LoggerFactory.getLogger(ReplyProducer.class);

    public ReplyProducer() {
        this.telegramClient = new OkHttpTelegramClient(System.getenv("TELEGRAM_BOT_TOKEN"));
    }

    /**
     * Executes sending of built message.
     * <p>
     * The method logs the sending process and catches {@link TelegramApiException}
     * to prevent interruption of the main processing thread due to network failures.
     * </p>
     *
     * @param message built object of {@link SendMessage}.
     */
    public void executeReply(SendMessage message) {
        logger.info("Attempting to send message to chatId: {}. Text preview: [{}]",
                message.getChatId(), truncateText(message.getText()));

        try {
            telegramClient.execute(message);
            logger.debug("Message successfully sent to chatId: {}", message.getChatId());
        } catch (TelegramApiException e) {
            logger.error("Failed to send telegram message to {}. Error: {}", message.getChatId(), e.getMessage());
        }
    }

    /**
     * Truncates message text for propper logging.
     *
     * @param text source message text.
     * @return truncated string (30 chars max) or 'empty'.
     */
    private String truncateText(String text) {
        if (text == null) return "empty";
        return text.length() > 30 ? text.substring(0, 27) + "..." : text;
    }
}
