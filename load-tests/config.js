// Конфигурация нагрузочного тестирования

export const CONFIG = {
    // Базовый URL приложения
    baseUrl: __ENV.BASE_URL || 'http://localhost:8080',

    // Распределение сценариев (в сумме 100%)
    scenarios: {
        browseProducts: 50,    // 50% - просмотр списка товаров
        searchProducts: 20,    // 20% - поиск товаров
        viewProduct: 20,       // 20% - просмотр конкретного товара
        viewCategories: 10,    // 10% - просмотр категорий
    },

    // Think time (пауза между действиями пользователя)
    thinkTime: {
        min: 1,  // секунд
        max: 3,  // секунд
    },

    // Параметры пагинации
    pagination: {
        defaultLimit: 20,
        maxPage: 100,
    },

    // Данные для поиска (рандомно выбираются)
    searchTerms: [
        'Product',
        'Apple',
        'Samsung',
        'Nike',
        'Adidas',
        'Sony',
    ],

    // Статусы для фильтрации
    statuses: ['ACTIVE', 'DRAFT', 'DISCONTINUED'],
};

// Профили нагрузки
export const LOAD_PROFILES = {
    // Smoke test - проверка что всё работает
    smoke: {
        vus: 1,
        duration: '10s',
    },

    // Лёгкая нагрузка
    light: {
        stages: [
            { duration: '30s', target: 10 },   // ramp-up
            { duration: '1m', target: 10 },    // stay
            { duration: '30s', target: 0 },    // ramp-down
        ],
    },

    // Средняя нагрузка
    medium: {
        stages: [
            { duration: '1m', target: 50 },
            { duration: '3m', target: 50 },
            { duration: '1m', target: 0 },
        ],
    },

    // Стресс тест
    stress: {
        stages: [
            { duration: '1m', target: 300 },
            { duration: '1m', target: 400 },
        ],
    },

    // Spike test - резкий скачок нагрузки
    spike: {
        stages: [
            { duration: '10s', target: 10 },
            { duration: '1m', target: 10 },
            { duration: '10s', target: 200 },  // spike!
            { duration: '1m', target: 200 },
            { duration: '10s', target: 10 },
            { duration: '1m', target: 10 },
            { duration: '10s', target: 0 },
        ],
    },
};

// Пороговые значения (SLO)
export const THRESHOLDS = {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],  // 95% < 500ms, 99% < 1s
    http_req_failed: ['rate<0.01'],                   // < 1% ошибок
    http_reqs: ['rate>100'],                          // > 100 RPS
};
