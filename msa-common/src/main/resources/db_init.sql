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

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_status') THEN
        CREATE TYPE order_status AS ENUM ('PENDING', 'CONFIRMED', 'COOKING', 'DELIVERING', 'COMPLETED', 'CANCELED');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_status') THEN
        CREATE TYPE payment_status AS ENUM ('PENDING', 'PAID', 'FAILED', 'REFUNDED');
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
                                           email_verified  boolean NOT NULL DEFAULT false,
                                           is_banned   boolean     NOT NULL DEFAULT false
);
-- unique 제약(중복 방지)
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_p_customers_username') THEN
        ALTER TABLE p_customers ADD CONSTRAINT uk_p_customers_username UNIQUE (username);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_p_customers_email') THEN
        ALTER TABLE p_customers ADD CONSTRAINT uk_p_customers_email UNIQUE (email);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS p_owners (
                                        owner_id   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                        username   varchar(10) NOT NULL UNIQUE,
                                        password   varchar(60) NOT NULL,
                                        name       varchar(10) NOT NULL,
                                        birth      date        NOT NULL,
                                        email      varchar(30) NOT NULL UNIQUE,
                                        email_verified  boolean NOT NULL DEFAULT false,
                                        is_banned  boolean     NOT NULL DEFAULT false
);
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_p_owners_username') THEN
        ALTER TABLE p_owners ADD CONSTRAINT uk_p_owners_username UNIQUE (username);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_p_owners_email') THEN
        ALTER TABLE p_owners ADD CONSTRAINT uk_p_owners_email UNIQUE (email);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS p_admin (
                                       admin_id  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                       username  varchar(10) NOT NULL UNIQUE,
                                       password  varchar(60) NOT NULL,
                                       name      varchar(10) NOT NULL,
                                       birth     date        NOT NULL,
                                       email     varchar(30) NOT NULL UNIQUE,
                                       email_verified  boolean NOT NULL DEFAULT false,
                                       is_banned boolean     NOT NULL DEFAULT false
);
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_p_admin_username') THEN
        ALTER TABLE p_admin ADD CONSTRAINT uk_p_admin_username UNIQUE (username);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_p_admin_email') THEN
        ALTER TABLE p_admin ADD CONSTRAINT uk_p_admin_email UNIQUE (email);
    END IF;
END $$;

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
                                        order_status   order_status,
                                        payment_status payment_status,
                                        total_price    int  NOT NULL,
                                        request_message text
);

CREATE TABLE IF NOT EXISTS p_order_items (
                                             order_item_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                             order_id      uuid NOT NULL REFERENCES p_orders(order_id),
                                             menu_id       uuid NOT NULL REFERENCES p_menus(menu_id),
                                             menu_name     varchar(20) NOT NULL,
                                             menu_price    int NOT NULL,
                                             line_total    int NOT NULL,
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
                                                payment_id    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                                order_id    uuid NOT NULL REFERENCES p_orders(order_id),
                                                payment_key varchar(100) NOT NULL UNIQUE ,
                                                status  varchar(20) NOT NULL,

                                                card_company         varchar(30),
                                                card_bin             varchar(8),   -- 앞 6~8자리
                                                card_last4           varchar(4),   -- 뒤 4자리
                                                card_number_masked   varchar(25),  -- Toss 제공 마스킹 문자열
                                                payment_amount       int NOT NULL,
                                                currency             varchar(3) DEFAULT 'KRW' NOT NULL,
                                                requested_at         timestamptz,
                                                approved_at          timestamptz,
                                                receipt_url          varchar(300),
                                                approve_no           varchar(32),
                                                issuer_code          varchar(8),
                                                acquirer_code        varchar(8),
                                                is_partial_cancelable boolean DEFAULT false NOT NULL,
                                                payment_result       varchar(30) NOT NULL,
                                                failure_reason       text,
                                                failure_code         varchar(50),
                                                m_id                 varchar(50),
                                                last_transaction_key varchar(64)
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
                                                deleted_at       timestamptz,
                                                deleted_by       uuid,
                                                deleted_rs       varchar(255),
                                                CONSTRAINT chk_customer_audit_updated
                                                    CHECK ((updated_at IS NULL AND updated_by IS NULL) OR
                                                           (updated_at IS NOT NULL AND updated_by IS NOT NULL)),
                                                CONSTRAINT chk_customer_audit_deleted
                                                    CHECK ((deleted_at IS NULL AND deleted_by IS NULL) OR
                                                           (deleted_at IS NOT NULL AND deleted_by IS NOT NULL))
);

-- OWNER
CREATE TABLE IF NOT EXISTS p_owner_audit (
                                             audit_id         uuid PRIMARY KEY REFERENCES p_owners(owner_id),
                                             created_at       timestamptz NOT NULL DEFAULT now(),
                                             created_by       uuid        NOT NULL REFERENCES p_owners(owner_id),
                                             updated_at       timestamptz,
                                             updated_by       uuid,
                                             deleted_at       timestamptz,
                                             deleted_by       uuid,
                                             deleted_rs       varchar(255),
                                             CONSTRAINT chk_owner_audit_updated
                                                 CHECK ((updated_at IS NULL AND updated_by IS NULL) OR
                                                        (updated_at IS NOT NULL AND updated_by IS NOT NULL)),
                                             CONSTRAINT chk_owner_audit_deleted
                                                 CHECK ((deleted_at IS NULL AND deleted_by IS NULL) OR
                                                        (deleted_at IS NOT NULL AND deleted_by IS NOT NULL))
);

-- ADMIN
CREATE TABLE IF NOT EXISTS p_admin_audit (
                                             audit_id         uuid PRIMARY KEY REFERENCES p_admin(admin_id),
                                             created_at       timestamptz NOT NULL DEFAULT now(),
                                             created_by       uuid        NOT NULL REFERENCES p_admin(admin_id),
                                             updated_at       timestamptz,
                                             updated_by       uuid,
                                             deleted_at       timestamptz,
                                             deleted_by       uuid,
                                             deleted_rs       varchar(255),
                                             CONSTRAINT chk_admin_audit_updated
                                                 CHECK ((updated_at IS NULL AND updated_by IS NULL) OR
                                                        (updated_at IS NOT NULL AND updated_by IS NOT NULL)),
                                             CONSTRAINT chk_admin_audit_deleted
                                                 CHECK ((deleted_at IS NULL AND deleted_by IS NULL) OR
                                                        (deleted_at IS NOT NULL AND deleted_by IS NOT NULL))
);

-- CUSTOMER ADDRESS
CREATE TABLE IF NOT EXISTS p_customer_address_audit (
                                                        audit_id         uuid PRIMARY KEY REFERENCES p_customer_address(address_id),
                                                        created_at       timestamptz NOT NULL DEFAULT now(),
                                                        created_by       uuid        NOT NULL REFERENCES p_customers(customer_id),
                                                        updated_at       timestamptz,
                                                        updated_by       uuid,
                                                        deleted_at       timestamptz,
                                                        deleted_by       uuid,
                                                        deleted_rs       varchar(255),
                                                        CONSTRAINT chk_customer_addr_audit_updated
                                                            CHECK ((updated_at IS NULL AND updated_by IS NULL) OR
                                                                   (updated_at IS NOT NULL AND updated_by IS NOT NULL)),
                                                        CONSTRAINT chk_customer_addr_audit_deleted
                                                            CHECK ((deleted_at IS NULL AND deleted_by IS NULL) OR
                                                                   (deleted_at IS NOT NULL AND deleted_by IS NOT NULL))
);

-- STORE
CREATE TABLE IF NOT EXISTS p_store_audit (
                                             audit_id         uuid PRIMARY KEY REFERENCES p_stores(store_id),
                                             created_at       timestamptz NOT NULL DEFAULT now(),
                                             created_by       uuid        NOT NULL REFERENCES p_owners(owner_id),
                                             updated_at       timestamptz,
                                             updated_by       uuid,
                                             deleted_at       timestamptz,
                                             deleted_by       uuid,
                                             deleted_rs       varchar(255),
                                             CONSTRAINT chk_store_audit_updated
                                                 CHECK ((updated_at IS NULL AND updated_by IS NULL) OR
                                                        (updated_at IS NOT NULL AND updated_by IS NOT NULL)),
                                             CONSTRAINT chk_store_audit_deleted
                                                 CHECK ((deleted_at IS NULL AND deleted_by IS NULL) OR
                                                        (deleted_at IS NOT NULL AND deleted_by IS NOT NULL))
);

-- MENU
CREATE TABLE IF NOT EXISTS p_menu_audit (
                                            audit_id         uuid PRIMARY KEY REFERENCES p_menus(menu_id),
                                            created_at       timestamptz NOT NULL DEFAULT now(),
                                            created_by       uuid        NOT NULL REFERENCES p_owners(owner_id),
                                            updated_at       timestamptz,
                                            updated_by       uuid,
                                            deleted_at       timestamptz,
                                            deleted_by       uuid,
                                            deleted_rs       varchar(255),
                                            CONSTRAINT chk_menu_audit_updated
                                                CHECK ((updated_at IS NULL AND updated_by IS NULL) OR
                                                       (updated_at IS NOT NULL AND updated_by IS NOT NULL)),
                                            CONSTRAINT chk_menu_audit_deleted
                                                CHECK ((deleted_at IS NULL AND deleted_by IS NULL) OR
                                                       (deleted_at IS NOT NULL AND deleted_by IS NOT NULL))
);

-- ORDER
CREATE TABLE IF NOT EXISTS p_order_audit (
                                             audit_id         uuid PRIMARY KEY REFERENCES p_orders(order_id),
                                             created_at       timestamptz NOT NULL DEFAULT now(),
                                             created_by       uuid        NOT NULL REFERENCES p_customers(customer_id),
                                             updated_at       timestamptz,
                                             updated_by       uuid,
                                             deleted_at       timestamptz,
                                             deleted_by       uuid,
                                             deleted_rs       varchar(255),
                                             CONSTRAINT chk_order_audit_updated
                                                 CHECK ((updated_at IS NULL AND updated_by IS NULL) OR
                                                        (updated_at IS NOT NULL AND updated_by IS NOT NULL)),
                                             CONSTRAINT chk_order_audit_deleted
                                                 CHECK (
                                                     (deleted_at IS NULL AND deleted_by IS NULL) OR
                                                     (deleted_at IS NOT NULL AND deleted_by IS NOT NULL))
);

-- -- ORDER ITEM
-- CREATE TABLE IF NOT EXISTS p_order_item_audit (
--                                                   audit_id         uuid PRIMARY KEY REFERENCES p_order_items(order_item_id),
--                                                   created_at       timestamptz NOT NULL DEFAULT now(),
--                                                   created_by       uuid        NOT NULL REFERENCES p_customers(customer_id),
--                                                   updated_at       timestamptz,
--                                                   updated_by       uuid,
--                                                   deleted_at       timestamptz,
--                                                   deleted_by       uuid,
--                                                   deleted_rs       varchar(255),
--                                                   CONSTRAINT chk_order_item_audit_updated
--                                                       CHECK ((updated_at IS NULL AND updated_by IS NULL) OR
--                                                              (updated_at IS NOT NULL AND updated_by IS NOT NULL)),
--                                                   CONSTRAINT chk_order_item_audit_deleted
--                                                       CHECK ((deleted_at IS NULL AND deleted_by IS NULL) OR
--                                                              (deleted_at IS NOT NULL AND deleted_by IS NOT NULL))
-- );

-- REVIEW
CREATE TABLE IF NOT EXISTS p_review_audit (
                                              audit_id         uuid PRIMARY KEY REFERENCES p_reviews(review_id),
                                              created_at       timestamptz NOT NULL DEFAULT now(),
                                              created_by       uuid        NOT NULL REFERENCES p_customers(customer_id),
                                              updated_at       timestamptz,
                                              updated_by       uuid,
                                              deleted_at       timestamptz,
                                              deleted_by       uuid,
                                              deleted_rs       varchar(255),
                                              CONSTRAINT chk_review_audit_updated
                                                  CHECK ((updated_at IS NULL AND updated_by IS NULL) OR
                                                         (updated_at IS NOT NULL AND updated_by IS NOT NULL)),
                                              CONSTRAINT chk_review_audit_deleted
                                                  CHECK ((deleted_at IS NULL AND deleted_by IS NULL) OR
                                                         (deleted_at IS NOT NULL AND deleted_by IS NOT NULL))
);

-- PAYMENT
CREATE TABLE IF NOT EXISTS p_payment_audit (
                                               audit_id         uuid PRIMARY KEY REFERENCES p_payments(payment_id),
                                               created_at       timestamptz NOT NULL DEFAULT now(),
                                               created_by       uuid        NOT NULL REFERENCES p_customers(customer_id),
                                               updated_at       timestamptz,
                                               updated_by       uuid,
                                               deleted_at       timestamptz,
                                               deleted_by       uuid,
                                               deleted_rs       varchar(255),
                                               CONSTRAINT chk_payment_audit_updated
                                                   CHECK ((updated_at IS NULL AND updated_by IS NULL) OR
                                                          (updated_at IS NOT NULL AND updated_by IS NOT NULL)),
                                               CONSTRAINT chk_payment_audit_deleted
                                                   CHECK ((deleted_at IS NULL AND deleted_by IS NULL) OR
                                                          (deleted_at IS NOT NULL AND deleted_by IS NOT NULL))
);

-- 권장 인덱스 (조회 패턴에 맞춰 선택적으로 추가)
-- CREATE INDEX IF NOT EXISTS idx_p_customer_audit_created_at ON p_customer_audit(created_at);
-- CREATE INDEX IF NOT EXISTS idx_p_store_audit_created_at ON p_store_audit(created_at);
-- ... 필요시 나머지도 동일하게 추가
