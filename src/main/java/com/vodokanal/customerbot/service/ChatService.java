package com.vodokanal.customerbot.service;

import com.vodokanal.customerbot.util.ChatUtil;
import com.vodokanal.customerbot.util.Constants;
import com.vodokanal.customerbot.enums.Operation;
import com.vodokanal.customerbot.enums.UserState;
import com.vodokanal.customerbot.util.MappingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {
    private final RabbitMQMessageService rabbitMQMessageService;
    private final ReplyProducer replyProducer;
    private final MappingUtil mappingUtil;
    private final ChatUtil chatUtil;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    public ChatService(RabbitMQMessageService rabbitMQMessageService, ReplyProducer replyProducer,
                       MappingUtil mappingUtil, ChatUtil chatUtil, UserService userService) {
        this.rabbitMQMessageService = rabbitMQMessageService;
        this.replyProducer = replyProducer;
        this.mappingUtil = mappingUtil;
        this.chatUtil = chatUtil;
        this.userService = userService;
    }

    public void handleMessage(Message message) {
        long chatID = message.getChatId();

        if (message.getText().equals("/start")) {
            logger.info("User {} triggered /start command", chatID);
            start(chatID);

            return;
        } else if (message.getText().equals("/test")) {
            logger.info("User {} triggered /test command", chatID);
            sendMessage(chatID, "Kurlyk!!");
        }

        if (userService.isUserExist(chatID)) {
            UserState currentUserState = userService.getUserState(chatID);

            logger.info("User {} sent message: {}. User state: {}", chatID, message.getText(), currentUserState);

            switch (currentUserState) {
                case UserState.ASKED_ACCOUNT_NUMBER -> verifyAccountNumber(message);
                case UserState.ASKED_EMAIL -> changeEmail(message);
                case UserState.ASKED_METER_NUMBER -> verifyMeterNumber(message);
                case UserState.ASKED_METER_CURRENT_READING -> verifyCurrentReading(message);
            }
        }
    }

    private void verifyCurrentReading(Message message) {
        String currentReadingText = message.getText();
        long chatID = message.getChatId();

        if (!chatUtil.isMeterValueFormatValid(currentReadingText)) {
            sendMessage(chatID, Constants.MESSAGE_VALUE_WRONG_FORMAT);
            askCurrentReading(chatID);
            return;
        }

        BigDecimal currentReading = new BigDecimal(currentReadingText);
        BigDecimal lastReading = new BigDecimal(userService.getLastReading(chatID));
        BigDecimal consumption = currentReading.subtract(lastReading);

        if (!chatUtil.isCurrentValueValid(currentReading, lastReading)) {
            sendMessage(chatID, Constants.MESSAGE_VALUE_INVALID);
            askCurrentReading(chatID);
            return;
        }

        userService.setCurrentReading(chatID, currentReadingText);
        userService.setConsumption(chatID, consumption.toPlainString());

        String[] buttonData = new String[]{Constants.MESSAGE_BUTTON_APPROVE, "approve_reading"};
        sendMessageKeyboarded(chatID, Constants.MESSAGE_READING_CONSUMPTION.formatted(consumption), buttonData);
    }

    private void verifyMeterNumber(Message message) {
        String meterNumber = message.getText();
        long chatID = message.getChatId();

        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.METER_VALIDATION.getOperation());
        requestData.put("meterNumber", meterNumber);
        requestData.put("chatID", String.valueOf(chatID));

        String response = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));

        if (response == null || response.isEmpty()) {
            sendMessage(chatID, Constants.MESSAGE_METER_NOT_FOUND);
            askMeterNumber(chatID);
            return;
        }

        Map<String, String> responseMap = mappingUtil.mapJsonToHashMap(response);
        String serialNumber = responseMap.get("number");
        String service = responseMap.get("service");
        String lastReading = responseMap.get("lastReading");
        String validThru = responseMap.get("valid");

        String messageText = Constants.MESSAGE_METER_DATA.formatted(serialNumber, service, lastReading, validThru);
        sendMessage(chatID, messageText);

        userService.setMeterNumber(chatID, meterNumber);
        userService.setLastReading(chatID, lastReading);
        askCurrentReading(chatID);
    }

    private void askCurrentReading(long chatID) {
        userService.setState(chatID, UserState.ASKED_METER_CURRENT_READING);
        sendMessage(chatID, Constants.MESSAGE_ASK_CURRENT_VALUE);
    }

    private void changeEmail(Message message) {
        String email = message.getText();
        long chatID = message.getChatId();

        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.EMAIL_INFO.getOperation());
        requestData.put("chatID", String.valueOf(chatID));

        String savedEmail = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));

        if (savedEmail == null || savedEmail.isEmpty() && email.equals("0")) {
            sendMessage(chatID, Constants.MESSAGE_EMAIL_UNABLE_UNLINKED);

        } else if (!email.equals("0") && !chatUtil.isEmailFormatValid(email)) {
            sendMessage(chatID, Constants.MESSAGE_EMAIL_WRONG_FORMAT);
            askEmail(chatID);

        } else {
            requestData = new HashMap<>();
            requestData.put("operation", Operation.CHANGE_EMAIL.getOperation());
            requestData.put("email", email);
            requestData.put("chatID", String.valueOf(chatID));

            String response = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));

            if (response.isEmpty()) {
                sendMessage(chatID, Constants.MESSAGE_EMAIL_UNLINKED);
            } else {
                sendMessage(chatID, Constants.MESSAGE_EMAIL_LINKED.formatted(response));
            }

            userService.setState(chatID, UserState.START);
        }
    }

    private void sendMessage(long chatID, String messageText) {
        SendMessage newMessage = chatUtil.buildMessage(messageText, chatID);
        replyProducer.executeReply(newMessage);
    }

    private void sendMessageKeyboarded(long chatID, String messageText, String[] buttonData) {
        SendMessage newMessage = chatUtil.buildMessage(messageText, chatID);
        newMessage.setReplyMarkup(chatUtil.buildKeyboard(buttonData));
        replyProducer.executeReply(newMessage);
    }

    private void verifyAccountNumber(Message message) {
        String accountNumber = message.getText();
        long chatID = message.getChatId();

        if (!chatUtil.isAccountNumberFormatValid(accountNumber)) {
            sendMessage(chatID, Constants.MESSAGE_ACCOUNT_WRONG_FORMAT);
            askAccountNumber(chatID);

        } else {
            Map<String, String> requestData = new HashMap<>();
            requestData.put("operation", Operation.BIND_ID.getOperation());
            requestData.put("accountNumber", accountNumber);
            requestData.put("chatID", String.valueOf(chatID));

            String response = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));

            if (response.isEmpty()) {
                sendMessage(chatID, Constants.MESSAGE_ACCOUNT_BIND_FAIL);

            } else {
                sendMessage(chatID, Constants.MESSAGE_ACCOUNT_BIND_SUCCESS);

                Map<String, String> responseMap = mappingUtil.mapJsonToHashMap(response);
                String balance = responseMap.get("balance");
                String email = responseMap.get("email");

                userService.setEmail(chatID, email);  //? проверить на ошибки при перезагрузке сервера
                sendMainMenu(accountNumber, balance, email, chatID);
            }
        }
    }

    private void start(long chatID) {
        userService.setState(chatID, UserState.START);

        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.START.getOperation());
        requestData.put("chatID", String.valueOf(chatID));

        String response = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));

        if (response.isEmpty()) {
            register(chatID);

        } else {
            Map<String, String> responseMap = mappingUtil.mapJsonToHashMap(response);
            String accountNumber = responseMap.get("number");
            String balance = responseMap.get("balance");
            String email = responseMap.get("email");

            userService.setEmail(chatID, email);
            sendMainMenu(accountNumber, balance, email, chatID);
        }
    }

    private void sendMainMenu(String accountNumber, String balance, String email, long chatID) {
        if (email.isEmpty()) {
            email = Constants.MESSAGE_EMAIL_NOT_LINKED;
        }

        String messageText = Constants.MESSAGE_MENU.formatted(accountNumber, balance, email);
        String[] buttonData = new String[]{
                Constants.MESSAGE_BUTTON_MY_METERS, "my_meters",
                Constants.MESSAGE_BUTTON_SEND_READING, "send_reading",
                Constants.MESSAGE_BUTTON_EMAIL_CHANGE, "change_email"
        };

        sendMessageKeyboarded(chatID, messageText, buttonData);
    }

    private void register(long chatID) {
        String[] buttonData = new String[]{Constants.MESSAGE_BUTTON_BIND_ID, "bind_id"};
        sendMessageKeyboarded(chatID, Constants.MESSAGE_WELCOME, buttonData);
    }

    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatID = callbackQuery.getMessage().getChatId();

        logger.info("User {} triggered {} command", chatID, data);

        switch (data) {
            case "bind_id" -> askAccountNumber(chatID);
            case "return" -> start(chatID);
            case "my_meters" -> displayMeters(chatID);
            case "send_reading" -> checkDate(chatID);
            case "change_email" -> askEmail(chatID);
            case "approve_reading" -> sendReading(chatID);
        }
    }

    private void sendReading(long chatID) {
        if (!userService.isUserExist(chatID)) {
            askRestart(chatID);
            return;
        }

        String meterNumber = userService.getMeterNumber(chatID);
        String currentReading = userService.getCurrentReading(chatID);
        String consumption = userService.getConsumption(chatID);

        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.READING_TRANSMIT.getOperation());
        requestData.put("chatID", String.valueOf(chatID));
        requestData.put("meterNumber", meterNumber);
        requestData.put("currentReading", currentReading);
        requestData.put("consumption", consumption);

        rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));

        sendMessage(chatID, Constants.MESSAGE_READING_ACCEPTED);
        userService.setState(chatID, UserState.START);
    }

    private void checkDate(long chatID) {
        if (!chatUtil.isDateValid()) {
            sendMessage(chatID, Constants.MESSAGE_READING_WRONG_DATE);
            userService.setState(chatID, UserState.START);
            return;
        }

        askMeterNumber(chatID);
    }

    private void askMeterNumber(long chatID) {
        if (!userService.isUserExist(chatID)) {
            askRestart(chatID);
            return;
        }

        userService.setState(chatID, UserState.ASKED_METER_NUMBER);
        sendMessage(chatID, Constants.MESSAGE_ASK_METER_NUMBER);
    }

    private void askEmail(long chatID) {
        if (!userService.isUserExist(chatID)) {
            askRestart(chatID);
            return;
        }

        userService.setState(chatID, UserState.ASKED_EMAIL);
        sendMessage(chatID, Constants.MESSAGE_ASK_EMAIL);
    }

    private void askRestart(long chatID) {
        sendMessage(chatID, Constants.MESSAGE_RESTART);
    }

    private void displayMeters(long chatID) {
        if (!userService.isUserExist(chatID)) {
            askRestart(chatID);
            return;
        }

        userService.setState(chatID, UserState.START);

        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.METER_INFO.getOperation());
        requestData.put("chatID", String.valueOf(chatID));

        String response = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));
        List<HashMap<String, String>> meterDataList = mappingUtil.mapJsonToHashMapList(response);

        for (HashMap<String, String> meterData : meterDataList) {
            String serialNumber = meterData.get("number");
            String service = meterData.get("service");
            String validThru = meterData.get("valid");
            String lastReading = meterData.get("lastReading");

            String messageText = Constants.MESSAGE_METER_DATA.formatted(serialNumber, service, lastReading, validThru);
            sendMessage(chatID, messageText);
        }
    }

    private void askAccountNumber(long chatID) {

        userService.setState(chatID, UserState.ASKED_ACCOUNT_NUMBER);
        sendMessage(chatID, Constants.MESSAGE_ASK_ACCOUNT);
    }
}
