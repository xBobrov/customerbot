package com.vodokanal.customerbot.service;

import com.vodokanal.customerbot.util.ChatUtil;
import com.vodokanal.customerbot.util.Constants;
import com.vodokanal.customerbot.model.Meter;
import com.vodokanal.customerbot.enums.Operation;
import com.vodokanal.customerbot.enums.UserState;
import com.vodokanal.customerbot.util.MappingUtil;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {
    private final RabbitMQMessageService rabbitMQMessageService;
    private final ReplyProducer replyProducer;
    private final MappingUtil mappingUtil;
    private final ChatUtil chatUtil;

    private final Map<Long, UserState> userStateMap = new ConcurrentHashMap<>();
    private final Map<Long, Meter> userMeterData = new ConcurrentHashMap<>();

    public ChatService(RabbitMQMessageService rabbitMQMessageService, ReplyProducer replyProducer,
                       MappingUtil mappingUtil, ChatUtil chatUtil) {
        this.rabbitMQMessageService = rabbitMQMessageService;
        this.replyProducer = replyProducer;
        this.mappingUtil = mappingUtil;
        this.chatUtil = chatUtil;
    }

    public void handleMessage(Message message) {
        long chatID = message.getChatId();
        UserState currentUserState = userStateMap.get(chatID);

        if (message.getText().equals("/start")) {
            start(chatID);
        } else if (currentUserState == UserState.ASKED_ACCOUNT) {
            verifyAccountNumber(message);
        }
    }

    private void verifyAccountNumber(Message message) {
        String accountNumber = message.getText();
        long chatID = message.getChatId();

        if (chatUtil.isAccountNumberFormatValid(accountNumber)) {
            Map<String, String> requestData = new HashMap<>();
            requestData.put("operation", Operation.BIND_ID.getOperation());
            requestData.put("accountNumber", accountNumber);
            requestData.put("chatID", String.valueOf(chatID));

            String response = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));

            if (response.isEmpty()) {
                SendMessage newMessage = chatUtil.buildMessage(Constants.MESSAGE_ACCOUNT_BIND_FAIL, chatID);
                replyProducer.executeReply(newMessage);
            } else {
                SendMessage newMessage = chatUtil.buildMessage(Constants.MESSAGE_ACCOUNT_BIND_SUCCESS, chatID);
                replyProducer.executeReply(newMessage);

                Map<String, String> responseMap = mappingUtil.mapJsonToHashMap(response);
                String balance = responseMap.get("balance");
                String email = responseMap.get("email");

                sendMainMenu(accountNumber, balance, email, chatID);
            }
        } else {
            SendMessage newMessage = chatUtil.buildMessage(Constants.MESSAGE_ACCOUNT_WRONG_FORMAT, chatID);
            replyProducer.executeReply(newMessage);
            askAccountNumber(chatID);
        }
    }

    private void start(long chatID) {
        userStateMap.put(chatID, UserState.START);

        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.START.getOperation());
        requestData.put("chatID", String.valueOf(chatID));

        String response = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));

        if (response.isEmpty()) {
            register(chatID);
        } else {
            Map<String, String> responseMap = mappingUtil.mapJsonToHashMap(response);
            String accountNumber = responseMap.get("accountNumber");
            String balance = responseMap.get("balance");
            String email = responseMap.get("email");

            sendMainMenu(accountNumber, balance, email, chatID);
        }
    }

    private void sendMainMenu(String accountNumber, String balance, String email, long chatID) {
        String messageText = Constants.MESSAGE_MENU.formatted(accountNumber, balance, email);
        SendMessage message = chatUtil.buildMessage(messageText, chatID);

        String[] buttonData = new String[]{
                Constants.MESSAGE_BUTTON_MY_METERS, "my_meters",
                Constants.MESSAGE_BUTTON_SEND_READING, "send_reading",
                email.isEmpty() ? Constants.MESSAGE_BUTTON_EMAIL_ADD : Constants.MESSAGE_BUTTON_EMAIL_CHANGE,
                email.isEmpty() ? "add_email" : "change_email"
        };

        message.setReplyMarkup(chatUtil.buildKeyboard(buttonData));
        replyProducer.executeReply(message);
    }

    private void register(long chatID) {
        SendMessage message = chatUtil.buildMessage(Constants.MESSAGE_WELCOME, chatID);

        String[] buttonData = new String[]{Constants.MESSAGE_BUTTON_BIND_ID, "bind_id"};
        message.setReplyMarkup(chatUtil.buildKeyboard(buttonData));

        replyProducer.executeReply(message);
    }

    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatID = callbackQuery.getMessage().getChatId();

        if (data.equals("bind_id")) {
            askAccountNumber(chatID);
        } else if (data.equals("return")) {
            start(chatID);
        } else if (data.equals("my_meters")) {
            displayMeters(chatID);
        }
    }

    private void displayMeters(long chatID) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.METER_INFO.getOperation());
        requestData.put("chatID", String.valueOf(chatID));

        String response = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));
        List<HashMap<String,String>> meterDataList = mappingUtil.mapJsonToHashMapList(response);

        for (HashMap<String,String> meterData : meterDataList) {
            String serialNumber = meterData.get("number");
            String service =  meterData.get("service");
            String validThru = meterData.get("valid");
            String lastReading = meterData.get("last_reading");

            SendMessage message = chatUtil.buildMessage(Constants.MESSAGE_METER_DATA.formatted(
                    serialNumber,
                    service,
                    lastReading,
                    validThru
            ), chatID);
            replyProducer.executeReply(message);
        }
    }

    private void askAccountNumber(long chatID) {
        userStateMap.put(chatID, UserState.ASKED_ACCOUNT);

        SendMessage message = chatUtil.buildMessage(Constants.MESSAGE_ASK_ACCOUNT, chatID);
        replyProducer.executeReply(message);
    }
}
