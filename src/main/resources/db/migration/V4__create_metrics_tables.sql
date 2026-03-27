CREATE TABLE metrics (
    id BIGSERIAL PRIMARY KEY,
    analysis_run_id BIGINT REFERENCES analysis_runs(id) ON DELETE CASCADE,
    total_files INT,
    total_lines INT,
    avg_cyclomatic_complexity DOUBLE PRECISION,
    max_cyclomatic_complexity DOUBLE PRECISION,
    avg_method_length DOUBLE PRECISION,
    max_method_length INT,
    duplicate_block_count INT,
    code_smell_count INT,
    test_file_count INT,
    test_coverage_estimate DOUBLE PRECISION,
    overall_health_score DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE file_metrics (
    id BIGSERIAL PRIMARY KEY,
    analysis_run_id BIGINT REFERENCES analysis_runs(id) ON DELETE CASCADE,
    file_path VARCHAR(1000) NOT NULL,
    language VARCHAR(50),
    lines_of_code INT,
    cyclomatic_complexity INT,
    method_count INT,
    avg_method_length DOUBLE PRECISION,
    max_method_length INT,
    has_long_methods BOOLEAN DEFAULT FALSE,
    has_deep_nesting BOOLEAN DEFAULT FALSE,
    nesting_depth INT,
    created_at TIMESTAMP DEFAULT NOW()
);
