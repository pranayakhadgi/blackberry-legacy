#!/bin/bash

# Import weekly checklist script
# Usage: ./import-weekly.sh [week-id]
# Example: ./import-weekly.sh 2025-W1

WEEK="${1:-2025-W1}"
JSON_FILE="data/weekly-plan-${WEEK}.json"

if [ ! -f "$JSON_FILE" ]; then
    echo "Error: JSON file not found: $JSON_FILE"
    echo "Usage: ./import-weekly.sh [week-id]"
    exit 1
fi

echo "Importing checklist for week: $WEEK"
echo "From file: $JSON_FILE"

curl -X POST "http://localhost:8080/import?week=$WEEK" \
  -H "Content-Type: application/json" \
  -d @"$JSON_FILE"

echo ""
echo "Import complete. Checklist saved to disk."
echo ""
echo "View at:"
echo "  http://localhost:8080/checklist?week=$WEEK"
echo "  Or: http://localhost:8080/checklist (auto-detects current week)"
echo ""
echo "On Z10:"
IP=$(./get-connection-info.sh 2>/dev/null | grep "Laptop IP" | awk '{print $4}' || echo "YOUR_IP")
echo "  http://$IP:8080/checklist?week=$WEEK"
echo "  Or: http://$IP:8080/checklist"

