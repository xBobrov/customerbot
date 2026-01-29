package com.vodokanal.customerbot.service;

import com.vodokanal.customerbot.dto.DatabaseRequestDto;
import com.vodokanal.customerbot.dto.DatabaseResponseDto;
import com.vodokanal.customerbot.util.MappingUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class RabbitMQMessageService {
    private final RabbitTemplate rabbitTemplate;
    private final MappingUtil mappingUtil;

    @Value("${rabbitmq.queue.name}")
    private String messageQueue;

    @Autowired
    public RabbitMQMessageService(RabbitTemplate rabbitTemplate, MappingUtil mappingUtil) {
        this.rabbitTemplate = rabbitTemplate;
        this.mappingUtil = mappingUtil;
    }

    public DatabaseResponseDto sendMessage(DatabaseRequestDto databaseRequestDto) {

        return mappingUtil.mapJsonToDto((String) rabbitTemplate.convertSendAndReceive(messageQueue,
                mappingUtil.mapDtoToJson(databaseRequestDto)));
    }
}
