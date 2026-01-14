# Product Service

REST API микросервис для управления товарами, категориями и отзывами.

## Tech Stack

- Java 23
- Spring Boot 4.0.1
- PostgreSQL
- Docker
- Grafana Stack (Prometheus, Loki, Tempo)

## Быстрый старт

### 1. Запуск с Docker Compose

```bash
# Запустить PostgreSQL + Observability
docker-compose -f docker/observability/docker-compose.yml up -d

# Запустить приложение
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5436/product_service \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  jlet4uk/product-service:latest
```

### 2. Локальный запуск

```bash
# Требования: Java 23, PostgreSQL
./mvnw spring-boot:run
```

## Документация API

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Observability

### Запуск стека мониторинга

```bash
cd docker/observability
docker-compose up -d
```

### Доступные сервисы

| Сервис | URL | Описание |
|--------|-----|----------|
| Grafana | http://localhost:3000 | Дашборды (admin/admin) |
| Prometheus | http://localhost:9090 | Метрики |
| Loki | http://localhost:3100 | Логи |
| Tempo | http://localhost:4317 | Трейсы |

### Endpoints приложения

| Endpoint | Описание |
|----------|----------|
| `/actuator/health` | Health check |
| `/actuator/prometheus` | Метрики для Prometheus |
| `/actuator/info` | Информация о приложении |

## Нагрузочное тестирование

```bash
cd load-tests

# Установка k6
brew install k6

# Smoke test
k6 run load-test.js

# Нагрузочный тест
k6 run -e PROFILE=medium load-test.js

# Тест максимального RPS
k6 run max-rps-test.js
```

Подробнее: [load-tests/README.md](load-tests/README.md)

## Генерация тестовых данных

```bash
# 1000 товаров
psql -U postgres -d product_service -v count=1000 -f scripts/generate_data.sql

# 100000 товаров
psql -U postgres -d product_service -v count=100000 -f scripts/generate_data.sql
```

## Docker

### Сборка образа

```bash
docker build -t jlet4uk/product-service:latest .
```

```bash
docker buildx build --platform linux/amd64 -t jlet4uk/product-service:latest --push .
```


### Публикация

```bash
docker push jlet4uk/product-service:latest
```

### Запуск

```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/db \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=pass \
  jlet4uk/product-service:latest
```

## Структура проекта

```
product-service/
├── src/main/java/kg/kazbekov/productservice/
│   ├── controller/     # REST контроллеры
│   ├── service/        # Бизнес-логика
│   ├── repository/     # JPA репозитории
│   ├── model/          # Сущности (Product, Category, Review)
│   ├── dto/            # DTO для API
│   ├── mapper/         # MapStruct мапперы
│   ├── exception/      # Обработка ошибок
│   └── config/         # Конфигурация
├── docker/
│   └── observability/  # Grafana, Prometheus, Loki, Tempo
├── load-tests/         # k6 нагрузочные тесты
├── scripts/            # SQL скрипты
├── Dockerfile
└── pom.xml
```