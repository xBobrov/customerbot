package com.vodokanal.customerbot.util;

public final class Constants {
    public static final String MESSAGE_WELCOME = "Добро пожаловать в телеграм бот АО \"Водоканал\"";
    public static final String MESSAGE_MENU = "Ваш лицевой счет: %s\nТекущий баланс: %s руб\n" +
            "Email: %s\n Выберите действие:";
    public static final String MESSAGE_RESTART = "Что-то пошло не так. Пожалуйста отправте комманду /start";


    public static final String MESSAGE_BUTTON_EMAIL_CHANGE = "Изменить email";
    public static final String MESSAGE_ASK_EMAIL = "Введите адрес электронной почты. Если хотите отвязать адрес, " +
            "отправте цифру 0";
    public static final String MESSAGE_EMAIL_WRONG_FORMAT = "Адрес электронной почты имеет неверный формат";
    public static final String MESSAGE_EMAIL_UNLINKED = "Адрес электронной почты отвязан от Вашего лицевого счета";
    public static final String MESSAGE_EMAIL_UNABLE_UNLINKED = "Невозможно выполнить комманду, к Вашей учетной " +
            "записи не привязан Email";
    public static final String MESSAGE_EMAIL_LINKED = "Адрес электронной почты %s привязан к Вашему лицевому счету";
    public static final String MESSAGE_EMAIL_NOT_LINKED = " не установлен";

    public static final String MESSAGE_READING_WRONG_DATE = "Показания принимаются с 20 по 25 число включительно";
    public static final String MESSAGE_READING_CONSUMPTION = "Расход: %.3f м³";
    public static final String MESSAGE_READING_ACCEPTED = "Переданые показания ИПУ приняты в обработку.";
    public static final String MESSAGE_ASK_METER_NUMBER = "Введите номер ИПУ";
    public static final String MESSAGE_ASK_CURRENT_VALUE = "Введите текущие показания ИПУ. Дробная часть числа должна" +
            "быть отделена точкой и иметь не более трех знаков.";

    public static final String MESSAGE_METER_DATA = "ИПУ №: %s\nУслуга: %s\nПоследние переданные показания: %s\n" +
            "Дата следующей поверки: %s";
    public static final String MESSAGE_METER_NOT_FOUND = "ИПУ с таким номером не привязан к Вашему лицквому счету." +
            "Проверьте правильность номера или обратитесь в абонентский отдел АО \"Водоканал\"";

    public static final String MESSAGE_BUTTON_BIND_ID = "Привязать аккаунт к лицевому счету";
    public static final String MESSAGE_BUTTON_MY_METERS = "Мои ИПУ";
    public static final String MESSAGE_BUTTON_APPROVE = "Подтвердить и отправить";
    public static final String MESSAGE_BUTTON_SEND_READING = "Передать показания";

    public static final String MESSAGE_ACCOUNT_WRONG_FORMAT = "Введеный Вами номер не соответствует формату";
    public static final String MESSAGE_ACCOUNT_BIND_SUCCESS = "Ваш телеграм аккаунт успешно привязан к лицевому счету";
    public static final String MESSAGE_ACCOUNT_BIND_FAIL = "Лицевого счета с таким номером не обнаружено." +
            " Проверьте правильность номера или обратитесь в абонентский отдел АО \"Водоканал\"";

    public static final String  MESSAGE_VALUE_WRONG_FORMAT = "Введенное Вами показание ИПУ не соответствует формату";
    public static final String  MESSAGE_VALUE_INVALID = "Введенное Вами показание ИПУ меньше предыдущего учтенного показания";

    public static final String MESSAGE_ASK_ACCOUNT = "Введите номер Вашего лицевого счета.\nНомер должен иметь формат: 0000-000-0";

    public static final int READING_START_DAY = 20;
    public static final int READING_END_DAY = 30;

    private Constants() {}
}
