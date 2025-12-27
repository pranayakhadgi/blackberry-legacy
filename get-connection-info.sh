#!/bin/bash

# Quick script to get connection information for Z10

echo "=== BlackBerry Z10 Connection Info ==="
echo ""

# Get IP address (prefer WiFi interface, exclude Docker)
# Priority: 192.168.x, 10.x, 150.x (your network), then other private IPs
IP=$(ip addr show wlp61s0 2>/dev/null | grep -E "inet.*192\.168\.|inet.*10\.|inet.*150\." | awk '{print $2}' | cut -d/ -f1 | head -1)

if [ -z "$IP" ]; then
    IP=$(ip addr show | grep -E "inet.*192\.168\.|inet.*10\.|inet.*150\." | grep -v "127.0.0.1" | grep -v "172.17" | awk '{print $2}' | cut -d/ -f1 | head -1)
fi

if [ -z "$IP" ]; then
    IP=$(hostname -I 2>/dev/null | awk '{print $1}')
fi

if [ -z "$IP" ]; then
    echo "⚠ Could not determine IP address"
    echo "Make sure you're connected to a network"
    exit 1
fi

echo "Laptop IP Address: $IP"
echo ""

# Check server status
if netstat -tlnp 2>/dev/null | grep 8080 > /dev/null || ss -tlnp 2>/dev/null | grep 8080 > /dev/null; then
    SERVER_STATUS="✓ Running"
else
    SERVER_STATUS="✗ Not Running"
fi

echo "Server Status: $SERVER_STATUS"
echo ""

echo "Z10 Browser URLs:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "  🏠 Home:        http://$IP:8080"
echo "  📋 Checklist:   http://$IP:8080/checklist?week=2025-W1"
echo "  🔗 Navigator:   http://$IP:8080/navigator"
echo "  ⚙️  Setup Guide: http://$IP:8080/setup"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "💡 TIP: Start with the Home page (/) - it has links to everything!"
echo ""

# Check USB
if lsusb | grep -i "BlackBerry\|Research\|RIM" > /dev/null; then
    echo "✓ USB: BlackBerry device detected"
else
    echo "✗ USB: No device detected"
fi

# Check WiFi hotspot
if nmcli connection show 2>/dev/null | grep -i "BlackBerry\|hotspot" > /dev/null; then
    HOTSPOT=$(nmcli connection show | grep -i "BlackBerry\|hotspot" | head -1 | awk '{print $1}')
    echo "✓ Hotspot: $HOTSPOT (if active)"
fi

echo ""

