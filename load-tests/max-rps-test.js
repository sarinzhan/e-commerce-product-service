/**
 * Тест максимального RPS (без think time)
 *
 * Запуск:
 *   k6 run max-rps-test.js                    # 50 VUs, 30 сек
 *   k6 run -e VUS=100 max-rps-test.js         # 100 VUs
 *   k6 run -e VUS=200 -e DURATION=60s max-rps-test.js
 */

import http from 'k6/http';
import { check } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const VUS = parseInt(__ENV.VUS) || 50;
const DURATION = __ENV.DURATION || '30s';

export const options = {
    vus: VUS,
    duration: DURATION,

    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};

// Простой GET запрос без пауз - максимальный throughput
export default function () {
    const response = http.get(`${BASE_URL}/api/v1/products?page=1&limit=20`);

    check(response, {
        'status 200': (r) => r.status === 200,
    });
}

export function handleSummary(data) {
    const metrics = data.metrics || {};
    const httpReqs = metrics.http_reqs?.values || {};
    const httpDuration = metrics.http_req_duration?.values || {};
    const httpFailed = metrics.http_req_failed?.values || {};

    const summary = `
================================================================================
                          ТЕСТ МАКСИМАЛЬНОГО RPS
================================================================================

Конфигурация: ${VUS} VUs, ${DURATION}

Результаты:
  ┌─────────────────────────────────────────────────────────────────────────────
  │ RPS:              ${(httpReqs.rate || 0).toFixed(2)} req/s
  │ Всего запросов:   ${httpReqs.count || 0}
  │ Ошибки:           ${((httpFailed.rate || 0) * 100).toFixed(2)}%
  ├─────────────────────────────────────────────────────────────────────────────
  │ Response Time:
  │   - Avg:          ${(httpDuration.avg || 0).toFixed(2)}ms
  │   - Min:          ${(httpDuration.min || 0).toFixed(2)}ms
  │   - Max:          ${(httpDuration.max || 0).toFixed(2)}ms
  │   - P50:          ${(httpDuration.med || 0).toFixed(2)}ms
  │   - P90:          ${(httpDuration['p(90)'] || 0).toFixed(2)}ms
  │   - P95:          ${(httpDuration['p(95)'] || 0).toFixed(2)}ms
  │   - P99:          ${(httpDuration['p(99)'] || 0).toFixed(2)}ms
  └─────────────────────────────────────────────────────────────────────────────

================================================================================
`;

    console.log(summary);
    return {
        stdout: textSummary(data, { indent: '  ', enableColors: true }),
    };
}
