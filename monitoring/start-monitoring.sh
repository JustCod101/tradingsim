#!/bin/bash

# TradingSim Monitoring Stack Startup Script

echo "🚀 Starting TradingSim Monitoring Stack..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Navigate to monitoring directory
cd "$(dirname "$0")"

# Stop any existing containers
echo "🛑 Stopping existing monitoring containers..."
docker-compose down

# Start the monitoring stack
echo "📊 Starting Prometheus, Grafana, and Node Exporter..."
docker-compose up -d

# Wait for services to start
echo "⏳ Waiting for services to start..."
sleep 10

# Check if services are running
echo "🔍 Checking service status..."

if docker ps | grep -q "tradingsim-prometheus"; then
    echo "✅ Prometheus is running on http://localhost:9090"
else
    echo "❌ Prometheus failed to start"
fi

if docker ps | grep -q "tradingsim-grafana"; then
    echo "✅ Grafana is running on http://localhost:3000"
    echo "   Default credentials: admin/admin"
else
    echo "❌ Grafana failed to start"
fi

if docker ps | grep -q "tradingsim-node-exporter"; then
    echo "✅ Node Exporter is running on http://localhost:9100"
else
    echo "❌ Node Exporter failed to start"
fi

echo ""
echo "📈 Monitoring Stack Status:"
echo "   - Prometheus: http://localhost:9090"
echo "   - Grafana: http://localhost:3000 (admin/admin)"
echo "   - Node Exporter: http://localhost:9100"
echo ""
echo "📋 Available Dashboards:"
echo "   - TradingSim Overview: System metrics and performance"
echo "   - TradingSim Business: User activity and game metrics"
echo ""
echo "🔧 To stop the monitoring stack, run:"
echo "   docker-compose down"
echo ""
echo "📝 To view logs, run:"
echo "   docker-compose logs -f [service_name]"