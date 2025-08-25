-- Migration: V1__CreateSpeedTestTables
-- Description: Create tables for speed test functionality
-- Date: 2024-01-01

-- Create speed_test_servers table
CREATE TABLE IF NOT EXISTS speed_test_servers (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    location VARCHAR(255) NOT NULL,
    country VARCHAR(100) NOT NULL,
    ip VARCHAR(45) DEFAULT '', -- IPv4/IPv6 address for Android compatibility
    is_active BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Speed test results are not stored (processed in real-time only)
CREATE INDEX IF NOT EXISTS idx_speed_test_servers_active ON speed_test_servers(is_active);
CREATE INDEX IF NOT EXISTS idx_speed_test_servers_priority ON speed_test_servers(priority);

-- Insert default servers
INSERT INTO speed_test_servers (id, name, host, port, location, country, ip, is_active, priority) VALUES
    ('osaka', 'Osaka VPN Server', 'osaka.myvpn.com', 443, 'Osaka', 'Japan', '15.168.240.118', true, 1),
    ('paris', 'Paris VPN Server', 'paris.myvpn.com', 443, 'Paris', 'France', '52.47.190.220', true, 2)
ON CONFLICT (id) DO NOTHING;

-- Add foreign key constraint (optional, for referential integrity)
-- ALTER TABLE speed_test_results ADD CONSTRAINT fk_speed_test_results_server 
--     FOREIGN KEY (server_id) REFERENCES speed_test_servers(id);
