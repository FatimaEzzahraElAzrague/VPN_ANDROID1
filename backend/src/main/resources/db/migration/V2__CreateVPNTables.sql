-- Migration: V2__CreateVPNTables
-- Description: Create tables for VPN functionality
-- Date: 2024-01-01

-- Create vpn_servers table
CREATE TABLE IF NOT EXISTS vpn_servers (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    country_code VARCHAR(10) NOT NULL,
    flag VARCHAR(10) NOT NULL,
    ip VARCHAR(45) NOT NULL,
    port INTEGER NOT NULL,
    subnet VARCHAR(50) NOT NULL,
    server_ip VARCHAR(45) NOT NULL,
    dns_servers TEXT NOT NULL,
    wireguard_public_key VARCHAR(255) NOT NULL,
    wireguard_endpoint VARCHAR(255) NOT NULL,
    allowed_ips VARCHAR(255) NOT NULL,
    mtu INTEGER DEFAULT 1420,
    is_active BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create vpn_client_configs table
CREATE TABLE IF NOT EXISTS vpn_client_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    server_id VARCHAR(100) NOT NULL,
    private_key VARCHAR(255) NOT NULL,
    public_key VARCHAR(255) NOT NULL,
    address VARCHAR(50) NOT NULL,
    dns VARCHAR(255) NOT NULL,
    mtu INTEGER DEFAULT 1420,
    allowed_ips VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP
);

-- Create vpn_connections table
CREATE TABLE IF NOT EXISTS vpn_connections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    server_id VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    connected_at TIMESTAMP,
    disconnected_at TIMESTAMP,
    last_error TEXT,
    bytes_received BIGINT DEFAULT 0,
    bytes_sent BIGINT DEFAULT 0,
    connection_duration BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_vpn_servers_active ON vpn_servers(is_active);
CREATE INDEX IF NOT EXISTS idx_vpn_servers_priority ON vpn_servers(priority);
CREATE INDEX IF NOT EXISTS idx_vpn_client_configs_user_server ON vpn_client_configs(user_id, server_id);
CREATE INDEX IF NOT EXISTS idx_vpn_connections_user_server ON vpn_connections(user_id, server_id);
CREATE INDEX IF NOT EXISTS idx_vpn_connections_status ON vpn_connections(status);

-- Insert default VPN servers
INSERT INTO vpn_servers (id, name, city, country, country_code, flag, ip, port, subnet, server_ip, dns_servers, wireguard_public_key, wireguard_endpoint, allowed_ips, mtu, is_active, priority) VALUES
    ('osaka', 'Osaka VPN Server', 'Osaka', 'Japan', 'JP', '🇯🇵', '15.168.240.118', 51820, '10.0.1.0/24', '10.0.1.1', '["1.1.1.1", "8.8.8.8"]', 'YOUR_OSAKA_SERVER_PUBLIC_KEY_HERE', '15.168.240.118:51820', '0.0.0.0/0,::/0', 1420, true, 1),
    ('paris', 'Paris VPN Server', 'Paris', 'France', 'FR', '🇫🇷', '52.47.190.220', 51820, '10.0.2.0/24', '10.0.2.1', '["1.1.1.1", "8.8.8.8"]', 'YOUR_PARIS_SERVER_PUBLIC_KEY_HERE', '52.47.190.220:51820', '0.0.0.0/0,::/0', 1420, true, 2)
ON CONFLICT (id) DO NOTHING;
