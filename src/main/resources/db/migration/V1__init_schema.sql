CREATE TYPE transaction_type AS ENUM ('IN', 'OUT', 'TRANSFER');
CREATE TYPE sync_status_enum AS ENUM ('SUCCESS', 'FAILED', 'IN_PROGRESS');

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(512) NOT NULL,
    location_id BIGINT,
    permission_mask BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE permission_sets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    permission_mask BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(255),
    base_price DECIMAL(19, 4) NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 0,
    reorder_quantity INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE units (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    symbol VARCHAR(50) NOT NULL UNIQUE,
    base_unit_id BIGINT,
    conversion_factor DECIMAL(19, 4) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE item_units (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT,
    UNIQUE(item_id, unit_id)
);

CREATE TABLE unit_conversions (
    id BIGSERIAL PRIMARY KEY,
    from_unit_id BIGINT NOT NULL,
    to_unit_id BIGINT NOT NULL,
    conversion_factor DECIMAL(19, 4) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT,
    CHECK (from_unit_id != to_unit_id)
);

CREATE TABLE inventory_stocks (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    current_quantity INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT,
    UNIQUE(item_id, location_id)
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    type transaction_type NOT NULL,
    item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_id BIGINT NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL DEFAULT 0,
    total_value DECIMAL(19, 4) NOT NULL DEFAULT 0,
    user_id BIGINT NOT NULL,
    location_from BIGINT,
    location_to BIGINT NOT NULL,
    synced BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE price_history (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    old_price DECIMAL(19, 4) NOT NULL,
    new_price DECIMAL(19, 4) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    changed_by BIGINT NOT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE inventory_valuations (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    total_value DECIMAL(19, 4) NOT NULL,
    calculated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE sync_status (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    last_sync_time TIMESTAMP,
    status sync_status_enum NOT NULL DEFAULT 'SUCCESS',
    pending_operations INT NOT NULL DEFAULT 0,
    last_error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT
);

-- Foreign keys added after all tables exist (required for PgBouncer)
ALTER TABLE users ADD CONSTRAINT fk_users_location FOREIGN KEY (location_id) REFERENCES locations(id);
ALTER TABLE users ADD CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE users ADD CONSTRAINT fk_users_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);
ALTER TABLE locations ADD CONSTRAINT fk_locations_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE locations ADD CONSTRAINT fk_locations_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);
ALTER TABLE permission_sets ADD CONSTRAINT fk_permission_sets_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE permission_sets ADD CONSTRAINT fk_permission_sets_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);
ALTER TABLE items ADD CONSTRAINT fk_items_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE items ADD CONSTRAINT fk_items_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);
ALTER TABLE units ADD CONSTRAINT fk_units_base_unit FOREIGN KEY (base_unit_id) REFERENCES units(id);
ALTER TABLE units ADD CONSTRAINT fk_units_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE units ADD CONSTRAINT fk_units_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);
ALTER TABLE item_units ADD CONSTRAINT fk_item_units_item FOREIGN KEY (item_id) REFERENCES items(id);
ALTER TABLE item_units ADD CONSTRAINT fk_item_units_unit FOREIGN KEY (unit_id) REFERENCES units(id);
ALTER TABLE item_units ADD CONSTRAINT fk_item_units_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE item_units ADD CONSTRAINT fk_item_units_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);
ALTER TABLE unit_conversions ADD CONSTRAINT fk_unit_conversions_from FOREIGN KEY (from_unit_id) REFERENCES units(id);
ALTER TABLE unit_conversions ADD CONSTRAINT fk_unit_conversions_to FOREIGN KEY (to_unit_id) REFERENCES units(id);
ALTER TABLE unit_conversions ADD CONSTRAINT fk_unit_conversions_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE unit_conversions ADD CONSTRAINT fk_unit_conversions_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);
ALTER TABLE inventory_stocks ADD CONSTRAINT fk_inventory_stocks_item FOREIGN KEY (item_id) REFERENCES items(id);
ALTER TABLE inventory_stocks ADD CONSTRAINT fk_inventory_stocks_location FOREIGN KEY (location_id) REFERENCES locations(id);
ALTER TABLE inventory_stocks ADD CONSTRAINT fk_inventory_stocks_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE inventory_stocks ADD CONSTRAINT fk_inventory_stocks_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_item FOREIGN KEY (item_id) REFERENCES items(id);
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_unit FOREIGN KEY (unit_id) REFERENCES units(id);
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_location_from FOREIGN KEY (location_from) REFERENCES locations(id);
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_location_to FOREIGN KEY (location_to) REFERENCES locations(id);
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);
ALTER TABLE price_history ADD CONSTRAINT fk_price_history_item FOREIGN KEY (item_id) REFERENCES items(id);
ALTER TABLE price_history ADD CONSTRAINT fk_price_history_changed_by FOREIGN KEY (changed_by) REFERENCES users(id);
ALTER TABLE price_history ADD CONSTRAINT fk_price_history_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE price_history ADD CONSTRAINT fk_price_history_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);
ALTER TABLE inventory_valuations ADD CONSTRAINT fk_inventory_valuations_item FOREIGN KEY (item_id) REFERENCES items(id);
ALTER TABLE inventory_valuations ADD CONSTRAINT fk_inventory_valuations_location FOREIGN KEY (location_id) REFERENCES locations(id);
ALTER TABLE inventory_valuations ADD CONSTRAINT fk_inventory_valuations_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE inventory_valuations ADD CONSTRAINT fk_inventory_valuations_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);
ALTER TABLE sync_status ADD CONSTRAINT fk_sync_status_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE sync_status ADD CONSTRAINT fk_sync_status_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE sync_status ADD CONSTRAINT fk_sync_status_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);

-- Indexes
CREATE INDEX idx_transactions_item ON transactions(item_id);
CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_transactions_location_to ON transactions(location_to);
CREATE INDEX idx_transactions_created ON transactions(created_at);
CREATE INDEX idx_inventory_stocks_item ON inventory_stocks(item_id);
CREATE INDEX idx_inventory_stocks_location ON inventory_stocks(location_id);
CREATE INDEX idx_items_category ON items(category);
CREATE INDEX idx_items_sku ON items(sku);
CREATE INDEX idx_item_units_item ON item_units(item_id);
CREATE INDEX idx_unit_conversions_from ON unit_conversions(from_unit_id);
CREATE INDEX idx_unit_conversions_to ON unit_conversions(to_unit_id);
