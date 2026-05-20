package com.vodokanal.customerbot.service;

import com.vodokanal.customerbot.model.User;
import com.vodokanal.customerbot.util.ChatUtil;
import com.vodokanal.customerbot.util.Constants;
import com.vodokanal.customerbot.enums.UserState;

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

/**
 * The core service for managing dialogue logic and user
 * states (Orchestrator).
 * <p>
 * The class manages user state transitions: from account
 * registration to meter reading submission.
 * </p>
 * <p>
 * Coordinates work of {@link UserService} for state storage
 * and {@link IntegrationService} for data exchange with the
 * external accounting system.
 * </p>
 */
@Service
public class ChatService {
    private final IntegrationService integrationService;
    private final ReplyProducer replyProducer;
    private final ChatUtil chatUtil;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    public ChatService(IntegrationService integrationService, ReplyProducer replyProducer,
                       ChatUtil chatUtil, UserService userService) {
        this.integrationService = integrationService;
        this.replyProducer = replyProducer;
        this.chatUtil = chatUtil;
        this.userService = userService;
    }

    /**
     * Entry point for processing all incoming text messages.
     * <p>
     * Handles global commands (e.g., {@code /start}) and routes user input
     * based on their current {@link UserState} received from {@link UserService}.
     * </p>
     *
     * @param message the incoming Telegram message object.
     */
    public void handleMessage(Message message) {
        long chatID = message.getChatId();

        if (message.getText().equals("/start")) {
            logger.info("User {} triggered /start command", chatID);
            start(chatID);

            return;
        }

        if (userService.isUserExist(chatID)) {
            User user = userService.findOrCreateUser(chatID);
            UserState currentUserState = user.getUserState();

            logger.info("User {} sent message: {}. User state: {}", chatID, message.getText(), currentUserState);

            switch (currentUserState) {
                case UserState.ASKED_ACCOUNT_NUMBER -> verifyAccountNumber(message, user);
                case UserState.ASKED_EMAIL -> changeEmail(message, user);
                case UserState.ASKED_METER_NUMBER -> verifyMeterNumber(message, user);
                case UserState.ASKED_METER_CURRENT_READING -> verifyCurrentReading(message, user);
            }
        }
    }

    /**
     * Validates meter readings and calculates consumption.
     * <p>
     * The method performs the following checks:
     * <ul>
     *     <li>Readings format correctness.</li>
     *     <li>Logical validity (the current reading cannot be less than the previous one).</li>
     * </ul>
     * Upon successful validation, calculates the difference using {@code BigDecimal}
     * and prepares the data for confirmation.
     * </p>
     *
     * @param message meter reading string.
     * @param user    current user object.
     */
    private void verifyCurrentReading(Message message, User user) {
        String currentReadingText = message.getText();
        long chatID = message.getChatId();

        if (!chatUtil.isMeterValueFormatValid(currentReadingText)) {
            logger.warn("User {} entered invalid format reading: {}", chatID, currentReadingText);
            sendMessage(chatID, Constants.MESSAGE_VALUE_WRONG_FORMAT);
            askCurrentReading(chatID, user);

            return;
        }

        BigDecimal currentReading = new BigDecimal(currentReadingText);
        BigDecimal lastReading = new BigDecimal(user.getLastReadingValue());
        BigDecimal consumption = currentReading.subtract(lastReading);

        if (!chatUtil.isCurrentValueValid(currentReading, lastReading)) {
            logger.warn("User {} entered invalid reading: {} (previous was {})", chatID, currentReading, lastReading);
            sendMessage(chatID, Constants.MESSAGE_VALUE_INVALID);
            askCurrentReading(chatID, user);

            return;
        }

        user.setCurrentReadingValue(currentReadingText);
        user.setConsumption(consumption.toPlainString());

        String[] buttonData = new String[]{Constants.MESSAGE_BUTTON_APPROVE, "approve_reading"};
        sendMessageKeyboarded(chatID, Constants.MESSAGE_READING_CONSUMPTION.formatted(consumption), buttonData);
    }

    /**
     * Provides meter number validation.
     * <p>
     * Checks if user-provided meter number is bound to user's account.
     * In case of success shows meter information and proceeds to
     * current reading value transmission.
     *
     * @param message message containing the meter serial number.
     * @param user    current user object.
     */
    private void verifyMeterNumber(Message message, User user) {
        String meterNumber = message.getText();
        long chatID = message.getChatId();

        Map<String, String> responseMap = integrationService.provideMeterValidation(meterNumber, chatID);

        if (responseMap.isEmpty()) {
            logger.warn("Meter validation failed for user {}: meter {} not found", chatID, meterNumber);
            sendMessage(chatID, Constants.MESSAGE_METER_NOT_FOUND);
            askMeterNumber(chatID, user);

            return;
        }

        logger.info("Meter validation success for user {}: meter service is {}", chatID, responseMap.get("service"));

        String serialNumber = responseMap.get("number");
        String service = responseMap.get("service");
        String lastReading = responseMap.get("lastReading");
        String validThru = responseMap.get("valid");

        String messageText = Constants.MESSAGE_METER_DATA.formatted(serialNumber, service, lastReading, validThru);
        sendMessage(chatID, messageText);

        user.setMeterNumber(meterNumber);
        user.setLastReadingValue(lastReading);

        askCurrentReading(chatID, user);
    }

    /**
     * Ask user for current value of metering devices and set relevant user state.
     *
     * @param chatID Telegram chat ID.
     * @param user    the current user object.
     */
    private void askCurrentReading(long chatID, User user) {
        user.setUserState(UserState.ASKED_METER_CURRENT_READING);
        sendMessage(chatID, Constants.MESSAGE_ASK_CURRENT_VALUE);
    }

    /**
     * Provide updating user's Email address.
     * <p>
     * This method validates the provided input and coordinates with the {@link IntegrationService}
     * to persist changes. It supports:
     * <ul>
     *     <li>Email address format correctness</li>
     *     <li>Unlinking the current email if sent address string is "0".</li>
     *     <li>Error if changing is impossible or address format is invalid.</li>
     * </ul>
     * </p>
     *
     * @param message message containing the new email address.
     * @param user    the current user object.
     */
    private void changeEmail(Message message, User user) {
        String email = message.getText();
        long chatID = message.getChatId();

        String savedEmail = integrationService.provideEmailInfo(chatID);

        if (savedEmail == null || savedEmail.isEmpty() && email.equals("0")) {
            logger.warn("Changing email failed for user {}, email is empty", chatID);
            sendMessage(chatID, Constants.MESSAGE_EMAIL_UNABLE_UNLINKED);

        } else if (!email.equals("0") && !chatUtil.isEmailFormatValid(email)) {
            logger.warn("Changing email failed for user {}, email is invalid", chatID);
            sendMessage(chatID, Constants.MESSAGE_EMAIL_WRONG_FORMAT);
            askEmail(chatID, user);

        } else {
            String response = integrationService.provideEmailChanging(email, chatID);

            if (response.isEmpty()) {
                logger.info("For user {} email is unlinked", chatID);
                sendMessage(chatID, Constants.MESSAGE_EMAIL_UNLINKED);
            } else {
                logger.info("For user {} email changed for {}", chatID, email);
                sendMessage(chatID, Constants.MESSAGE_EMAIL_LINKED.formatted(response));
            }

            user.setUserState(UserState.START);
        }
    }
    /**
     * Sends a text message to the chat.
     *
     * @param chatID Telegram chat ID.
     * @param messageText message to be sent.
     */
    private void sendMessage(long chatID, String messageText) {
        SendMessage newMessage = chatUtil.buildMessage(messageText, chatID);
        replyProducer.executeReply(newMessage);
    }

    /**
     * Sends a message with an inline keyboard.
     *
     * @param chatID Telegram chat ID.
     * @param messageText message to be sent.
     * @param buttonData  array of button labels and callback data.
     */
    private void sendMessageKeyboarded(long chatID, String messageText, String[] buttonData) {
        SendMessage newMessage = chatUtil.buildMessage(messageText, chatID);
        newMessage.setReplyMarkup(chatUtil.buildKeyboard(buttonData));
        replyProducer.executeReply(newMessage);
    }

    /**
     * Verifies account number and binds Telegram ID to it.
     *
     * @param message message containing the account number.
     * @param user    the current user object.
     */
    private void verifyAccountNumber(Message message, User user) {
        String accountNumber = message.getText();
        long chatID = message.getChatId();

        if (!chatUtil.isAccountNumberFormatValid(accountNumber)) {
            logger.warn("User {} entered wrong format account number: {}", chatID, accountNumber);
            sendMessage(chatID, Constants.MESSAGE_ACCOUNT_WRONG_FORMAT);
            askAccountNumber(chatID, user);

        } else {
            Map<String, String> accountData = integrationService.provideBindingID(accountNumber, chatID);

            if (accountData.isEmpty()) {
                logger.warn("User {} entered non-existent account number: {}", chatID, accountNumber);
                sendMessage(chatID, Constants.MESSAGE_ACCOUNT_BIND_FAIL);
            } else {
                sendMessage(chatID, Constants.MESSAGE_ACCOUNT_BIND_SUCCESS);
                String email = accountData.get("email");
                user.setEmail(email);
                sendMainMenu(accountNumber, email, chatID);
            }
        }
    }

    /**
     * Initializes the user session upon the {@code /start} command.
     * <p>
     * This method resets the user's state to {@link UserState#START} and synchronizes
     * account data with the billing system. If Telegram ID is not bound
     * to any account, it proceeds to the registration step. Otherwise, it
     * displays the main menu.
     * </p>
     *
     * @param chatID Telegram chat ID.
     */
    private void start(long chatID) {
        User user = userService.findOrCreateUser(chatID);
        user.setUserState(UserState.START);

        Map<String, String> accountData = integrationService.provideStarting(chatID);

        if (accountData.isEmpty()) {
            register(chatID);
        } else {
            String accountNumber = accountData.get("number");
            String email = accountData.get("email");

            user.setEmail(email);
            sendMainMenu(accountNumber, email, chatID);
        }
    }

    /**
     * Displays the main menu.
     * <p>
     * It initializes an inline keyboard with available commands:
     * meter list, reading submission, and email settings.
     * </p>
     *
     * @param accountNumber account number.
     * @param email         linked email address.
     * @param chatID Telegram chat ID.
     */
    private void sendMainMenu(String accountNumber, String email, long chatID) {
        if (email.isEmpty()) {
            email = Constants.MESSAGE_EMAIL_NOT_LINKED;
        }

        String messageText = Constants.MESSAGE_MENU.formatted(accountNumber, email);
        String[] buttonData = new String[]{
                Constants.MESSAGE_BUTTON_MY_METERS, "my_meters",
                Constants.MESSAGE_BUTTON_SEND_READING, "send_reading",
                Constants.MESSAGE_BUTTON_EMAIL_CHANGE, "change_email"
        };

        sendMessageKeyboarded(chatID, messageText, buttonData);
    }

    /**
     * Initiates the registration process for new users.
     * <p>
     * Displays a welcome message and provides a registration
     * button
     * </p>
     *
     * @param chatID Telegram chat ID.
     */
    private void register(long chatID) {
        String[] buttonData = new String[]{Constants.MESSAGE_BUTTON_BIND_ID, "bind_id"};
        sendMessageKeyboarded(chatID, Constants.MESSAGE_WELCOME, buttonData);
    }

    /**
     * Processes button clicks (callback queries).
     * <p>
     * Switches the dialogue context based on the action selected by the user
     * (account binding, reading transmission, or meter list viewing).
     * </p>
     *
     * @param callbackQuery the callback query event object from Telegram API.
     */
    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatID = callbackQuery.getMessage().getChatId();
        User user = userService.findOrCreateUser(chatID);

        logger.info("User {} triggered {} command", chatID, data);

        switch (data) {
            case "bind_id" -> askAccountNumber(chatID, user);
            case "return" -> start(chatID);
            case "my_meters" -> displayMeters(chatID, user);
            case "send_reading" -> checkDate(chatID, user);
            case "change_email" -> askEmail(chatID, user);
            case "approve_reading" -> sendReading(chatID, user);
        }
    }

    /**
     * Performs transmission of meter readings to the database.
     * <p>
     * Triggered after the user clicks the confirmation button.
     * </p>
     *
     * @param chatID Telegram chat ID.
     * @param user    the current user object.
     */
    private void sendReading(long chatID, User user) {
        if (!userService.isUserExist(chatID)) {
            askRestart(chatID);
            return;
        }

        String dbRowsInserted = integrationService.provideReadingSubmission(chatID, user);

        if (dbRowsInserted.equals("0")) {
            sendMessage(chatID, Constants.MESSAGE_READING_DENIED);
        } else {
            sendMessage(chatID, Constants.MESSAGE_READING_ACCEPTED);
        }

        user.setUserState(UserState.START);
    }

    /**
     * Checks if current date is in regulated period for reading acceptance.
     *
     * @param chatID Telegram chat ID.
     * @param user    the current user object.
     */
    private void checkDate(long chatID, User user) {
        if (!chatUtil.isDateValid()) {
            sendMessage(chatID, Constants.MESSAGE_READING_WRONG_DATE);
            user.setUserState(UserState.START);
            return;
        }

        askMeterNumber(chatID, user);
    }

    /**
     * Ask user for meter serial number and set relevant user state.
     *
     * @param chatID Telegram chat ID.
     * @param user    the current user object.
     */
    private void askMeterNumber(long chatID, User user) {
        if (!userService.isUserExist(chatID)) {
            askRestart(chatID);
            return;
        }

        user.setUserState(UserState.ASKED_METER_NUMBER);
        sendMessage(chatID, Constants.MESSAGE_ASK_METER_NUMBER);
    }


    /**
     * Ask user for Email address and set relevant user state.
     *
     * @param chatID Telegram chat ID.
     * @param user    the current user object.
     */
    private void askEmail(long chatID, User user) {
        if (!userService.isUserExist(chatID)) {
            askRestart(chatID);
            return;
        }

        user.setUserState(UserState.ASKED_EMAIL);
        sendMessage(chatID, Constants.MESSAGE_ASK_EMAIL);
    }

    /**
     * Informs user of impossibility to perform requested
     * action and suggests triggering {@code /start} command.
     *
     * @param chatID Telegram chat ID.
     */
    private void askRestart(long chatID) {
        sendMessage(chatID, Constants.MESSAGE_RESTART);
    }

    /**
     * Shows the list of metering devices bounded to the account.
     *
     * @param chatID Telegram chat ID.
     * @param user    the current user object.
     */
    private void displayMeters(long chatID, User user) {
        if (!userService.isUserExist(chatID)) {
            askRestart(chatID);
            return;
        }

        user.setUserState(UserState.START);

        List<HashMap<String, String>> meterDataList = integrationService.provideMeterInfo(chatID);
        for (HashMap<String, String> meterData : meterDataList) {
            String serialNumber = meterData.get("number");
            String service = meterData.get("service");
            String validThru = meterData.get("valid");
            String lastReading = meterData.get("lastReading");

            String messageText = Constants.MESSAGE_METER_DATA.formatted(serialNumber, service, lastReading, validThru);
            sendMessage(chatID, messageText);
        }
    }

    /**
     * Ask user for account number and set relevant user state.
     *
     * @param chatID Telegram chat ID.
     * @param user    the current user object.
     */
    private void askAccountNumber(long chatID, User user) {
        user.setUserState(UserState.ASKED_ACCOUNT_NUMBER);
        sendMessage(chatID, Constants.MESSAGE_ASK_ACCOUNT);
    }
}