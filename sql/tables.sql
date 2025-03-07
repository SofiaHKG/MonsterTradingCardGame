CREATE TABLE IF NOT EXISTS users (
    username         VARCHAR(50)  PRIMARY KEY,
    password         VARCHAR(255) NOT NULL,
    token            VARCHAR(255),
    coins            INT NOT NULL DEFAULT 20,
    fullname         VARCHAR(100),
    bio              TEXT,
    image            TEXT,
    wins             INT NOT NULL DEFAULT 0,
    losses           INT NOT NULL DEFAULT 0,
    elo              INT NOT NULL DEFAULT 100
    );

CREATE TABLE IF NOT EXISTS packages (
    id SERIAL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS cards (
    id               UUID PRIMARY KEY,
    name             VARCHAR(50) NOT NULL,
    damage           DOUBLE PRECISION NOT NULL,
    package_id       INT REFERENCES packages(id) ON DELETE SET NULL,
    owner            VARCHAR(50) REFERENCES users(username),
    in_deck          BOOLEAN NOT NULL DEFAULT FALSE,
    locked_for_trade BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS trading_deals (
    id            UUID PRIMARY KEY,
    card_to_trade UUID NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    owner         VARCHAR(50) NOT NULL REFERENCES users(username),
    required_type VARCHAR(20) NOT NULL,
    min_damage    DOUBLE PRECISION NOT NULL
);

