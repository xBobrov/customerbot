package com.vodokanal.customerbot.util;

public final class Constants {
    public static final String MESSAGE_WELCOME = "Добро пожаловать в телеграм бот АО \"Водоканал\"";
    public static final String MESSAGE_MENU = "Ваш лицевой счет: %s\nТекущий баланс: %s руб\n" +
            "Email: %s\n Выберите действие:";

    public static final String MESSAGE_BUTTON_EMAIL_ADD = "Привязать email";
    public static final String MESSAGE_BUTTON_EMAIL_CHANGE = "Изменить email";


    public static final String MESSAGE_METER_DATA = "ИПУ №: %s\nУслуга: %s\nПоследние переданные показания: %s\n" +
            "Дата следующей поверки: %s";
    public static final String MESSAGE_METER_CHECK_EXIST = "ИПУ с таким номером уже привязан к Вашему лицевому счету";
    public static final String MESSAGE_METER_CHECK_SUCCESS = "ИПУ успешно привязан к Вашему лицевому счету";

    public static final String MESSAGE_BUTTON_BIND_ID = "Привязать аккаунт к лицевому счету";
    public static final String MESSAGE_BUTTON_MY_METERS = "Мои ИПУ";
    public static final String MESSAGE_BUTTON_ADD_METER = "Добавить ИПУ";
    public static final String MESSAGE_BUTTON_SEND_READING = "Передать показания";
    public static final String MESSAGE_BUTTON_HOT_WATER = "Горячее водоснабжение";
    public static final String MESSAGE_BUTTON_COLD_WATER = "Холодное водоснабжение";
    public static final String MESSAGE_BUTTON_CHECK = "Проверить ИПУ в ФГИС и сохранить";
    public static final String MESSAGE_BUTTON_RETURN = "Вернуться в главное меню";

    public static final String MESSAGE_ACCOUNT_WRONG_FORMAT = "Введеный Вами номер не соответствует формату";
    public static final String MESSAGE_ACCOUNT_BIND_SUCCESS = "Ваш телеграм аккаунт успешно привязан к лицевому счету";
    public static final String MESSAGE_ACCOUNT_BIND_FAIL = "Лицевого счета с таким номером не обнаружено." +
            " Проверьте правильность ввода номера. Если номер введен верно, обратитесь в абонентский отдел АО \"Водоканал\"";

    public static final String MESSAGE_DATA_WRONG_FORMAT = "Введеная Вами дата не соответствует формату\nВведите дату поверки." +
            " Дата должна иметь формат: ГГГГ-ММ-ДД";
    public static final String  MESSAGE_VALUE_WRONG_FORMAT = "Введенное Вами показание не соответствует формату\n" +
            "Введите показания ИПУ указанные в акте поверки";

    public static final String MESSAGE_ASK_ACCOUNT = "Введите номер Вашего лицевого счета.\nНомер должен иметь формат: 0000-000-0";
    public static final String MESSAGE_ASK_METER_NUMBER = "Введите номер ИПУ.\n" +
            "Вводимый номер должен точно совподать с номером ИПУ указанным в акте поверки";
    public static final String MESSAGE_ASK_METER_VERIFICATION_DATE = "Введите дату поверки. Дата должна иметь формат: ГГГГ-ММ-ДД";
    public static final String MESSAGE_ASK_METER_INITIAL_VALUE = "Введите показания ИПУ указанные в акте поверки\n" +
            "Дробная часть числа должна быть отделена знаком .";
    public static final String MESSAGE_ASK_METER_SERVICE = "Выберите услугу";
    public static final String MESSAGE_ASK_METER_SAVE = "ИПУ № %s\nДата поверки: %s \nНачальное показание: %.3f \nУслуга: %s";

    public static final int METER_HOT_WATER_ID = 2;
    public static final int METER_COLD_WATER_ID = 1;

    private Constants() {}
}
