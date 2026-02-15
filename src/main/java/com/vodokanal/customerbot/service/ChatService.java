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

import java.math.BigDecimal;
import java.util.HashMap;
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
        } else if (currentUserState == UserState.ASKED_METER_NUMBER) {
            setMeterSerialNumber(message);
        } else if (currentUserState == UserState.ASKED_METER_VERIFICATION_DATE) {
            setMeterVerificationDate(message);
        } else if (currentUserState == UserState.ASKED_METER_INITIAL_VALUE) {
            setMeterInitialValue(message);
        }
    }

    private void verifyAccountNumber(Message message) {
        String accountNumber = message.getText();
        long chatID = message.getChatId();

        if (chatUtil.isAccountNumberFormatValid(accountNumber)) {
            Map<String, String> requestData = new HashMap<>();
            requestData.put("operation", Operation.BINDING_ID.getOperation());
            requestData.put("accountNumber", accountNumber);
            requestData.put("chatID", String.valueOf(chatID));

            String response = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));

            if (response.isEmpty()) {
                SendMessage newMessage = chatUtil.buildMessage(Constants.MESSAGE_ACCOUNT_BIND_FAIL, chatID);
                replyProducer.executeReply(newMessage);
            } else {
                SendMessage newMessage = chatUtil.buildMessage(Constants.MESSAGE_ACCOUNT_BIND_SUCCESS, chatID);
                replyProducer.executeReply(newMessage);
                sendMainMenu(accountNumber, response, chatID);
            }
        } else {
            SendMessage newMessage = chatUtil.buildMessage(Constants.MESSAGE_ACCOUNT_WRONG_FORMAT, chatID);
            replyProducer.executeReply(newMessage);
            askAccountNumber(chatID);
        }
    }

    private void setMeterSerialNumber(Message message) {
        String meterSerialNumber = message.getText();
        long chatID = message.getChatId();

        Meter meter = new Meter();
        meter.setSerialNumber(meterSerialNumber);
        userMeterData.put(chatID, meter);

        userStateMap.put(chatID, UserState.ASKED_METER_VERIFICATION_DATE);
        SendMessage newMessage = chatUtil.buildMessage(Constants.MESSAGE_ASK_METER_VERIFICATION_DATE, chatID);
        replyProducer.executeReply(newMessage);
    }

    private void setMeterVerificationDate(Message message) {
        String meterVerificationDate = message.getText();
        long chatID = message.getChatId();

        if (chatUtil.isDateFormatValid(meterVerificationDate)) {
            Meter meter = userMeterData.get(chatID);
            meter.setVerificationDate(meterVerificationDate);
            userMeterData.put(chatID, meter);

            userStateMap.put(chatID, UserState.ASKED_METER_INITIAL_VALUE);
            SendMessage newMessage = chatUtil.buildMessage(Constants.MESSAGE_ASK_METER_INITIAL_VALUE, chatID);
            replyProducer.executeReply(newMessage);
        } else {
            SendMessage newMessage = chatUtil.buildMessage(Constants.MESSAGE_DATA_WRONG_FORMAT, chatID);
            replyProducer.executeReply(newMessage);
        }
    }

    private void setMeterInitialValue(Message message) {
        String setValue = message.getText();
        long chatID = message.getChatId();
        BigDecimal meterInitialValue;

        try {
            meterInitialValue = new BigDecimal(setValue);
        } catch (NumberFormatException e) {
            SendMessage newMessage = chatUtil.buildMessage(Constants.MESSAGE_VALUE_WRONG_FORMAT, chatID);
            replyProducer.executeReply(newMessage);
            return;
        }

        Meter meter = userMeterData.get(chatID);
        meter.setInitialValue(meterInitialValue);
        userMeterData.put(chatID, meter);
        userStateMap.put(chatID, UserState.ASKED_METER_SERVICE);

        SendMessage newMessage = chatUtil.buildMessage(Constants.MESSAGE_ASK_METER_SERVICE, chatID);
        String[] buttonData = new String[]{
                Constants.MESSAGE_BUTTON_HOT_WATER, "hot_water",
                Constants.MESSAGE_BUTTON_COLD_WATER, "cold_water",
        };

        newMessage.setReplyMarkup(chatUtil.buildKeyboard(buttonData));
        replyProducer.executeReply(newMessage);
    }

    private void setMeterService(long chatID, int serviceID) {
        Meter meter = userMeterData.get(chatID);
        meter.setService(serviceID);
        userMeterData.put(chatID, meter);
        userStateMap.put(chatID, UserState.ASKED_CHEK);

        String messageText = Constants.MESSAGE_ASK_METER_SAVE.formatted(
                meter.getSerialNumber(),
                meter.getVerificationDate(),
                meter.getInitialValue(),
                meter.getService() == 1 ? Constants.MESSAGE_BUTTON_COLD_WATER :  Constants.MESSAGE_BUTTON_HOT_WATER
        );

        SendMessage newMessage = chatUtil.buildMessage(messageText, chatID);
        String[] buttonData = new String[]{
                Constants.MESSAGE_BUTTON_CHECK, "chek_meter",
                Constants.MESSAGE_BUTTON_RETURN, "return"
        };

        newMessage.setReplyMarkup(chatUtil.buildKeyboard(buttonData));
        replyProducer.executeReply(newMessage);
    }

    private void validateMeter(long chatID) {
        Meter meter = userMeterData.get(chatID);
        String meterExpirationDate = chatUtil.getMeterExpirationDate(meter);

        if (meterExpirationDate.isEmpty()) {
            SendMessage newMessage = chatUtil.buildMessage(Constants.MESSAGE_METER_CHECK_FAIL, chatID);
            replyProducer.executeReply(newMessage);
            start(chatID);
            return;
        }

        meter.setExpirationDate(meterExpirationDate);

        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.ADD_METER.getOperation());
        requestData.put("meter", mappingUtil.mapObjectToJson(meter));
        requestData.put("chatID", String.valueOf(chatID));

        String response = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));
        String messageText;

        if (response.isEmpty()) {
            messageText = Constants.MESSAGE_METER_CHECK_EXIST;
        } else {
            messageText = Constants.MESSAGE_METER_CHECK_SUCCESS;
        }

        SendMessage newMessage = chatUtil.buildMessage(messageText, chatID);
        replyProducer.executeReply(newMessage);
        start(chatID);
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

            sendMainMenu(accountNumber, balance, chatID);
        }
    }

    private void sendMainMenu(String accountNumber, String balance, long chatID) {
        String messageText = Constants.MESSAGE_MENU.formatted(accountNumber, balance);
        SendMessage message = chatUtil.buildMessage(messageText, chatID);

        String[] buttonData = new String[]{
                Constants.MESSAGE_BUTTON_MY_METERS, "my_meters",
                Constants.MESSAGE_BUTTON_ADD_METER, "add_meter",
                Constants.MESSAGE_BUTTON_SEND_READING, "send_reading"
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
        } else if (data.equals("add_meter")) {
            askMeterNumber(chatID);
        } else if (data.equals("hot_water")) {
            setMeterService(chatID, Constants.METER_HOT_WATER_ID);
        } else if (data.equals("cold_water")) {
            setMeterService(chatID, Constants.METER_COLD_WATER_ID);
        } else if (data.equals("return")) {
            start(chatID);
        } else if (data.equals("chek_meter")) {
            validateMeter(chatID);
        }
    }

    private void askMeterNumber(long chatID) {
        userStateMap.put(chatID, UserState.ASKED_METER_NUMBER);

        SendMessage message = chatUtil.buildMessage(Constants.MESSAGE_ASK_METER_NUMBER, chatID);
        replyProducer.executeReply(message);
    }

    private void askAccountNumber(long chatID) {
        userStateMap.put(chatID, UserState.ASKED_ACCOUNT);

        SendMessage message = chatUtil.buildMessage(Constants.MESSAGE_ASK_ACCOUNT, chatID);
        replyProducer.executeReply(message);
    }
}
