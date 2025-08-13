-- ===========================================
-- DUMMY DATA for the schema from init.sql
-- ===========================================

-- ---------- STORES CATEGORY (5) ----------
INSERT INTO p_stores_category (stores_category_id, stores_category)
VALUES
    (gen_random_uuid(), '한식'),
    (gen_random_uuid(), '중식'),
    (gen_random_uuid(), '피자'),
    (gen_random_uuid(), '치킨'),
    (gen_random_uuid(), '버거');

-- ---------- REGIONS (30) ----------
INSERT INTO p_regions (region_id, region_1depth_name, region_2depth_name, region_3depth_name)
SELECT gen_random_uuid(),
       'Seoul', 'Gu-' || lpad(gs::text, 2, '0'), 'Dong-' || lpad((gs % 10 + 1)::text, 2, '0')
FROM generate_series(1,30) AS gs;

-- ======================================================
-- USERS: CUSTOMERS(10), OWNERS(10), ADMINS(3)
-- password 평문은 모두 0000
-- 해시 더미(60자) 예: '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu'
-- ======================================================

-- ---------- CUSTOMERS (10) ----------
INSERT INTO p_customers (customer_id, username, password, name, birth, email, is_banned)
VALUES
(gen_random_uuid(), 'cust01', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '고객1', '1990-01-01', 'cust01@example.com', false),
(gen_random_uuid(), 'cust02', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '고객2', '1991-02-02', 'cust02@example.com', false),
(gen_random_uuid(), 'cust03', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '고객3', '1992-03-03', 'cust03@example.com', false),
(gen_random_uuid(), 'cust04', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '고객4', '1993-04-04', 'cust04@example.com', false),
(gen_random_uuid(), 'cust05', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '고객5', '1994-05-05', 'cust05@example.com', false),
(gen_random_uuid(), 'cust06', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '고객6', '1995-06-06', 'cust06@example.com', false),
(gen_random_uuid(), 'cust07', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '고객7', '1996-07-07', 'cust07@example.com', false),
(gen_random_uuid(), 'cust08', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '고객8', '1997-08-08', 'cust08@example.com', false),
(gen_random_uuid(), 'cust09', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '고객9', '1998-09-09', 'cust09@example.com', false),
(gen_random_uuid(), 'cust10', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '고객10','1999-10-10', 'cust10@example.com', false);

-- ---------- OWNERS (10) ----------
INSERT INTO p_owners (owner_id, username, password, name, birth, email, is_banned)
VALUES
(gen_random_uuid(), 'owner01', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '사장1', '1980-01-01', 'owner01@example.com', false),
(gen_random_uuid(), 'owner02', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '사장2', '1981-02-02', 'owner02@example.com', false),
(gen_random_uuid(), 'owner03', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '사장3', '1982-03-03', 'owner03@example.com', false),
(gen_random_uuid(), 'owner04', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '사장4', '1983-04-04', 'owner04@example.com', false),
(gen_random_uuid(), 'owner05', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '사장5', '1984-05-05', 'owner05@example.com', false),
(gen_random_uuid(), 'owner06', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '사장6', '1985-06-06', 'owner06@example.com', false),
(gen_random_uuid(), 'owner07', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '사장7', '1986-07-07', 'owner07@example.com', false),
(gen_random_uuid(), 'owner08', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '사장8', '1987-08-08', 'owner08@example.com', false),
(gen_random_uuid(), 'owner09', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '사장9', '1988-09-09', 'owner09@example.com', false),
(gen_random_uuid(), 'owner10', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '사장10','1989-10-10', 'owner10@example.com', false);

-- ---------- ADMINS (3) ----------
INSERT INTO p_admin (admin_id, username, password, name, birth, email, is_banned)
VALUES
(gen_random_uuid(), 'admin01', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '관리자1', '1975-01-01', 'admin01@example.com', false),
(gen_random_uuid(), 'admin02', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '관리자2', '1976-02-02', 'admin02@example.com', false),
(gen_random_uuid(), 'admin03', '$2a$10$QvsSRN8CKCM9LO/Lx96IZuXh6yVsQHS0pWUlwgkwdzQ13KoQXA5Lu', '관리자3', '1977-03-03', 'admin03@example.com', false);

-- ======================================================
-- ADDRESS: 고객당 1건
-- ======================================================
INSERT INTO p_customer_address (address_id, customer_id, address_name, address1, address2, zip_cd, user_latitude, user_longitude, is_default)
SELECT gen_random_uuid(), c.customer_id,
       '집', '서울시 어딘가 ' || right(c.username,2), '101동 100' || (row_number() over (order by c.customer_id))::text,
       lpad((10000 + row_number() over (order by c.customer_id))::text, 6, '0'),
       37.50 + (row_number() over (order by c.customer_id))*0.001,
       127.00 + (row_number() over (order by c.customer_id))*0.001,
       true
FROM p_customers c;

-- ======================================================
-- STORES: 사장당 1건, 카테고리 5건, 매장-카테고리 연결, 지역 30건, 매장-지역 연결
-- ======================================================

-- 사장당 1개 매장
WITH cat AS (
    SELECT sc.stores_category_id,
           row_number() OVER (ORDER BY sc.stores_category_id) AS rn
    FROM p_stores_category sc
),
     owners AS (
         SELECT o.owner_id, o.username,
                row_number() OVER (ORDER BY o.owner_id) AS rn
         FROM p_owners o
     )
INSERT INTO p_stores (
    store_id, owner_id, stores_category_id, store_name, store_description,
    address1, address2, zip_cd, store_phone, store_latitude, store_longitude,
    open_time, close_time, is_banned
)
SELECT
    gen_random_uuid(),
    o.owner_id,
    c.stores_category_id,                         -- 카테고리 매칭
    '가게' || RIGHT(o.username, 2),
    '맛있는 집 ' || RIGHT(o.username, 2),
    '서울 어딘가 ' || RIGHT(o.username, 2),
    '지하 1층',
    LPAD((20000 + o.rn)::text, 6, '0'),    -- o.rn 재사용
    '02-1234-56' || LPAD(o.rn::text, 2, '0'),
    37.35 + o.rn * 0.002,
    127.05 + o.rn * 0.002,
    '09:00'::time, '21:00'::time, false
FROM owners o
         JOIN cat c
              ON c.rn = ((o.rn - 1) % 5) + 1;     -- 윈도우 함수 대신 사전계산 rn 사용

-- 매장-지역 매핑: 각 매장을 3개 지역에 연결
WITH r AS (
    SELECT region_id, row_number() over (order by region_id) AS rn FROM p_regions
),
     s AS (
         SELECT store_id, row_number() over (order by store_id) AS rn FROM p_stores
     )
INSERT INTO p_stores_regions (store_region_id, store_id, region_id)
SELECT gen_random_uuid(), s.store_id, r.region_id
FROM s
         JOIN r ON r.rn IN ( ((s.rn-1)*3 % 30) + 1, ((s.rn-1)*3 % 30) + 2, ((s.rn-1)*3 % 30) + 3 );

-- 매장당 메뉴 카테고리 5개
INSERT INTO p_menu_category (menu_category_id, store_id, menu_category_name)
SELECT gen_random_uuid(), s.store_id, 'Category ' || gs
FROM p_stores s
         CROSS JOIN generate_series(1,5) AS gs;

-- ======================================================
-- MENUS: 총 30건 (앞에서 만든 매장/카테고리와 연결)
--  각 메뉴는 매장의 카테고리 중 하나에 매핑
-- ======================================================
WITH target_stores AS (
    SELECT store_id
    FROM p_stores
    ORDER BY store_id
    LIMIT 10   -- 10개 매장 * 3메뉴 = 30메뉴
),
     target AS (
         SELECT ts.store_id, gs AS seq
         FROM target_stores ts
                  CROSS JOIN generate_series(1,3) AS gs
     ),
     pick_cat AS (
         SELECT mc.store_id, mc.menu_category_id, mc.menu_category_name,
                row_number() over (partition by mc.store_id order by mc.menu_category_name) AS rn
         FROM p_menu_category mc
     )
INSERT INTO p_menus (menu_id, store_id, menu_category_id, menu_name, menu_price, menu_description, is_public, menu_photo_url, is_public_photo)
SELECT gen_random_uuid(), t.store_id,
       (SELECT menu_category_id FROM pick_cat pc WHERE pc.store_id = t.store_id AND pc.rn = ((t.seq-1) % 5) + 1),
       '메뉴' || (row_number() over (order by t.store_id, t.seq))::text,
       5000 + (t.seq * 500),
       '아주 맛있는 메뉴',
       true,
       NULL,
       NULL
FROM target t;

-- ======================================================
-- CARTS (3) & CART ITEMS (각 3개)
-- ======================================================
-- 카트 3건: 고객 1~3, 상점 1~3
WITH cust AS (
    SELECT customer_id, username, row_number() OVER (ORDER BY customer_id) AS rn
    FROM p_customers
),
     stor AS (
         SELECT store_id, row_number() OVER (ORDER BY store_id) AS rn
         FROM p_stores
     ),
     ins_carts AS (  -- 여기서 carts 3건을 만들고 RETURNING으로 결과를 CTE에 담음 (테이블 아님!)
         INSERT INTO p_carts (cart_id, customer_id, store_id)
             SELECT gen_random_uuid(), c.customer_id, s.store_id
             FROM cust c
                      JOIN stor s ON s.rn = c.rn
             WHERE c.rn <= 3
             RETURNING cart_id, customer_id, store_id
     ),
     pick_menu AS (  -- 매장별 메뉴에 순번 부여 (CTE)
         SELECT m.store_id, m.menu_id,
                row_number() OVER (PARTITION BY m.store_id ORDER BY m.menu_id) AS rn
         FROM p_menus m
     )
-- 각 카트당 아이템 3개: 해당 매장의 메뉴 3개 선택
INSERT INTO p_cart_items (cart_item_id, cart_id, menu_id, quantity)
SELECT
    gen_random_uuid(),
    ic.cart_id,
    pm.menu_id,
    ((pm.rn - 1) % 3) + 1         -- 각 카트에 3개씩, 수량은 1~3 순환
FROM ins_carts ic
         JOIN pick_menu pm ON pm.store_id = ic.store_id
WHERE pm.rn <= 3;                -- "카트당 아이템 3개" 제한


-- ======================================================
-- ORDERS (3) & ORDER ITEMS (각 3개)
-- ======================================================
WITH cust AS (
    SELECT c.customer_id, ca.address_id, row_number() over (order by c.customer_id) AS rn
    FROM p_customers c
             JOIN p_customer_address ca ON ca.customer_id = c.customer_id
),
     stor AS (
         SELECT store_id, row_number() over (order by store_id) AS rn FROM p_stores
     ),
     ins_orders AS (
         INSERT INTO p_orders (order_id, customer_id, store_id, address_id, total_price, request_message)
             SELECT gen_random_uuid(), c.customer_id, s.store_id, c.address_id,
                    0, '빨리 부탁해요'
             FROM cust c
                      JOIN stor s ON s.rn = c.rn
             WHERE c.rn <= 3
             RETURNING order_id, customer_id, store_id
     ),
     pick_menu AS (
         SELECT m.store_id, m.menu_id,
                row_number() over (partition by m.store_id order by m.menu_id) AS rn
         FROM p_menus m
     )
INSERT INTO p_order_items (order_item_id, order_id, menu_id, quantity)
SELECT gen_random_uuid(), io.order_id, pm.menu_id, ((pm.rn - 1) % 3) + 1
FROM ins_orders io
         JOIN pick_menu pm ON pm.store_id = io.store_id
WHERE pm.rn <= 3;

-- 주문 합계(total_price) 갱신 (아이템 기준 합)
UPDATE p_orders o
SET total_price = sub.sum_price
FROM (
         SELECT oi.order_id, SUM(m.menu_price * oi.quantity) AS sum_price
         FROM p_order_items oi
                  JOIN p_menus m ON m.menu_id = oi.menu_id
         GROUP BY oi.order_id
     ) sub
WHERE o.order_id = sub.order_id;

-- ======================================================
-- REVIEWS: 각 메뉴당 1건 (총 30건), 고객을 순환 배정
-- ======================================================
WITH cust AS (
    SELECT customer_id, row_number() over (order by customer_id) AS rn, count(*) over() AS total
    FROM p_customers
),
     menu AS (
         SELECT menu_id, store_id, row_number() over (order by menu_id) AS rn
         FROM p_menus
     )
INSERT INTO p_reviews (review_id, customer_id, menu_id, rating, comment, is_public)
SELECT gen_random_uuid(),
       (SELECT customer_id FROM cust WHERE rn = ((menu.rn - 1) % (SELECT total FROM cust LIMIT 1)) + 1),
       menu.menu_id,
       ((menu.rn - 1) % 5) + 1,
       '맛있어요!',
       true
FROM menu;

-- REVIEW AVERAGE: 리뷰 기반 집계
INSERT INTO p_review_average (store_avg_id, store_id, count, total)
SELECT gen_random_uuid(), m.store_id, COUNT(r.review_id) AS cnt, SUM(r.rating) AS ttl
FROM p_reviews r
         JOIN p_menus m ON m.menu_id = r.menu_id
GROUP BY m.store_id;

-- ======================================================
-- PAYMENTS, ERRORS : (요청대로 비움)
-- ======================================================

-- 끝.
