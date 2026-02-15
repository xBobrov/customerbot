package com.vodokanal.customerbot.service;

import com.vodokanal.customerbot.util.MappingUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class RabbitMQMessageService {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue.name}")
    private String messageQueue;

    @Autowired
    public RabbitMQMessageService(RabbitTemplate rabbitTemplate, MappingUtil mappingUtil) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public String sendMessage(String json) {
        return (String) rabbitTemplate.convertSendAndReceive(messageQueue, json);
    }
}
