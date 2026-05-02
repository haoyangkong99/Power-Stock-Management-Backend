CREATE TYPE transaction_type AS ENUM ('IN', 'OUT', 'TRANSFER');
CREATE TYPE sync_status_enum AS ENUM ('SUCCESS', 'FAILED', 'IN_PROGRESS');
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(512) NOT NULL,
    location_id BIGINT,                    -- no REFERENCES yet
    permission_mask BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_location
    FOREIGN KEY (location_id) REFERENCES locations(id);

CREATE TABLE permission_sets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    permission_mask BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
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
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

CREATE TABLE units (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    symbol VARCHAR(50) NOT NULL UNIQUE,
    base_unit_id BIGINT REFERENCES units(id),
    conversion_factor DECIMAL(19, 4) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

CREATE TABLE item_units (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id),
    unit_id BIGINT NOT NULL REFERENCES units(id),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    UNIQUE(item_id, unit_id)
);

CREATE TABLE unit_conversions (
    id BIGSERIAL PRIMARY KEY,
    from_unit_id BIGINT NOT NULL REFERENCES units(id),
    to_unit_id BIGINT NOT NULL REFERENCES units(id),
    conversion_factor DECIMAL(19, 4) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    CHECK (from_unit_id != to_unit_id)
);

CREATE TABLE inventory_stocks (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id),
    location_id BIGINT NOT NULL REFERENCES locations(id),
    current_quantity INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    UNIQUE(item_id, location_id)
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    type transaction_type NOT NULL,
    item_id BIGINT NOT NULL REFERENCES items(id),
    quantity INT NOT NULL,
    unit_id BIGINT NOT NULL REFERENCES units(id),
    unit_price DECIMAL(19, 4) NOT NULL DEFAULT 0,
    total_value DECIMAL(19, 4) NOT NULL DEFAULT 0,
    user_id BIGINT NOT NULL REFERENCES users(id),
    location_from BIGINT REFERENCES locations(id),
    location_to BIGINT NOT NULL REFERENCES locations(id),
    synced BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

CREATE TABLE price_history (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id),
    old_price DECIMAL(19, 4) NOT NULL,
    new_price DECIMAL(19, 4) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    changed_by BIGINT NOT NULL REFERENCES users(id),
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

CREATE TABLE inventory_valuations (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id),
    location_id BIGINT NOT NULL REFERENCES locations(id),
    quantity INT NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    total_value DECIMAL(19, 4) NOT NULL,
    calculated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

CREATE TABLE sync_status (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    last_sync_time TIMESTAMP,
    status sync_status_enum NOT NULL DEFAULT 'SUCCESS',
    pending_operations INT NOT NULL DEFAULT 0,
    last_error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

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