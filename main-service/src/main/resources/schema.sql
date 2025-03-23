-- Таблица пользователей
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(250) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE
);

-- Таблица событий
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    annotation TEXT NOT NULL,
    description TEXT NOT NULL,
    category_id BIGINT NOT NULL,
    event_date TIMESTAMP NOT NULL,
    created_on TIMESTAMP NOT NULL,
    published_on TIMESTAMP,
    initiator_id BIGINT NOT NULL,
    location_lat FLOAT NOT NULL,
    location_lon FLOAT NOT NULL,
    paid BOOLEAN NOT NULL,
    participant_limit INT DEFAULT 0,
    request_moderation BOOLEAN DEFAULT TRUE,
    state VARCHAR(20) NOT NULL
);

-- Таблица заявок на участие
CREATE TABLE participation_requests (
    id BIGSERIAL PRIMARY KEY,
    created TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    requester_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL
);

-- Внешние ключи
ALTER TABLE participation_requests
ADD CONSTRAINT fk_participation_user
FOREIGN KEY (requester_id) REFERENCES users(id);

ALTER TABLE participation_requests
ADD CONSTRAINT fk_participation_event
FOREIGN KEY (event_id) REFERENCES events(id);