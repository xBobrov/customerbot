package com.vodokanal.customerbot.service;

import com.vodokanal.customerbot.enums.Operation;
import com.vodokanal.customerbot.model.User;
import com.vodokanal.customerbot.util.MappingUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A gateway for interacting with an external accounting system (billing).
 * Encapsulates exchanging protocol through RabbitMQ and
 * deserialization JSON-responses into Java collection objects.
 */
@Service
public class IntegrationService {
    private final RabbitMQMessageService rabbitMQMessageService;
    private final MappingUtil mappingUtil;


    public IntegrationService(RabbitMQMessageService rabbitMQMessageService, MappingUtil mappingUtil) {
        this.rabbitMQMessageService = rabbitMQMessageService;
        this.mappingUtil = mappingUtil;
    }

    /**
     * Validates metering device existence.
     *
     * @param meterNumber meter serial number.
     * @param chatID Telegram user identifier.
     * @return Map with keys: 'id', 'number', 'service', 'lastReading', 'valid'.
     *         Returns an empty map when metering device is not found.
     */
    public Map<String, String> provideMeterValidation(String meterNumber, long chatID) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.METER_VALIDATION.getOperation());
        requestData.put("meterNumber", meterNumber);
        requestData.put("chatID", String.valueOf(chatID));

        String meterDataJson = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));

        return mappingUtil.mapJsonToHashMap(meterDataJson);
    }

    /**
     * Request Email bound to account.
     *
     * @param chatID Telegram user identifier.
     * @return String with Email or empty string if Email is undefined.
     */
    public String provideEmailInfo(long chatID) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.EMAIL_INFO.getOperation());
        requestData.put("chatID", String.valueOf(chatID));

        return rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));
    }

    /**
     * Binds or updates the user's email in the database.
     *
     * @param email new address string (or "0" for unbind).
     * @param chatID Telegram user identifier.
     * @return String with Email or empty string if Email is undefined.
     */
    public String provideEmailChanging(String email, long chatID) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.CHANGE_EMAIL.getOperation());
        requestData.put("email", email);
        requestData.put("chatID", String.valueOf(chatID));

        return rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));
    }

    /**
     * Binds Telegram ID to the account in the database and returns account data.
     *
     * @param accountNumber account number. Format: ####-###-#  (# — any digit).
     * @param chatID Telegram user identifier.
     * @return Map with keys 'id', 'number', 'address', 'payer', 'email', 'isActive',
     *         'telegramID', 'balance', 'residentRegd', 'active'.
     *         Returns an empty map when account is not found.
     */
    public Map<String, String> provideBindingID(String accountNumber, long chatID) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.BIND_ID.getOperation());
        requestData.put("accountNumber", accountNumber);
        requestData.put("chatID", String.valueOf(chatID));

        String accountDataJson = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));

        return mappingUtil.mapJsonToHashMap(accountDataJson);
    }

    /**
     * Requests all metering devices assigned to the account.
     *
     * @param chatID Telegram user identifier.
     * @return List of Maps with keys 'id', 'number', 'service', 'lastReading', 'valid'.
     *         Returns an empty map when no metering devices are found.
     */
    public List<HashMap<String, String>> provideMeterInfo(long chatID) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.METER_INFO.getOperation());
        requestData.put("chatID", String.valueOf(chatID));

        String meterInfoJson = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));

        return mappingUtil.mapJsonToHashMapList(meterInfoJson);
    }

    /**
     * Submits meter reading to the database.
     *
     * @param chatID Telegram user identifier.
     * @param user an object {@link User} containing meter number, current reading and
     *             consumption value for submission to the database.
     * @return The number of lines inserted in the database.
     */
    public String provideReadingSubmission(long chatID, User user) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.READING_TRANSMIT.getOperation());
        requestData.put("chatID", String.valueOf(chatID));
        requestData.put("meterNumber", user.getMeterNumber());
        requestData.put("currentReading", user.getCurrentReadingValue());
        requestData.put("consumption", user.getConsumption());

        return rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));
    }

    /**
     * Check if Telegram ID is bound to the account.
     *
     * @param chatID Telegram user identifier.
     * @return Map with keys 'id', 'number', 'address', 'payer', 'email', 'isActive',
     *         'telegramID', 'balance', 'residentRegd', 'active'.
     *         Returns an empty map when account is not found.
     */
    public Map<String, String> provideStarting(long chatID) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("operation", Operation.START.getOperation());
        requestData.put("chatID", String.valueOf(chatID));

        String accountData = rabbitMQMessageService.sendMessage(mappingUtil.mapObjectToJson(requestData));
        return mappingUtil.mapJsonToHashMap(accountData);
    }
}
