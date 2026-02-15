package com.vodokanal.customerbot.util;

import com.vodokanal.customerbot.model.Meter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class ChatUtil {
    private final MappingUtil mappingUtil;

    public ChatUtil(MappingUtil mappingUtil) {
        this.mappingUtil = mappingUtil;
    }

    public boolean isDateFormatValid(String date) {
        return date.matches("(19|20)\\d\\d-((0[1-9]|1[012])-(0[1-9]|[12]\\d)|(0[13-9]|1[012])-30|(0[13578]|1[02])-31)");
    }

    public boolean isAccountNumberFormatValid(String accountNumber) {
        return accountNumber.matches("\\d{4}-\\d{3}-\\d");
    }

    public SendMessage buildMessage(String text, long chatID) {

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

    public InlineKeyboardMarkup buildKeyboard(String[] buttonData) {
        List<InlineKeyboardRow> keyboardRowList = IntStream.iterate(0, i -> i < buttonData.length, i -> i + 2)
                .mapToObj(i -> new InlineKeyboardRow(buildButton(buttonData[i], buttonData[i + 1])))
                .toList();

        return new InlineKeyboardMarkup(keyboardRowList);
    }

    public String getMeterExpirationDate(Meter meter) {
        String requestDate = meter.getVerificationDate();
        String requestNumber = meter.getSerialNumber();

        String uri = Constants.FGIS_URL + Constants.FGIS_REQUEST_STRING.formatted(requestDate, requestNumber);
        String request = executeRequest(uri);
        String reply = mappingUtil.parseFGISResponse(request);

        return reply;
    }

    private String executeRequest(String uri) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
