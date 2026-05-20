package com.vodokanal.customerbot.service;

import com.vodokanal.customerbot.enums.UserState;
import com.vodokanal.customerbot.model.User;
import com.vodokanal.customerbot.util.ChatUtil;
import com.vodokanal.customerbot.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {
    private final long CHAT_ID = 98765432;

    @Mock
    private IntegrationService integrationService;
    @Mock
    private UserService userService;
    @Mock
    private ChatUtil chatUtil;
    @Mock
    private ReplyProducer replyProducer;
    @Mock
    private User user;

    @InjectMocks
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        when(userService.findOrCreateUser(CHAT_ID)).thenReturn(user);
    }

    @Test
    @DisplayName("Triggered /start command and Telegram ID is bound to account: show main menu")
    void shouldShowMainMenuWhenIDIsBound() {
        // given
        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(message.getText()).thenReturn("/start");

        Map<String, String> accountData = Map.of("number", "9876-543-2", "email", "test@test.com");
        when(integrationService.provideStarting(CHAT_ID)).thenReturn(accountData);

        SendMessage mockSendMessage = mock(SendMessage.class);
        when(chatUtil.buildMessage(anyString(), eq(CHAT_ID))).thenReturn(mockSendMessage);

        // when
        chatService.handleMessage(message);

        // then
        verify(user).setUserState(UserState.START);
        verify(user).setEmail("test@test.com");

        String messageWelcome = Constants.MESSAGE_MENU.formatted("9876-543-2", "test@test.com");
        verify(chatUtil).buildMessage(eq(messageWelcome), eq(CHAT_ID));
        verify(replyProducer).executeReply(any(SendMessage.class));
    }

    @Test
    @DisplayName("Triggered /start command but Telegram ID is not bound to account: advice registration")
    void shouldAdviceRegistrationWhenIDIsNotBound() {
        // given
        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(message.getText()).thenReturn("/start");

        when(integrationService.provideStarting(CHAT_ID)).thenReturn(Collections.emptyMap());
        SendMessage mockSendMessage = mock(SendMessage.class);
        when(chatUtil.buildMessage(anyString(), eq(CHAT_ID))).thenReturn(mockSendMessage);

        // when
        chatService.handleMessage(message);

        // then
        verify(user).setUserState(UserState.START);
        verify(chatUtil).buildMessage(eq(Constants.MESSAGE_WELCOME), eq(CHAT_ID));
        verify(replyProducer).executeReply(any(SendMessage.class));
    }

    @Test
    @DisplayName("Pressed Bind ID button: ask account number")
    void shouldAskAccountNumber() {
        //given
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        MaybeInaccessibleMessage maybeInaccessibleMessage = mock(MaybeInaccessibleMessage.class);

        when(callbackQuery.getMessage()).thenReturn(maybeInaccessibleMessage);
        when(maybeInaccessibleMessage.getChatId()).thenReturn(CHAT_ID);
        when(callbackQuery.getData()).thenReturn("bind_id");

        SendMessage mockSendMessage = mock(SendMessage.class);
        when(chatUtil.buildMessage(anyString(), eq(CHAT_ID))).thenReturn(mockSendMessage);

        //when
        chatService.handleCallbackQuery(callbackQuery);

        //then
        verify(user).setUserState(UserState.ASKED_ACCOUNT_NUMBER);
        verify(chatUtil).buildMessage(eq(Constants.MESSAGE_ASK_ACCOUNT), eq(CHAT_ID));
        verify(replyProducer).executeReply(any(SendMessage.class));
    }

    @Test
    @DisplayName("Sent valid account number: bind Telegram ID to account")
    void shouldBindTelegramIDToAccount() {
        //given
        String accountNumber = "9876-543-2";

        when(userService.isUserExist(CHAT_ID)).thenReturn(true);
        when(user.getUserState()).thenReturn(UserState.ASKED_ACCOUNT_NUMBER);

        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(message.getText()).thenReturn(accountNumber);

        when(chatUtil.isAccountNumberFormatValid(accountNumber)).thenReturn(true);

        Map<String, String> accountData = Map.of("number", "9876-543-2", "email", "test@test.com");
        when(integrationService.provideBindingID(accountNumber, CHAT_ID)).thenReturn(accountData);

        SendMessage mockSendMessage = mock(SendMessage.class);
        when(chatUtil.buildMessage(anyString(), eq(CHAT_ID))).thenReturn(mockSendMessage);

        //when
        chatService.handleMessage(message);

        // then
        verify(user).setEmail("test@test.com");
        verify(chatUtil).buildMessage(eq(Constants.MESSAGE_ACCOUNT_BIND_SUCCESS), eq(CHAT_ID));
    }

    @Test
    @DisplayName("Sent wrong account number: reject binding Telegram ID to account")
    void shouldFailBindingTelegramID() {
        //given
        String wrongNumber = "000000";

        when(userService.isUserExist(CHAT_ID)).thenReturn(true);
        when(user.getUserState()).thenReturn(UserState.ASKED_ACCOUNT_NUMBER);

        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(message.getText()).thenReturn(wrongNumber);

        when(chatUtil.isAccountNumberFormatValid(wrongNumber)).thenReturn(true);
        when(integrationService.provideBindingID(wrongNumber, CHAT_ID)).thenReturn(Collections.emptyMap());

        SendMessage mockSendMessage = mock(SendMessage.class);
        when(chatUtil.buildMessage(anyString(), eq(CHAT_ID))).thenReturn(mockSendMessage);

        //when
        chatService.handleMessage(message);

        // then
        verify(user, never()).setEmail(anyString());
        verify(chatUtil).buildMessage(eq(Constants.MESSAGE_ACCOUNT_BIND_FAIL), eq(CHAT_ID));
    }

    @Test
    @DisplayName("Sent valid current reading: save current reading")
    void shouldSaveCurrentReading() {
        //given
        String currentReading = "234.567";
        String previousReading = "123.456";
        String consumption = "111.111";


        when(userService.isUserExist(CHAT_ID)).thenReturn(true);
        when(user.getUserState()).thenReturn(UserState.ASKED_METER_CURRENT_READING);

        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(message.getText()).thenReturn(currentReading);

        when(chatUtil.isMeterValueFormatValid(currentReading)).thenReturn(true);
        when(user.getLastReadingValue()).thenReturn(previousReading);
        when(chatUtil.isCurrentValueValid(new BigDecimal(currentReading), new BigDecimal(previousReading)))
                .thenReturn(true);

        SendMessage mockSendMessage = mock(SendMessage.class);
        when(chatUtil.buildMessage(anyString(), eq(CHAT_ID))).thenReturn(mockSendMessage);

        //when
        chatService.handleMessage(message);

        //then
        verify(user).setCurrentReadingValue(currentReading);
        verify(user).setConsumption(consumption);
        verify(chatUtil).buildMessage(argThat(text -> text.replace(',', '.')
                .contains(consumption)), eq(CHAT_ID));
    }

    @Test
    @DisplayName("Sent current reading lower than previous one: reject acception")
    void shouldFailReadingVerification() {
        //given
        String currentReading = "123.456";
        String previousReading = "234.567";

        when(userService.isUserExist(CHAT_ID)).thenReturn(true);
        when(user.getUserState()).thenReturn(UserState.ASKED_METER_CURRENT_READING);

        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(message.getText()).thenReturn(currentReading);

        when(chatUtil.isMeterValueFormatValid(currentReading)).thenReturn(true);
        when(user.getLastReadingValue()).thenReturn(previousReading);
        when(chatUtil.isCurrentValueValid(new BigDecimal(currentReading), new BigDecimal(previousReading)))
                .thenReturn(false);

        //when
        chatService.handleMessage(message);

        //then
        verify(chatUtil).buildMessage(eq(Constants.MESSAGE_VALUE_INVALID), eq(CHAT_ID));
        verify(user).setUserState(UserState.ASKED_METER_CURRENT_READING);
        verify(user, never()).setCurrentReadingValue(anyString());
    }
}
