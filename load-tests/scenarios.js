import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { CONFIG } from './config.js';

const BASE_URL = CONFIG.baseUrl;

// Утилиты
function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomElement(array) {
    return array[Math.floor(Math.random() * array.length)];
}

function thinkTime() {
    sleep(randomInt(CONFIG.thinkTime.min, CONFIG.thinkTime.max));
}

// ============================================
// СЦЕНАРИИ ПОЛЬЗОВАТЕЛЕЙ
// ============================================

/**
 * Сценарий: Просмотр списка товаров
 * Типичное поведение: пользователь листает страницы товаров
 */
export function browseProducts() {
    const page = randomInt(1, CONFIG.pagination.maxPage);
    const limit = CONFIG.pagination.defaultLimit;

    const response = http.get(`${BASE_URL}/api/v1/products?page=${page}&limit=${limit}`, {
        tags: { scenario: 'browse_products' },
    });

    check(response, {
        'browse: status 200': (r) => r.status === 200,
        'browse: has data': (r) => {
            const body = JSON.parse(r.body);
            return body.data && Array.isArray(body.data);
        },
        'browse: response time < 500ms': (r) => r.timings.duration < 500,
    });

    thinkTime();
    return response;
}

/**
 * Сценарий: Поиск товаров
 * Пользователь ищет товары по названию или фильтрует по статусу
 */
export function searchProducts() {
    const searchTerm = randomElement(CONFIG.searchTerms);
    const useStatus = Math.random() > 0.5;
    const status = useStatus ? randomElement(CONFIG.statuses) : null;

    let url = `${BASE_URL}/api/v1/products?search=${encodeURIComponent(searchTerm)}`;
    if (status) {
        url += `&status=${status}`;
    }

    const response = http.get(url, {
        tags: { scenario: 'search_products' },
    });

    check(response, {
        'search: status 200': (r) => r.status === 200,
        'search: valid response': (r) => {
            try {
                JSON.parse(r.body);
                return true;
            } catch {
                return false;
            }
        },
        'search: response time < 500ms': (r) => r.timings.duration < 500,
    });

    thinkTime();
    return response;
}

/**
 * Сценарий: Просмотр конкретного товара
 * Пользователь открывает страницу товара
 */
export function viewProduct() {
    // Сначала получаем список товаров
    const listResponse = http.get(`${BASE_URL}/api/v1/products?page=1&limit=20`, {
        tags: { scenario: 'view_product_list' },
    });

    if (listResponse.status !== 200) {
        return listResponse;
    }

    const products = JSON.parse(listResponse.body).data;
    if (!products || products.length === 0) {
        return listResponse;
    }

    // Выбираем случайный товар
    const product = randomElement(products);
    thinkTime();

    // Запрашиваем детали товара
    const detailResponse = http.get(`${BASE_URL}/api/v1/products/${product.id}`, {
        tags: { scenario: 'view_product_detail' },
    });

    check(detailResponse, {
        'view: status 200': (r) => r.status === 200,
        'view: has product data': (r) => {
            const body = JSON.parse(r.body);
            return body.data && body.data.id;
        },
        'view: response time < 300ms': (r) => r.timings.duration < 300,
    });

    thinkTime();
    return detailResponse;
}

/**
 * Сценарий: Просмотр категорий
 * Пользователь смотрит дерево категорий
 */
export function viewCategories() {
    const response = http.get(`${BASE_URL}/api/v1/categories`, {
        tags: { scenario: 'view_categories' },
    });

    check(response, {
        'categories: status 200': (r) => r.status === 200,
        'categories: has data': (r) => {
            const body = JSON.parse(r.body);
            return body.data && Array.isArray(body.data);
        },
        'categories: response time < 200ms': (r) => r.timings.duration < 200,
    });

    thinkTime();
    return response;
}

/**
 * Сценарий: Полный путь пользователя
 * Категории -> Список товаров -> Просмотр товара -> Отзывы
 */
export function fullUserJourney() {
    group('User Journey', function () {
        // 1. Смотрим категории
        group('Browse Categories', function () {
            const catResponse = http.get(`${BASE_URL}/api/v1/categories`);
            check(catResponse, { 'categories loaded': (r) => r.status === 200 });
            thinkTime();
        });

        // 2. Смотрим товары
        group('Browse Products', function () {
            const prodResponse = http.get(`${BASE_URL}/api/v1/products?page=1&limit=20`);
            check(prodResponse, { 'products loaded': (r) => r.status === 200 });

            if (prodResponse.status === 200) {
                const products = JSON.parse(prodResponse.body).data;
                if (products && products.length > 0) {
                    thinkTime();

                    // 3. Открываем товар
                    group('View Product', function () {
                        const product = randomElement(products);
                        const detailResponse = http.get(`${BASE_URL}/api/v1/products/${product.id}`);
                        check(detailResponse, { 'product details loaded': (r) => r.status === 200 });
                        thinkTime();

                        // 4. Смотрим отзывы
                        group('View Reviews', function () {
                            const reviewsResponse = http.get(`${BASE_URL}/api/v1/products/${product.id}/reviews`);
                            check(reviewsResponse, { 'reviews loaded': (r) => r.status === 200 });
                        });
                    });
                }
            }
        });
    });
}

// ============================================
// ВЫБОР СЦЕНАРИЯ ПО ВЕСАМ
// ============================================

export function selectScenario() {
    const rand = Math.random() * 100;
    const { scenarios } = CONFIG;

    let cumulative = 0;

    cumulative += scenarios.browseProducts;
    if (rand < cumulative) return browseProducts;

    cumulative += scenarios.searchProducts;
    if (rand < cumulative) return searchProducts;

    cumulative += scenarios.viewProduct;
    if (rand < cumulative) return viewProduct;

    return viewCategories;
}
