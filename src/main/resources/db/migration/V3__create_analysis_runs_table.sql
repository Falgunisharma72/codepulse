CREATE TABLE analysis_runs (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT REFERENCES repositories(id) ON DELETE CASCADE,
    commit_sha VARCHAR(40) NOT NULL,
    commit_message TEXT,
    author VARCHAR(255),
    branch VARCHAR(255),
    status VARCHAR(20) DEFAULT 'QUEUED',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
