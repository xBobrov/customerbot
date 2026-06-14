## Telegram-бот для взаимодействия абонентов с информационной системой водоснабжающей организации (Pet-project)

Телеграм-бот для взаимодействия абонента (пользователя) с моделью информационной системы ресурсоснабжающей организации, представленной в репазитории https://github.com/xBobrov/accounting. Проект разрабатывается в учебных целях для демонстрации навыков построения **микросервисной архитектуры** и взаимодействия с **Telegram API**.

## 🎯 Основные функции
*   **Привязка лицевого счета**: Аунтификация пользователя во внешней информационной системе.
*   **Передача показаний**: Пошаговый сценарий (FSM) ввода данных с валидацией "на лету".
*   **Умная валидация**: Проверка форматов (Regex) и бизнес-логики (сравнение с предыдущими показаниями через `BigDecimal`).
*   **Управление профилем**: Изменение/отвязка Email и просмотр списка доступных приборов учета.

## 📈 Схема работы чат-бота

![Схема работы чат-бота](https://github.com/xBobrov/customerbot/blob/master/assets/bot_scheme.png)

## 💻 Технические особенности

### 1. Finite State Machine (FSM)
Логика диалогов реализована на основе конечного автомата. Состояния пользователей (`UserState`) хранятся в `UserService` с использованием `ConcurrentHashMap`, что обеспечивает потокобезопасность при асинхронной обработке обновлений.

### 2. RabbitMQ RPC Pattern
Взаимодействие с внешним бэкендом (биллингом) реализовано через **паттерн Request-Response (RPC)**. Бот отправляет JSON-запрос в очередь и ожидает синхронного ответа, что позволяет изолировать бизнес-логику от транспортного уровня.

### 3. Чистая Архитектура
*   **Orchestrator (`ChatService`)**: Управляет только сценарием диалога.
*   **Gateway (`IntegrationService`)**: Инкапсулирует работу с очередями и маппинг данных.
*   **Utility Layer (`ChatUtil`)**: Содержит "чистые" функции валидации и сборки UI-элементов.

### 4. Тестирование и надежность
*   Покрытие ключевой бизнес-логики и сценариев перехода состояний с помощью **JUnit 5** и **Mockito**.
*   Реализована обработка сетевых исключений и таймаутов очереди (Fault Tolerance).

## 🛠 Технологический стек
*   **Java 22**, **Spring Boot 3**
*   **Spring AMQP** (RabbitMQ)
*   **Jackson** (JSON Processing)
*   **TelegramBots Spring Boot Starter**
*   **Docker & Docker Compose**

## 🚦 Запуск проекта

Данный проект является составной частью информационной системы наряду с crud-сервисом, представленным в отдельном репозитории: https://github.com/xBobrov/accounting.
Система спроектированна для запуска на платформе Docker и предполагает работу в связке с брокером сообщений RabbitMQ и базой данных PostgreSQL.

![Схема микросервисной архетиктуры](https://github.com/xBobrov/customerbot/blob/master/assets/project_scheme.png)

### Docker-compose для полной информационной системы

```yml
services:
  # База данных PostgreSQL
  postgres-db:
    image: postgres:16-alpine
    container_name: postgres_db
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: admin
      POSTGRES_DB: vodokanal
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U user -d crud_db"]
      interval: 5s
      timeout: 5s
      retries: 5

  # Очередь сообщений RabbitMQ
  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_running"]
      interval: 5s
      timeout: 5s
      retries: 5

  # CRUD Сервис (Maven) (https://github.com/xBobrov/accounting)
  accounting:
    build:
      context: ./accounting # Путь к папке с CRUD проектом
    container_name: accounting
    depends_on:
      postgres-db:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-db:5432/vodokanal
      SPRING_RABBITMQ_HOST: rabbitmq

  # Telegram Бот (Gradle) (данный проект)
  customerbot:
    build:
      context: ./customerbot # Путь к папке с Telegram-bot проектом 
    container_name: customerbot
    depends_on:
      rabbitmq:
        condition: service_healthy
    environment:
      SPRING_RABBITMQ_HOST: rabbitmq
      TELEGRAM_BOT_TOKEN: "***" # Токен Telegram
```

## 📝 Структура кода
```text
src/main/java/com/vodokanal/customerbot/
├── model/       # Доменные сущности (User)
├── service/     # Сервисы (Оркестрация, Интеграция, RabbitMQ)
├── util/        # Валидация и построение клавиатур
└── enums/       # Состояния FSM и коды операций
```

## 📅 Дальнейшие шаги по развитию проекта

* Расширение функционала чат-бота: получение квитанций на оплату, история начислений и оплат по лицевому счету, справочно-информационный раздел, интергация с внешними сервисами сферы ЖКХ.
* Полное покрытие кода unit-тестами и доработка документации javadoc.
* Сведение chat-bot и crud микросервисов в единый репазиторий. 

---
*Проект создается в учебных целях для демонстрации навыков Trainee/Junior Java Developer.*
