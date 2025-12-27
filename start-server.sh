#!/bin/bash

# Start BlackBerry Z10 Server
# This script builds and starts the server, then shows connection info

set -e

echo "=== Starting BlackBerry Z10 Server ==="
echo ""

# Check if JAR exists, build if not
if [ ! -f "target/project-blackberry-server-1.0-SNAPSHOT.jar" ]; then
    echo "Building project..."
    mvn clean package -q
    echo "✓ Build complete"
    echo ""
fi

# Get IP address for display (prefer WiFi, exclude Docker)
IP=$(ip addr show wlp61s0 2>/dev/null | grep -E "inet.*192\.168\.|inet.*10\.|inet.*150\." | awk '{print $2}' | cut -d/ -f1 | head -1)

if [ -z "$IP" ]; then
    IP=$(ip addr show | grep -E "inet.*192\.168\.|inet.*10\.|inet.*150\." | grep -v "127.0.0.1" | grep -v "172.17" | awk '{print $2}' | cut -d/ -f1 | head -1)
fi

if [ -z "$IP" ]; then
    IP=$(hostname -I 2>/dev/null | awk '{print $1}')
fi

if [ -z "$IP" ]; then
    IP="YOUR_IP_HERE"
fi

echo "Server will be accessible at:"
echo "  http://$IP:8080/today"
echo "  http://$IP:8080/checklist?week=2025-W1"
echo "  http://$IP:8080/navigator"
echo ""
echo "Starting server..."
echo "Press Ctrl+C to stop"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Start server
java -jar target/project-blackberry-server-1.0-SNAPSHOT.jar

