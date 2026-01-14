/**
 * Нагрузочный тест для Product Service
 *
 * Запуск:
 *   k6 run load-test.js                           # smoke test (по умолчанию)
 *   k6 run -e PROFILE=light load-test.js          # лёгкая нагрузка
 *   k6 run -e PROFILE=medium load-test.js         # средняя нагрузка
 *   k6 run -e PROFILE=stress load-test.js         # стресс тест
 *   k6 run -e PROFILE=spike load-test.js          # spike тест
 *   k6 run -e BASE_URL=http://prod:8080 load-test.js  # другой URL
 *
 * С выводом в HTML:
 *   k6 run --out json=results.json load-test.js
 */

import { selectScenario, fullUserJourney } from './scenarios.js';
import { LOAD_PROFILES, THRESHOLDS, CONFIG } from './config.js';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';

// Выбираем профиль нагрузки
const profile = __ENV.PROFILE || 'smoke';
const loadProfile = LOAD_PROFILES[profile] || LOAD_PROFILES.smoke;

// Конфигурация теста
export const options = {
    ...loadProfile,
    thresholds: THRESHOLDS,

    // Теги для группировки метрик
    tags: {
        testProfile: profile,
    },
};

// Основная функция теста
export default function () {
    // Выбираем сценарий по весам из конфига
    const scenario = selectScenario();
    scenario();
}

// Форматирование результатов
export function handleSummary(data) {
    const summary = textSummary(data, { indent: '  ', enableColors: true });

    // Безопасное получение метрик
    const metrics = data.metrics || {};
    const httpReqs = metrics.http_reqs?.values || {};
    const httpDuration = metrics.http_req_duration?.values || {};
    const httpFailed = metrics.http_req_failed?.values || {};

    // Дополнительная статистика
    const customSummary = `
================================================================================
                        РЕЗУЛЬТАТЫ НАГРУЗОЧНОГО ТЕСТА
================================================================================

Профиль: ${profile.toUpperCase()}
URL: ${CONFIG.baseUrl}

Ключевые метрики:
  - Всего запросов: ${httpReqs.count || 0}
  - RPS: ${(httpReqs.rate || 0).toFixed(2)}
  - Средний response time: ${(httpDuration.avg || 0).toFixed(2)}ms
  - P95 response time: ${(httpDuration['p(95)'] || 0).toFixed(2)}ms
  - P99 response time: ${(httpDuration['p(99)'] || 0).toFixed(2)}ms
  - Ошибок: ${((httpFailed.rate || 0) * 100).toFixed(2)}%

================================================================================
`;

    console.log(customSummary);

    return {
        stdout: summary,
        'results/summary.json': JSON.stringify(data, null, 2),
    };
}
