<h1 align="center">Микросервис контрагентов</h1>

Микросервис для управления контрагентами, построенный на Spring Boot с использованием PostgreSQL в качестве базы данных.

## Требования

Для локального развертывания приложения необходимо установить:


- Docker
- Docker Compose
- RabbitMQ
- Redis


## Локальное развертывание

### 1. Клонирование репозитория

```bash
git clone https://github.com/Vlad1slavS/Contractor-microservice.git
cd Contractor-microservice
```

### 2. Сборка и запуск приложения

Для запуска всех сервисов выполните команду:

```bash
docker-compose up --build
```

### 3. Проверка статуса сервисов

Для проверки состояния контейнеров:

```bash
docker-compose ps
```

### 4. Просмотр логов

Для просмотра логов всех сервисов:

```bash
docker-compose logs -f
```

Для просмотра логов конкретного сервиса:

```bash
docker-compose logs -f app
docker-compose logs -f postgres
docker-compose logs -f rabbit
docker-compose logs -f redis
```

## Доступ к приложению

После успешного запуска:

- **Приложение**: http://localhost:8080
- **База данных PostgreSQL**: http://localhost:5432
- **Rabbit UI**: http://localhost:15672

## Подключение к базе данных

Параметры подключения к PostgreSQL:

- **Host**: localhost
- **Port**: 5432
- **Database**: contractor_db
- **Username**: contractor
- **Password**: 1234

Параметры подключения к RabbitMQ:
- **Host**: localhost
- **Port**: 5672
- **Username**: guest
- **Password**: guest

Параметры подключения к Redis:
- **Host**: localhost
- **Port**: 6379
- **Password**: -


## Конфигурация

Основные переменные окружения для приложения:

- `SPRING_DATASOURCE_URL` - URL подключения к базе данных
- `SPRING_DATASOURCE_USERNAME` - имя пользователя БД
- `SPRING_DATASOURCE_PASSWORD` - пароль БД
- `SPRING_LIQUIBASE_CHANGE-LOG` - путь к changelog файлу Liquibase
- `spring.rabbitmq.host` - хост для подключения к RabbitMQ
- `spring.rabbitmq.port` - порт
- `spring.rabbitmq.username` - username
- `spring.rabbitmq.password` - пароль
- `spring.rabbitmq.template.receive-timeout` - Таймаут (в миллисекундах) для получения сообщения
- `spring.rabbitmq.template.reply-timeout` - Таймаут на ожидание ответа
- `spring.data.redis.host` - хост redis
- `spring.data.redis.port` - порт
- `spring.data.redis.password` - пароль


