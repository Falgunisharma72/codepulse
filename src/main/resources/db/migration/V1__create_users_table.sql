CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    github_username VARCHAR(255) UNIQUE NOT NULL,
    access_token VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);
