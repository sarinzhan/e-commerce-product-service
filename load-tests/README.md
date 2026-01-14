# Нагрузочное тестирование Product Service

## Установка k6

```bash
# macOS
brew install k6

# Linux
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6

# Docker
docker run --rm -i grafana/k6 run - <load-test.js
```

## Запуск тестов

```bash
cd load-tests

# Smoke test (1 пользователь, 10 секунд) - проверка работоспособности
k6 run load-test.js

# Лёгкая нагрузка (10 пользователей)
k6 run -e PROFILE=light load-test.js

# Средняя нагрузка (50 пользователей)
k6 run -e PROFILE=medium load-test.js

# Стресс тест (до 200 пользователей)
k6 run -e PROFILE=stress load-test.js

# Spike тест (резкий скачок до 200)
k6 run -e PROFILE=spike load-test.js

# С кастомным URL
k6 run -e PROFILE=medium -e BASE_URL=http://192.168.1.100:8080 load-test.js
```

## Профили нагрузки

| Профиль | VUs | Длительность | Назначение |
|---------|-----|--------------|------------|
| smoke | 1 | 10s | Проверка что API работает |
| light | 10 | 2m | Базовая нагрузка |
| medium | 50 | 5m | Типичная нагрузка |
| stress | 100-200 | 16m | Поиск пределов |
| spike | 10→200→10 | 5m | Устойчивость к скачкам |

## Сценарии пользователей

Распределение по умолчанию (настраивается в `config.js`):

- **50%** - Просмотр списка товаров (пагинация)
- **20%** - Поиск товаров
- **20%** - Просмотр конкретного товара
- **10%** - Просмотр категорий

## Метрики и пороги (SLO)

```javascript
thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],  // 95% < 500ms
    http_req_failed: ['rate<0.01'],                   // < 1% ошибок
    http_reqs: ['rate>100'],                          // > 100 RPS
}
```

## Пример вывода

```
================================================================================
                        РЕЗУЛЬТАТЫ НАГРУЗОЧНОГО ТЕСТА
================================================================================

Профиль: MEDIUM
URL: http://localhost:8080

Ключевые метрики:
  - Всего запросов: 15234
  - RPS: 253.90
  - Средний response time: 45.23ms
  - P95 response time: 120.45ms
  - P99 response time: 250.12ms
  - Ошибок: 0.02%

================================================================================
```

## Интерпретация результатов

### Хорошие показатели:
- P95 < 200ms
- P99 < 500ms
- Ошибки < 0.1%
- RPS стабильный

### Проблемы:
- P95 > 1s → узкое место (скорее всего БД)
- Ошибки > 1% → нужно увеличить пул соединений
- RPS падает при увеличении VUs → достигнут предел

## Сохранение результатов

```bash
# JSON для анализа
k6 run --out json=results.json load-test.js

# С Prometheus метриками
k6 run --out experimental-prometheus-rw load-test.js
```
