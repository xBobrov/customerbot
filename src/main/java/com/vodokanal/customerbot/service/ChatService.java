package com.vodokanal.customerbot.service;

import com.vodokanal.customerbot.dto.DatabaseRequestDto;
import com.vodokanal.customerbot.dto.DatabaseResponseDto;
import com.vodokanal.customerbot.enums.Operation;
import com.vodokanal.customerbot.enums.UserState;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Service
public class ChatService {
    private final RabbitMQMessageService rabbitMQMessageService;
    private final ReplyProducer replyProducer;
    private final Map<Long, UserState> userStateMap = new ConcurrentHashMap<>();

    private final String MESSAGE_WELCOME = "Добро пожаловать в телеграм бот АО \"Водоканал\"";
    private final String MESSAGE_WRONG_ACCOUNT_FORMAT = "Введеный Вами номер не соответствует формату";
    private final String MESSAGE_ACCOUNT_BIND_SUCCESS = "Ваш телеграм аккаунт успешно привязан к лицевому счету";
    private final String MESSAGE_ACCOUNT_BIND_FAIL = "Лицевого счета с таким номером не обнаружено, обратитесь в абонентский отдел АО \"Водоканал\"";
    private final String MESSAGE_ASK_ACCOUNT = "Введите номер Вашего лицевого счета.\n Номер должен иметь формат: 0000-000-0";
    private final String MESSAGE_BUTTON_SIGNUP = "Привязать аккаунт к лицевому счету";

    public ChatService(RabbitMQMessageService rabbitMQMessageService, ReplyProducer replyProducer) {
        this.rabbitMQMessageService = rabbitMQMessageService;
        this.replyProducer = replyProducer;
    }

    public void handleMessage(Message message) {
        long chatID = message.getChatId();
        UserState currentUserState = userStateMap.get(chatID);

        if (message.getText().equals("/start")) {
            userStateMap.put(chatID, UserState.START);
            DatabaseResponseDto databaseResponseDto = requestDb(chatID, "", Operation.SIGNIN);

            if ((databaseResponseDto.reply()).isEmpty()) {
                prepareSignUpReply(chatID);
            } else {

            }


        } else if (currentUserState == UserState.ASKED_ACCOUNT) {
            String accountNumber = message.getText();
            if (validateAccountNumber(accountNumber)) {
                if (Boolean.parseBoolean(requestDb(chatID, accountNumber, Operation.SIGNUP).reply())) {
                    SendMessage newMessage = buildMessage(MESSAGE_ACCOUNT_BIND_SUCCESS, chatID);
                    replyProducer.executeReply(newMessage);
                    askAccountNumber(chatID);
                } else {
                    SendMessage newMessage = buildMessage(MESSAGE_ACCOUNT_BIND_FAIL, chatID);
                    replyProducer.executeReply(newMessage);
                }
            } else {
                SendMessage newMessage = buildMessage(MESSAGE_WRONG_ACCOUNT_FORMAT, chatID);
                replyProducer.executeReply(newMessage);
                askAccountNumber(chatID);
            }
        }
    }

    private DatabaseResponseDto requestDb(long chatID, String data, Operation operation) {
        DatabaseRequestDto databaseRequestDto = new DatabaseRequestDto(chatID, data, operation);

        return rabbitMQMessageService.sendMessage(databaseRequestDto);
    }

    private void prepareSignUpReply(long chatID) {
        SendMessage message = buildMessage(MESSAGE_WELCOME, chatID);

        String[] buttonData = new String[]{MESSAGE_BUTTON_SIGNUP, "signup"};
        message.setReplyMarkup(buildKeyboard(buttonData));

        replyProducer.executeReply(message);
    }

    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatID = callbackQuery.getMessage().getChatId();

        switch (data) {
            case "signup" -> askAccountNumber(chatID);
            default -> {
            }
        }
    }

    private void askAccountNumber(long chatID) {
        userStateMap.put(chatID, UserState.ASKED_ACCOUNT);

        SendMessage message = buildMessage(MESSAGE_ASK_ACCOUNT, chatID);
        replyProducer.executeReply(message);
    }

    private SendMessage buildMessage(String text, long chatID) {

        return SendMessage.builder()
                .text(text)
                .chatId(chatID)
                .build();
    }

    private InlineKeyboardButton buildButton(String text, String callbackData) {

        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    private InlineKeyboardMarkup buildKeyboard(String[] buttonData) {
        List<InlineKeyboardRow> keyboardRowList = IntStream.iterate(0, i -> i < buttonData.length, i -> i + 2)
                .mapToObj(i -> new InlineKeyboardRow(buildButton(buttonData[i], buttonData[i + 1])))
                .toList();

        return new InlineKeyboardMarkup(keyboardRowList);
    }

    private boolean validateAccountNumber(String accountNumber) {
        return accountNumber.matches("\\d{4}-\\d{3}-\\d");
    }
}
