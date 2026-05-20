package com.vodokanal.customerbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * A service for synchronous interaction with RabbitMQ queues.
 * <p>
 * Implements the Request-Response (RPC) pattern allowing JSON
 * requests to be sent to an external system while awaiting
 * processing results in blocking mode.
 * </p>
 */
@Service
public class RabbitMQMessageService {
    private final RabbitTemplate rabbitTemplate;
    private final Logger logger = LoggerFactory.getLogger(RabbitMQMessageService.class);

    /** Queue name which the message is sent into. Set up in application.properties. */
    @Value("${rabbitmq.queue.name}")
    private String messageQueue;

    @Autowired
    public RabbitMQMessageService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Sends JSON message into the queue and awaits JSON-response.
     * <p>
     * The method uses {@code convertSendAndReceive} which make request synchronous.
     * If the response is not received within a set time the method returns {@code null}.
     * </p>
     *
     * @param json JSON string containing request data.
     * @return JSON response string or {@code null} in case of timeout.
     * @throws AmqpException if a protocol-level or message broker error occurs.
     */
    public String sendMessage(String json) {
        logger.info("Sending message to queue {}", messageQueue);
        logger.debug("Payload: {}", json);

        try {
            String response = (String) rabbitTemplate.convertSendAndReceive(messageQueue, json);

            if (response == null) {
                logger.warn("Receive timeout: no response from queue '{}'", messageQueue);
            } else {
                logger.info("Response received successfully");
                logger.debug("Response body: {}", response);
            }

            return response;
        } catch (AmqpException e) {
            logger.error("Fail to send message due to {}", e.getMessage());
            throw e;
        }
    }
}
