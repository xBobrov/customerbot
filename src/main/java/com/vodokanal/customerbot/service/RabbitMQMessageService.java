package com.vodokanal.customerbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class RabbitMQMessageService {
    private final RabbitTemplate rabbitTemplate;
    private final Logger logger = LoggerFactory.getLogger(RabbitMQMessageService.class);

    @Value("${rabbitmq.queue.name}")
    private String messageQueue;

    @Autowired
    public RabbitMQMessageService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

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
