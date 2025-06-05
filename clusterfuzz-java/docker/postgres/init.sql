-- ClusterFuzz PostgreSQL Initialization Script
-- This script sets up the initial database schema and configuration

-- Create additional databases for testing
CREATE DATABASE clusterfuzz_test;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE clusterfuzz TO clusterfuzz;
GRANT ALL PRIVILEGES ON DATABASE clusterfuzz_test TO clusterfuzz;

-- Connect to main database
\c clusterfuzz;

-- Create schema for application
CREATE SCHEMA IF NOT EXISTS clusterfuzz;
GRANT ALL ON SCHEMA clusterfuzz TO clusterfuzz;

-- Set default search path
ALTER DATABASE clusterfuzz SET search_path TO clusterfuzz, public;

-- Create indexes for performance (will be created by JPA, but good to have)
-- These will be created automatically by Hibernate, but we can prepare them

-- Performance tuning settings
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
ALTER SYSTEM SET random_page_cost = 1.1;
ALTER SYSTEM SET effective_io_concurrency = 200;

-- Reload configuration
SELECT pg_reload_conf();