package com.vodokanal.customerbot.dto;

import com.vodokanal.customerbot.enums.Operation;

public record DatabaseRequestDto(
        long chatID,
        String data,
        Operation operation
) {}
