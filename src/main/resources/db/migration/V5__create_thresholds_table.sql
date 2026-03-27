CREATE TABLE quality_thresholds (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT REFERENCES repositories(id) ON DELETE CASCADE UNIQUE,
    max_cyclomatic_complexity INT DEFAULT 10,
    max_method_length INT DEFAULT 30,
    max_file_length INT DEFAULT 300,
    max_nesting_depth INT DEFAULT 4,
    min_health_score DOUBLE PRECISION DEFAULT 70.0,
    created_at TIMESTAMP DEFAULT NOW()
);
