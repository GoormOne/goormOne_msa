-- UUID 생성 함수 사용
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =====================
-- ENUMS
-- =====================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'role_type') THEN
CREATE TYPE role_type AS ENUM ('CUSTOMER', 'OWNER', 'ADMIN');
END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_method') THEN
CREATE TYPE payment_method AS ENUM ('CARD');
END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_result') THEN
CREATE TYPE payment_result AS ENUM ('SUCCESS', 'CANCELED', 'FAILED');
END IF;
END$$;

-- =====================
-- USER DOMAIN
-- =====================
CREATE TABLE IF NOT EXISTS p_customers (
                                           customer_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    username    varchar(10) NOT NULL UNIQUE,
    password    varchar(60) NOT NULL,
    name        varchar(10) NOT NULL,
    birth       date        NOT NULL,
    email       varchar(30) NOT NULL UNIQUE,
    is_banned   boolean     NOT NULL DEFAULT false
    );

CREATE TABLE IF NOT EXISTS p_owners (
                                        owner_id   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    username   varchar(10) NOT NULL UNIQUE,
    password   varchar(60) NOT NULL,
    name       varchar(10) NOT NULL,
    birth      date        NOT NULL,
    email      varchar(30) NOT NULL UNIQUE,
    is_banned  boolean     NOT NULL DEFAULT false
    );

CREATE TABLE IF NOT EXISTS p_admin (
                                       admin_id  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    username  varchar(10) NOT NULL UNIQUE,
    password  varchar(60) NOT NULL,
    name      varchar(10) NOT NULL,
    birth     date        NOT NULL,
    email     varchar(30) NOT NULL UNIQUE,
    is_banned boolean     NOT NULL DEFAULT false
    );

-- =====================
-- ADDRESS
-- =====================
CREATE TABLE IF NOT EXISTS p_customer_address (
                                                  address_id     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id    uuid NOT NULL REFERENCES p_customers(customer_id),
    address_name   varchar(20) NOT NULL,
    address1       varchar(50) NOT NULL,
    address2       varchar(50) NOT NULL,
    zip_cd         varchar(6)  NOT NULL,
    user_latitude  numeric(10,6) NOT NULL,
    user_longitude numeric(10,6) NOT NULL,
    is_default     boolean NOT NULL
    );

-- =====================
-- STORE DOMAIN
-- =====================
CREATE TABLE IF NOT EXISTS p_stores_category (
                                                 stores_category_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    stores_category    varchar(30) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS p_regions (
                                         region_id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    region_1depth_name varchar(50) NOT NULL,
    region_2depth_name varchar(50) NOT NULL,
    region_3depth_name varchar(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS p_stores (
                                        store_id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id           uuid NOT NULL REFERENCES p_owners(owner_id),
    stores_category_id uuid NOT NULL REFERENCES p_stores_category(stores_category_id),
    store_name         varchar(30) NOT NULL,
    store_description  text NOT NULL,
    address1           varchar(50) NOT NULL,
    address2           varchar(50) NOT NULL,
    zip_cd             varchar(6)  NOT NULL,
    store_phone        varchar(15) NOT NULL,
    store_latitude     numeric(10,6) NOT NULL,
    store_longitude    numeric(10,6) NOT NULL,
    open_time          time NOT NULL,
    close_time         time NOT NULL,
    is_banned          boolean NOT NULL DEFAULT false
    );

CREATE TABLE IF NOT EXISTS p_stores_regions (
                                                store_region_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        uuid NOT NULL REFERENCES p_stores(store_id),
    region_id       uuid NOT NULL REFERENCES p_regions(region_id),
    UNIQUE (store_id, region_id)
    );

-- =====================
-- MENU DOMAIN
-- =====================
CREATE TABLE IF NOT EXISTS p_menu_category (
                                               menu_category_id   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id           uuid NOT NULL REFERENCES p_stores(store_id),
    menu_category_name varchar(50) NOT NULL,
    UNIQUE (store_id, menu_category_name)
    );

CREATE TABLE IF NOT EXISTS p_menus (
                                       menu_id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id         uuid NOT NULL REFERENCES p_stores(store_id),
    menu_category_id uuid NOT NULL REFERENCES p_menu_category(menu_category_id),
    menu_name        varchar(20) NOT NULL,
    menu_price       int NOT NULL,
    menu_description text NOT NULL,
    is_public        boolean NOT NULL,
    menu_photo_url   varchar(100),
    is_public_photo  boolean
    );

-- =====================
-- CART DOMAIN
-- =====================
CREATE TABLE IF NOT EXISTS p_carts (
                                       cart_id     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id uuid NOT NULL REFERENCES p_customers(customer_id),
    store_id    uuid NOT NULL REFERENCES p_stores(store_id)
    );

CREATE TABLE IF NOT EXISTS p_cart_items (
                                            cart_item_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id      uuid NOT NULL REFERENCES p_carts(cart_id),
    menu_id      uuid NOT NULL REFERENCES p_menus(menu_id),
    quantity     int  NOT NULL
    );

-- =====================
-- ORDER DOMAIN
-- =====================
CREATE TABLE IF NOT EXISTS p_orders (
                                        order_id       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id    uuid NOT NULL REFERENCES p_customers(customer_id),
    store_id       uuid NOT NULL REFERENCES p_stores(store_id),
    address_id     uuid NOT NULL REFERENCES p_customer_address(address_id),
    total_price    int  NOT NULL,
    request_message text
    );

CREATE TABLE IF NOT EXISTS p_order_items (
                                             order_item_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id      uuid NOT NULL REFERENCES p_orders(order_id),
    menu_id       uuid NOT NULL REFERENCES p_menus(menu_id),
    quantity      int  NOT NULL
    );

-- =====================
-- REVIEW DOMAIN
-- =====================
CREATE TABLE IF NOT EXISTS p_reviews (
                                         review_id   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id uuid NOT NULL REFERENCES p_customers(customer_id),
    menu_id     uuid NOT NULL REFERENCES p_menus(menu_id),
    rating      smallint NOT NULL,
    comment     text,
    is_public   boolean NOT NULL
    );

CREATE TABLE IF NOT EXISTS p_review_average (
                                                store_avg_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id     uuid NOT NULL REFERENCES p_stores(store_id),
    count        int,
    total        int
    );

-- =====================
-- PAYMENT DOMAIN
-- =====================
CREATE TABLE IF NOT EXISTS p_payments (
                                          payment_id     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id       uuid NOT NULL UNIQUE REFERENCES p_orders(order_id),
    payment_method payment_method NOT NULL,
    card_number    varchar(16) NOT NULL,
    payment_amount int NOT NULL,
    payment_time   timestamp NOT NULL,
    payment_result payment_result NOT NULL,
    failure_reason text
    );

-- =====================
-- ERROR LOG
-- =====================
CREATE TABLE IF NOT EXISTS p_errors (
                                        error_id     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id  uuid REFERENCES p_customers(customer_id),
    request_url  varchar(255) NOT NULL,
    http_method  varchar(10)  NOT NULL,
    error_code   varchar(20)  NOT NULL,
    error_message text        NOT NULL,
    client_ip    varchar(45),
    user_agent   text,
    created_at   timestamp NOT NULL DEFAULT now()
    );

-- =====================
-- AUDIT TABLES (공통 스키마/순서)
--  * audit_id는 해당 도메인 PK와 1:1 (PK=FK)
--  * created_by/updated_by/deleted_by : FK 비강제 (UUID만 저장)
--  * updated_* / deleted_* : 삼중 체크 제약
-- =====================

-- CUSTOMER
CREATE TABLE IF NOT EXISTS p_customer_audit (
                                                audit_id         uuid PRIMARY KEY REFERENCES p_customers(customer_id),
    created_at       timestamptz NOT NULL DEFAULT now(),
    created_by       uuid        NOT NULL REFERENCES p_customers(customer_id),
    updated_at       timestamptz,
    updated_by       uuid,
    updated_by_type  role_type,
    deleted_at       timestamptz,
    deleted_by       uuid,
    deleted_by_type  role_type,
    deleted_rs       varchar(255),
    CONSTRAINT chk_customer_audit_updated
    CHECK (
(updated_at IS NULL AND updated_by IS NULL AND updated_by_type IS NULL)
    OR
(updated_at IS NOT NULL AND updated_by IS NOT NULL AND updated_by_type IS NOT NULL)
    ),
    CONSTRAINT chk_customer_audit_deleted
    CHECK (
(deleted_at IS NULL AND deleted_by IS NULL AND deleted_by_type IS NULL)
    OR
(deleted_at IS NOT NULL AND deleted_by IS NOT NULL AND deleted_by_type IS NOT NULL)
    )
    );

-- OWNER
CREATE TABLE IF NOT EXISTS p_owner_audit (
                                             audit_id         uuid PRIMARY KEY REFERENCES p_owners(owner_id),
    created_at       timestamptz NOT NULL DEFAULT now(),
    created_by       uuid        NOT NULL REFERENCES p_owners(owner_id),
    updated_at       timestamptz,
    updated_by       uuid,
    updated_by_type  role_type,
    deleted_at       timestamptz,
    deleted_by       uuid,
    deleted_by_type  role_type,
    deleted_rs       varchar(255),
    CONSTRAINT chk_owner_audit_updated
    CHECK (
(updated_at IS NULL AND updated_by IS NULL AND updated_by_type IS NULL)
    OR
(updated_at IS NOT NULL AND updated_by IS NOT NULL AND updated_by_type IS NOT NULL)
    ),
    CONSTRAINT chk_owner_audit_deleted
    CHECK (
(deleted_at IS NULL AND deleted_by IS NULL AND deleted_by_type IS NULL)
    OR
(deleted_at IS NOT NULL AND deleted_by IS NOT NULL AND deleted_by_type IS NOT NULL)
    )
    );

-- ADMIN
CREATE TABLE IF NOT EXISTS p_admin_audit (
                                             audit_id         uuid PRIMARY KEY REFERENCES p_admin(admin_id),
    created_at       timestamptz NOT NULL DEFAULT now(),
    created_by       uuid        NOT NULL REFERENCES p_admin(admin_id),
    updated_at       timestamptz,
    updated_by       uuid,
    updated_by_type  role_type,
    deleted_at       timestamptz,
    deleted_by       uuid,
    deleted_by_type  role_type,
    deleted_rs       varchar(255),
    CONSTRAINT chk_admin_audit_updated
    CHECK (
(updated_at IS NULL AND updated_by IS NULL AND updated_by_type IS NULL)
    OR
(updated_at IS NOT NULL AND updated_by IS NOT NULL AND updated_by_type IS NOT NULL)
    ),
    CONSTRAINT chk_admin_audit_deleted
    CHECK (
(deleted_at IS NULL AND deleted_by IS NULL AND deleted_by_type IS NULL)
    OR
(deleted_at IS NOT NULL AND deleted_by IS NOT NULL AND deleted_by_type IS NOT NULL)
    )
    );

-- CUSTOMER ADDRESS
CREATE TABLE IF NOT EXISTS p_customer_address_audit (
                                                        audit_id         uuid PRIMARY KEY REFERENCES p_customer_address(address_id),
    created_at       timestamptz NOT NULL DEFAULT now(),
    created_by       uuid        NOT NULL REFERENCES p_customers(customer_id),
    updated_at       timestamptz,
    updated_by       uuid,
    updated_by_type  role_type,
    deleted_at       timestamptz,
    deleted_by       uuid,
    deleted_by_type  role_type,
    deleted_rs       varchar(255),
    CONSTRAINT chk_customer_addr_audit_updated
    CHECK (
(updated_at IS NULL AND updated_by IS NULL AND updated_by_type IS NULL)
    OR
(updated_at IS NOT NULL AND updated_by IS NOT NULL AND updated_by_type IS NOT NULL)
    ),
    CONSTRAINT chk_customer_addr_audit_deleted
    CHECK (
(deleted_at IS NULL AND deleted_by IS NULL AND deleted_by_type IS NULL)
    OR
(deleted_at IS NOT NULL AND deleted_by IS NOT NULL AND deleted_by_type IS NOT NULL)
    )
    );

-- STORE
CREATE TABLE IF NOT EXISTS p_store_audit (
                                             audit_id         uuid PRIMARY KEY REFERENCES p_stores(store_id),
    created_at       timestamptz NOT NULL DEFAULT now(),
    created_by       uuid        NOT NULL REFERENCES p_owners(owner_id),
    updated_at       timestamptz,
    updated_by       uuid,
    updated_by_type  role_type,
    deleted_at       timestamptz,
    deleted_by       uuid,
    deleted_by_type  role_type,
    deleted_rs       varchar(255),
    CONSTRAINT chk_store_audit_updated
    CHECK (
(updated_at IS NULL AND updated_by IS NULL AND updated_by_type IS NULL)
    OR
(updated_at IS NOT NULL AND updated_by IS NOT NULL AND updated_by_type IS NOT NULL)
    ),
    CONSTRAINT chk_store_audit_deleted
    CHECK (
(deleted_at IS NULL AND deleted_by IS NULL AND deleted_by_type IS NULL)
    OR
(deleted_at IS NOT NULL AND deleted_by IS NOT NULL AND deleted_by_type IS NOT NULL)
    )
    );

-- MENU
CREATE TABLE IF NOT EXISTS p_menu_audit (
                                            audit_id         uuid PRIMARY KEY REFERENCES p_menus(menu_id),
    created_at       timestamptz NOT NULL DEFAULT now(),
    created_by       uuid        NOT NULL REFERENCES p_owners(owner_id),
    updated_at       timestamptz,
    updated_by       uuid,
    updated_by_type  role_type,
    deleted_at       timestamptz,
    deleted_by       uuid,
    deleted_by_type  role_type,
    deleted_rs       varchar(255),
    CONSTRAINT chk_menu_audit_updated
    CHECK (
(updated_at IS NULL AND updated_by IS NULL AND updated_by_type IS NULL)
    OR
(updated_at IS NOT NULL AND updated_by IS NOT NULL AND updated_by_type IS NOT NULL)
    ),
    CONSTRAINT chk_menu_audit_deleted
    CHECK (
(deleted_at IS NULL AND deleted_by IS NULL AND deleted_by_type IS NULL)
    OR
(deleted_at IS NOT NULL AND deleted_by IS NOT NULL AND deleted_by_type IS NOT NULL)
    )
    );

-- ORDER
CREATE TABLE IF NOT EXISTS p_order_audit (
                                             audit_id         uuid PRIMARY KEY REFERENCES p_orders(order_id),
    created_at       timestamptz NOT NULL DEFAULT now(),
    created_by       uuid        NOT NULL REFERENCES p_customers(customer_id),
    updated_at       timestamptz,
    updated_by       uuid,
    updated_by_type  role_type,
    deleted_at       timestamptz,
    deleted_by       uuid,
    deleted_by_type  role_type,
    deleted_rs       varchar(255),
    CONSTRAINT chk_order_audit_updated
    CHECK (
(updated_at IS NULL AND updated_by IS NULL AND updated_by_type IS NULL)
    OR
(updated_at IS NOT NULL AND updated_by IS NOT NULL AND updated_by_type IS NOT NULL)
    ),
    CONSTRAINT chk_order_audit_deleted
    CHECK (
(deleted_at IS NULL AND deleted_by IS NULL AND deleted_by_type IS NULL)
    OR
(deleted_at IS NOT NULL AND deleted_by IS NOT NULL AND deleted_by_type IS NOT NULL)
    )
    );

-- ORDER ITEM
CREATE TABLE IF NOT EXISTS p_order_item_audit (
                                                  audit_id         uuid PRIMARY KEY REFERENCES p_order_items(order_item_id),
    created_at       timestamptz NOT NULL DEFAULT now(),
    created_by       uuid        NOT NULL REFERENCES p_customers(customer_id),
    updated_at       timestamptz,
    updated_by       uuid,
    updated_by_type  role_type,
    deleted_at       timestamptz,
    deleted_by       uuid,
    deleted_by_type  role_type,
    deleted_rs       varchar(255),
    CONSTRAINT chk_order_item_audit_updated
    CHECK (
(updated_at IS NULL AND updated_by IS NULL AND updated_by_type IS NULL)
    OR
(updated_at IS NOT NULL AND updated_by IS NOT NULL AND updated_by_type IS NOT NULL)
    ),
    CONSTRAINT chk_order_item_audit_deleted
    CHECK (
(deleted_at IS NULL AND deleted_by IS NULL AND deleted_by_type IS NULL)
    OR
(deleted_at IS NOT NULL AND deleted_by IS NOT NULL AND deleted_by_type IS NOT NULL)
    )
    );

-- REVIEW
CREATE TABLE IF NOT EXISTS p_review_audit (
                                              audit_id         uuid PRIMARY KEY REFERENCES p_reviews(review_id),
    created_at       timestamptz NOT NULL DEFAULT now(),
    created_by       uuid        NOT NULL REFERENCES p_customers(customer_id),
    updated_at       timestamptz,
    updated_by       uuid,
    updated_by_type  role_type,
    deleted_at       timestamptz,
    deleted_by       uuid,
    deleted_by_type  role_type,
    deleted_rs       varchar(255),
    CONSTRAINT chk_review_audit_updated
    CHECK (
(updated_at IS NULL AND updated_by IS NULL AND updated_by_type IS NULL)
    OR
(updated_at IS NOT NULL AND updated_by IS NOT NULL AND updated_by_type IS NOT NULL)
    ),
    CONSTRAINT chk_review_audit_deleted
    CHECK (
(deleted_at IS NULL AND deleted_by IS NULL AND deleted_by_type IS NULL)
    OR
(deleted_at IS NOT NULL AND deleted_by IS NOT NULL AND deleted_by_type IS NOT NULL)
    )
    );

-- PAYMENT
CREATE TABLE IF NOT EXISTS p_payment_audit (
                                               audit_id         uuid PRIMARY KEY REFERENCES p_payments(payment_id),
    created_at       timestamptz NOT NULL DEFAULT now(),
    created_by       uuid        NOT NULL REFERENCES p_customers(customer_id),
    updated_at       timestamptz,
    updated_by       uuid,
    updated_by_type  role_type,
    deleted_at       timestamptz,
    deleted_by       uuid,
    deleted_by_type  role_type,
    deleted_rs       varchar(255),
    CONSTRAINT chk_payment_audit_updated
    CHECK (
(updated_at IS NULL AND updated_by IS NULL AND updated_by_type IS NULL)
    OR
(updated_at IS NOT NULL AND updated_by IS NOT NULL AND updated_by_type IS NOT NULL)
    ),
    CONSTRAINT chk_payment_audit_deleted
    CHECK (
(deleted_at IS NULL AND deleted_by IS NULL AND deleted_by_type IS NULL)
    OR
(deleted_at IS NOT NULL AND deleted_by IS NOT NULL AND deleted_by_type IS NOT NULL)
    )
    );

-- 권장 인덱스 (조회 패턴에 맞춰 선택적으로 추가)
-- CREATE INDEX IF NOT EXISTS idx_p_customer_audit_created_at ON p_customer_audit(created_at);
-- CREATE INDEX IF NOT EXISTS idx_p_store_audit_created_at ON p_store_audit(created_at);
-- ... 필요시 나머지도 동일하게 추가