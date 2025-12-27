#!/bin/bash

# BlackBerry Z10 Connection Setup Script
# This script helps set up a convenient connection between your laptop and Z10

set -e

echo "=== BlackBerry Z10 Connection Setup ==="
echo ""

# Check if running as root for some operations
if [ "$EUID" -ne 0 ]; then 
    echo "Note: Some operations may require sudo privileges"
    echo ""
fi

# Function to check USB connection
check_usb() {
    echo "1. Checking USB connection..."
    if lsusb | grep -i "BlackBerry\|Research\|RIM" > /dev/null; then
        echo "   ✓ BlackBerry device detected via USB"
        DEVICE=$(lsusb | grep -i "BlackBerry\|Research\|RIM")
        echo "   Device: $DEVICE"
        return 0
    else
        echo "   ✗ No BlackBerry device detected via USB"
        echo ""
        echo "   USB Troubleshooting:"
        echo "   - Make sure USB cable is connected"
        echo "   - On Z10: Settings → Storage and Access → USB Connection"
        echo "   - Try different USB modes: 'Mass Storage', 'Internet Tethering', or 'Charge Only'"
        echo "   - Try a different USB cable/port"
        echo "   - Install usbutils if missing: sudo apt install usbutils"
        return 1
    fi
}

# Function to setup WiFi hotspot
setup_hotspot() {
    echo ""
    echo "2. Setting up WiFi Hotspot (Recommended Method)"
    echo ""
    
    if ! command -v nmcli &> /dev/null; then
        echo "   ✗ nmcli not found. Install NetworkManager:"
        echo "     sudo apt install network-manager"
        return 1
    fi
    
    read -p "   Create WiFi hotspot? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        return 1
    fi
    
    HOTSPOT_NAME="BlackBerry-Z10"
    HOTSPOT_PASSWORD="blackberry123"
    
    echo "   Creating hotspot: $HOTSPOT_NAME"
    echo "   Password: $HOTSPOT_PASSWORD"
    echo ""
    
    # Check if hotspot already exists
    if nmcli connection show "$HOTSPOT_NAME" &> /dev/null; then
        echo "   Hotspot '$HOTSPOT_NAME' already exists"
        read -p "   Delete and recreate? (y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            sudo nmcli connection delete "$HOTSPOT_NAME" 2>/dev/null || true
        else
            echo "   Using existing hotspot"
            return 0
        fi
    fi
    
    # Get WiFi interface
    WIFI_IFACE=$(nmcli -t -f DEVICE,TYPE device | grep wifi | cut -d: -f1 | head -1)
    if [ -z "$WIFI_IFACE" ]; then
        echo "   ✗ No WiFi interface found"
        return 1
    fi
    
    echo "   Using interface: $WIFI_IFACE"
    
    # Create hotspot
    sudo nmcli connection add type wifi ifname "$WIFI_IFACE" con-name "$HOTSPOT_NAME" \
        autoconnect yes ssid "$HOTSPOT_NAME" \
        wifi.mode ap wifi-sec.key-mgmt wpa-psk \
        wifi-sec.psk "$HOTSPOT_PASSWORD" \
        ipv4.method shared
    
    echo ""
    echo "   ✓ Hotspot created!"
    echo ""
    echo "   Next steps:"
    echo "   1. Connect your Z10 to WiFi network: $HOTSPOT_NAME"
    echo "   2. Password: $HOTSPOT_PASSWORD"
    echo "   3. Get hotspot IP: ./get-connection-info.sh"
    echo ""
    
    read -p "   Start hotspot now? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        sudo nmcli connection up "$HOTSPOT_NAME"
        echo "   ✓ Hotspot started!"
    fi
}

# Function to get connection info
get_connection_info() {
    echo ""
    echo "3. Connection Information"
    echo ""
    
    # Get IP address (prefer WiFi, exclude Docker)
    IP=$(ip addr show wlp61s0 2>/dev/null | grep -E "inet.*192\.168\.|inet.*10\.|inet.*150\." | awk '{print $2}' | cut -d/ -f1 | head -1)
    
    if [ -z "$IP" ]; then
        IP=$(ip addr show | grep -E "inet.*192\.168\.|inet.*10\.|inet.*150\." | grep -v "127.0.0.1" | grep -v "172.17" | awk '{print $2}' | cut -d/ -f1 | head -1)
    fi
    
    if [ -z "$IP" ]; then
        IP=$(hostname -I 2>/dev/null | awk '{print $1}')
    fi
    
    if [ -z "$IP" ]; then
        echo "   ✗ Could not determine IP address"
        return 1
    fi
    
    echo "   Your laptop IP: $IP"
    echo ""
    echo "   Z10 Browser URLs:"
    echo "   - Home:        http://$IP:8080/today"
    echo "   - Checklist:   http://$IP:8080/checklist?week=2025-W1"
    echo "   - Navigator:   http://$IP:8080/navigator"
    echo ""
    
    # Check if server is running
    if netstat -tlnp 2>/dev/null | grep 8080 > /dev/null || ss -tlnp 2>/dev/null | grep 8080 > /dev/null; then
        echo "   ✓ Server is running on port 8080"
    else
        echo "   ⚠ Server is NOT running"
        echo "   Start it with: java -jar target/project-blackberry-server-1.0-SNAPSHOT.jar"
    fi
}

# Main menu
echo "Choose an option:"
echo "  1) Check USB connection"
echo "  2) Setup WiFi hotspot (recommended)"
echo "  3) Get connection info"
echo "  4) All of the above"
echo ""
read -p "Enter choice (1-4): " choice

case $choice in
    1)
        check_usb
        ;;
    2)
        setup_hotspot
        ;;
    3)
        get_connection_info
        ;;
    4)
        check_usb
        setup_hotspot
        get_connection_info
        ;;
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac

echo ""
echo "=== Setup Complete ==="

