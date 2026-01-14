-- ==============================================
-- Процедура генерации тестовых данных
--
-- Использование:
--   CALL generate_test_data(100000);
--   CALL generate_test_data(1000000);
-- ==============================================
call generate_test_data(100000);

select count(*) from products;


CREATE OR REPLACE PROCEDURE generate_test_data(total_products INT DEFAULT 100000)
LANGUAGE plpgsql
AS $$
DECLARE
    batch_size INT;
    current_batch INT := 0;
    category_count INT;
    product_count INT;
    review_count INT;
    brands TEXT[] := ARRAY['Apple', 'Samsung', 'Nike', 'Adidas', 'Sony', 'LG', 'Xiaomi', 'Huawei', 'Puma', 'Reebok', 'Dell', 'HP', 'Lenovo', 'Asus', 'Bosch'];
    colors TEXT[] := ARRAY['black', 'white', 'red', 'blue', 'green', 'yellow', 'gray', 'brown', 'pink', 'orange'];
    sizes TEXT[] := ARRAY['XS', 'S', 'M', 'L', 'XL', 'XXL'];
BEGIN
    batch_size := LEAST(100000, total_products);
    review_count := total_products * 3;

    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Generating % products', total_products;
    RAISE NOTICE '==========================================';

    -- ==============================================
    -- 1. Категории
    -- ==============================================
    RAISE NOTICE 'Creating categories...';

    -- Корневые категории
    INSERT INTO categories (id, parent_id, name, slug, is_active, created_at, updated_at)
    VALUES
        (gen_random_uuid(), NULL, 'Электроника', 'electronics', true, NOW(), NOW()),
        (gen_random_uuid(), NULL, 'Одежда', 'clothing', true, NOW(), NOW()),
        (gen_random_uuid(), NULL, 'Дом и сад', 'home-garden', true, NOW(), NOW()),
        (gen_random_uuid(), NULL, 'Спорт', 'sports', true, NOW(), NOW()),
        (gen_random_uuid(), NULL, 'Книги', 'books', true, NOW(), NOW()),
        (gen_random_uuid(), NULL, 'Авто', 'auto', true, NOW(), NOW()),
        (gen_random_uuid(), NULL, 'Красота', 'beauty', true, NOW(), NOW()),
        (gen_random_uuid(), NULL, 'Игрушки', 'toys', true, NOW(), NOW()),
        (gen_random_uuid(), NULL, 'Еда', 'food', true, NOW(), NOW()),
        (gen_random_uuid(), NULL, 'Техника', 'appliances', true, NOW(), NOW())
    ON CONFLICT (slug) DO NOTHING;

    -- Подкатегории электроники
    INSERT INTO categories (id, parent_id, name, slug, is_active, created_at, updated_at)
    SELECT gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'electronics'), name, slug, true, NOW(), NOW()
    FROM (VALUES ('Смартфоны', 'smartphones'), ('Ноутбуки', 'laptops'), ('Телевизоры', 'tvs'), ('Наушники', 'headphones'), ('Планшеты', 'tablets')) AS t(name, slug)
    ON CONFLICT (slug) DO NOTHING;

    -- Подкатегории одежды
    INSERT INTO categories (id, parent_id, name, slug, is_active, created_at, updated_at)
    SELECT gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'clothing'), name, slug, true, NOW(), NOW()
    FROM (VALUES ('Мужская', 'mens'), ('Женская', 'womens'), ('Детская', 'kids'), ('Обувь', 'shoes'), ('Аксессуары', 'accessories')) AS t(name, slug)
    ON CONFLICT (slug) DO NOTHING;

    -- Подкатегории спорта
    INSERT INTO categories (id, parent_id, name, slug, is_active, created_at, updated_at)
    SELECT gen_random_uuid(), (SELECT id FROM categories WHERE slug = 'sports'), name, slug, true, NOW(), NOW()
    FROM (VALUES ('Фитнес', 'fitness'), ('Бег', 'running'), ('Велоспорт', 'cycling'), ('Плавание', 'swimming')) AS t(name, slug)
    ON CONFLICT (slug) DO NOTHING;

    -- ==============================================
    -- 2. Отключаем индексы для быстрой вставки
    -- ==============================================
    RAISE NOTICE 'Dropping indexes...';

    DROP INDEX IF EXISTS idx_products_sku;
    DROP INDEX IF EXISTS idx_products_slug;
    DROP INDEX IF EXISTS idx_products_category;
    DROP INDEX IF EXISTS idx_products_status;
    DROP INDEX IF EXISTS idx_reviews_product;
    DROP INDEX IF EXISTS idx_reviews_user;

    -- ==============================================
    -- 3. Генерация продуктов
    -- ==============================================
    RAISE NOTICE 'Generating products...';

    CREATE TEMP TABLE IF NOT EXISTS temp_category_ids AS
    SELECT id, ROW_NUMBER() OVER () as rn FROM categories WHERE parent_id IS NOT NULL;
    CREATE INDEX ON temp_category_ids(rn);

    SELECT COUNT(*) INTO category_count FROM temp_category_ids;

    IF category_count = 0 THEN
        RAISE EXCEPTION 'No categories found!';
    END IF;

    current_batch := 0;
    WHILE current_batch * batch_size < total_products LOOP
        INSERT INTO products (
            id, sku, name, slug, description, brand_name, category_id,
            price, compare_at_price, status, attributes, images,
            created_at, updated_at
        )
        SELECT
            gen_random_uuid(),
            'SKU-' || LPAD((current_batch * batch_size + i)::TEXT, 10, '0'),
            'Product ' || (current_batch * batch_size + i),
            'product-' || (current_batch * batch_size + i),
            'Description for product ' || (current_batch * batch_size + i),
            brands[1 + ((current_batch * batch_size + i) % array_length(brands, 1))],
            (SELECT id FROM temp_category_ids WHERE rn = ((current_batch * batch_size + i) % category_count) + 1),
            ROUND((10 + RANDOM() * 990)::NUMERIC, 2),
            CASE WHEN RANDOM() > 0.7 THEN ROUND((20 + RANDOM() * 1000)::NUMERIC, 2) ELSE NULL END,
            CASE WHEN RANDOM() < 0.7 THEN 'ACTIVE' WHEN RANDOM() < 0.9 THEN 'DRAFT' ELSE 'DISCONTINUED' END,
            jsonb_build_object(
                'color', colors[1 + ((current_batch * batch_size + i) % array_length(colors, 1))],
                'size', sizes[1 + ((current_batch * batch_size + i) % array_length(sizes, 1))]
            ),
            jsonb_build_array(
                jsonb_build_object('url', 'https://picsum.photos/400/400?random=' || (current_batch * batch_size + i), 'alt', 'Image', 'primary', 'true')
            ),
            NOW() - (RANDOM() * INTERVAL '365 days'),
            NOW()
        FROM generate_series(1, LEAST(batch_size, total_products - current_batch * batch_size)) AS i;

        current_batch := current_batch + 1;
        RAISE NOTICE 'Products: % / %', LEAST(current_batch * batch_size, total_products), total_products;
        COMMIT;
    END LOOP;

    DROP TABLE IF EXISTS temp_category_ids;

    -- ==============================================
    -- 4. Генерация отзывов (~3 на продукт)
    -- ==============================================
    RAISE NOTICE 'Generating reviews...';

    SELECT COUNT(*) INTO product_count FROM products;

    IF product_count = 0 THEN
        RAISE NOTICE 'No products found, skipping reviews.';
    ELSE
        CREATE TEMP TABLE IF NOT EXISTS temp_product_ids AS
        SELECT id, ROW_NUMBER() OVER () as rn FROM products;
        CREATE INDEX ON temp_product_ids(rn);

        current_batch := 0;
        WHILE current_batch * batch_size < review_count LOOP
            INSERT INTO reviews (id, product_id, user_id, rating, comment, created_at)
            SELECT
                gen_random_uuid(),
                (SELECT id FROM temp_product_ids WHERE rn = ((current_batch * batch_size + i - 1) % product_count) + 1),
                gen_random_uuid(),
                1 + (RANDOM() * 4)::INT,
                CASE (i % 10)
                    WHEN 0 THEN 'Отличный товар!'
                    WHEN 1 THEN 'Хорошее качество'
                    WHEN 2 THEN 'Рекомендую'
                    WHEN 3 THEN 'Средне'
                    WHEN 4 THEN 'Могло быть лучше'
                    WHEN 5 THEN 'Супер!'
                    WHEN 6 THEN 'Нормально'
                    WHEN 7 THEN 'Доволен покупкой'
                    WHEN 8 THEN 'Быстрая доставка'
                    ELSE 'Всё отлично'
                END,
                NOW() - (RANDOM() * INTERVAL '180 days')
            FROM generate_series(1, LEAST(batch_size, review_count - current_batch * batch_size)) AS i;

            current_batch := current_batch + 1;
            RAISE NOTICE 'Reviews: % / %', LEAST(current_batch * batch_size, review_count), review_count;
            COMMIT;
        END LOOP;

        DROP TABLE IF EXISTS temp_product_ids;
    END IF;

    -- ==============================================
    -- 5. Восстанавливаем индексы
    -- ==============================================
    RAISE NOTICE 'Creating indexes...';

    CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
    CREATE INDEX IF NOT EXISTS idx_products_slug ON products(slug);
    CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id);
    CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
    CREATE INDEX IF NOT EXISTS idx_reviews_product ON reviews(product_id);
    CREATE INDEX IF NOT EXISTS idx_reviews_user ON reviews(user_id);

    -- ==============================================
    -- 6. Анализ
    -- ==============================================
    RAISE NOTICE 'Analyzing tables...';

    ANALYZE products;
    ANALYZE categories;
    ANALYZE reviews;

    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Generation complete!';
    RAISE NOTICE '==========================================';
END;
$$;

-- Вывод статистики после генерации
CREATE OR REPLACE FUNCTION show_data_stats()
RETURNS TABLE(entity TEXT, count BIGINT)
LANGUAGE sql
AS $$
    SELECT 'Categories', COUNT(*) FROM categories
    UNION ALL
    SELECT 'Products', COUNT(*) FROM products
    UNION ALL
    SELECT 'Reviews', COUNT(*) FROM reviews;
$$;
