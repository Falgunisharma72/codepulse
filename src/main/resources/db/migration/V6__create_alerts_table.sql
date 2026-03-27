CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    analysis_run_id BIGINT REFERENCES analysis_runs(id) ON DELETE CASCADE,
    repository_id BIGINT REFERENCES repositories(id) ON DELETE CASCADE,
    alert_type VARCHAR(50),
    severity VARCHAR(20),
    message TEXT,
    file_path VARCHAR(1000),
    is_resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_alerts_repo_unresolved ON alerts(repository_id, is_resolved) WHERE is_resolved = FALSE;
CREATE INDEX idx_analysis_runs_repo_status ON analysis_runs(repository_id, status);
CREATE INDEX idx_metrics_analysis_run ON metrics(analysis_run_id);
CREATE INDEX idx_file_metrics_analysis_run ON file_metrics(analysis_run_id);
