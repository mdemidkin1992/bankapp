# BankApp - Микросервисное банковское приложение

Банковское приложение, реализованное с использованием микросервисной архитектуры на Spring Boot.
Позволяет регистрироваться в системе, управлять счетами в различных валютах, переводить деньги и отслеживать курсы валют.

## Запуск проекта

Для запуска проекта на порту `8080` используйте команды:

```bash
# Сборка jar файлов
gradle clean build -x test

# Сборка и запуск контейнеров
docker-compose up --build -d
```
Убедиться, что все контейнеры запущены.
Приложение станет доступно по адресу http://localhost:8080

Для входа в систему используйте существующих пользователей:

- user1 / password1
- user2 / password2
- user3 / password3

## Архитектура проекта

Проект имеет микросервисную архитектуру:

- **service-gateway** - API Gateway и точка входа (порт `8080`)
- **service-front** - веб-интерфейс фронтенда
- **service-accounts** - управление аккаунтами и счетами
- **service-cash** - пополнение и снятие денег
- **service-transfer** - переводы между счетами
- **service-exchange** - генерация курсов валют 
- **service-convert** - конвертация валют по курсу
- **service-blocker** - блокировка подозрительных операций
- **service-notifications** - уведомления пользователей

```
bankapp/
├── service-gateway/         # API Gateway
├── service-front/           # Frontend UI
├── service-accounts/        # Управление аккаунтами
├── service-cash/            # Операции с наличными
├── service-transfer/        # Переводы
├── service-convert/         # Обмен валют
├── service-exchange/        # Курсы валют
├── service-blocker/         # Блокировка операций  
├── service-notifications/   # Уведомления
├── common-dto/              # Общие DTO
└── docker-compose.yaml
```

## Технологии

- Spring Boot WebFlux (реактивный стек)
- Spring Cloud Config Server (externalized хранилище конфигураций)
- Spring Cloud Gateway (API Gateway)
- Spring Security (аутентификация и авторизация)
- OAuth2 / Keycloak (межсервисная аутентификация)
- Apache Zookeeper (Service Discovery)
- Resilience4j (Circuit Breaker, Retry)
- PostgreSQL (база данных)
- Docker & Docker Compose

## API эндпоинты

Главная страница (через Gateway):
- GET / - редирект на /main
- GET /main - главная страница с данными пользователя

Управление аккаунтом:
- POST /user/{login}/editPassword - смена пароля
- POST /user/{login}/editUserAccounts - редактирование профиля

Финансовые операции:
- POST /user/{login}/cash - пополнение/снятие денег
- POST /user/{login}/transfer - переводы между счетами

Регистрация:
- GET /signup - форма регистрации
- POST /signup - создание нового пользователя

API курсов валют:
- GET /api/rates - получение текущих курсов валют (JSON)

## Схема взаимодействия сервисов

```
Gateway     → Front UI
            → Accounts (регистрация, профиль, счета)
            → Cash (пополнение/снятие)
            → Transfer (переводы)
            → Exchange (курсы валют)
Cash        → Accounts, Blocker, Notifications
Transfer    → Accounts, Convert, Blocker, Notifications  
Accounts    → Notifications
Convert     → Exchange
```

## Service Discovery

Используется Apache Zookeeper для регистрации и обнаружения сервисов:

```yaml
spring:
  cloud:
    zookeeper:
      connect-string: zookeeper:2181
      discovery:
        enabled: true
```

## Resilience4j / Circuit Breaker

Микросервисы используют паттерны устойчивости:

- Retry - автоматические повторы запросов (3 попытки с паузой 1с)
- Circuit Breaker - прерывание цепи при 50% ошибок
- Fallback методы - заглушки при недоступности сервисов

```yaml
resilience4j:
  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 1s
  circuitbreaker:
    configs:
      default:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
```

## Spring Cloud Config Server

Централизованная конфигурация через Spring Cloud Config Server с хранением в **config-server**

```yaml
spring:
  application:
    name: service-front
  config:
    import: configserver:${CONFIG_SERVER_URL:http://127.0.0.1:8888}
```

## Безопасность

#### Spring Security

Приложение использует Spring Security для защиты ресурсов:

- Форма входа для базовой аутентификации
- CSRF защита для POST запросов
- Сессии пользователей

#### OAuth2 с Keycloak

Используется для безопасного межсервисного взаимодействия:

- Client Credentials Grant для сервис-сервис аутентификации
- JWT токены для авторизации запросов между client и resource-server
- Валидация токенов на стороне resource-server

## База данных

- PostgreSQL - основная база данных
- Отдельные схемы для каждого микросервиса (Database per Service pattern)
- Миграции через Liquibase

## Тесты

Для запуска тестов:

```bash
# Все сервисы
gradle clean test

# Отдельные сервисы
gradle :service-accounts:test
gradle :service-gateway:test
```

## Dockerfile
Каждый микросервис упакован в Docker контейнер:

```dockerfile
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY service-*/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

## Jenkins

Запуск Jenkins:

```
java -Dhudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT=true -jar jenkins.war
```

