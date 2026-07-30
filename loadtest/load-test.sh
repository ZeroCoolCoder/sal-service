#!/bin/bash
# SAL Service Load Test Script
# Usage: ./load-test.sh [concurrent_users] [total_requests]

set -e

BASE_URL="${SAL_URL:-http://localhost:8081}"
CONCURRENT="${1:-10}"
TOTAL="${2:-100}"

echo "=========================================="
echo "SAL Service Load Test"
echo "=========================================="
echo "Base URL: $BASE_URL"
echo "Concurrent Users: $CONCURRENT"
echo "Total Requests: $TOTAL"
echo "=========================================="

# Check if service is running
if ! curl -s "$BASE_URL/api/v1/sal/health" > /dev/null 2>&1; then
    echo "ERROR: SAL service is not running at $BASE_URL"
    exit 1
fi

echo ""
echo "1. HEALTH CHECK LOAD TEST"
echo "--------------------------"
ab -n $TOTAL -c $CONCURRENT -q "$BASE_URL/api/v1/sal/health" 2>/dev/null | grep -E "(Requests per second|Time per request|Failed requests)"

# Create test data file for uploads
UPLOAD_DATA=$(cat <<EOF
{
    "name": "loadtest-file.txt",
    "ownerId": "loadtest-user",
    "type": "TEXT",
    "storageType": "FILESYSTEM",
    "content": "TG9hZCB0ZXN0IGNvbnRlbnQgLSB0aGlzIGlzIGEgdGVzdCBmaWxlIGZvciBwZXJmb3JtYW5jZSB0ZXN0aW5nIG9mIHRoZSBTQUwgc2VydmljZS4="
}
EOF
)

echo "$UPLOAD_DATA" > /tmp/sal-upload-data.json

echo ""
echo "2. UPLOAD LOAD TEST (POST)"
echo "--------------------------"
ab -n $TOTAL -c $CONCURRENT -q -p /tmp/sal-upload-data.json -T "application/json" \
    "$BASE_URL/api/v1/sal/objects/upload" 2>/dev/null | grep -E "(Requests per second|Time per request|Failed requests)"

# Get a sample UUID for read tests
SAMPLE_UUID=$(curl -s -X POST "$BASE_URL/api/v1/sal/objects/upload" \
    -H "Content-Type: application/json" \
    -d "$UPLOAD_DATA" | python3 -c "import sys,json; print(json.load(sys.stdin)['salUuid'])" 2>/dev/null)

if [ -n "$SAMPLE_UUID" ]; then
    echo ""
    echo "3. GET INFO LOAD TEST"
    echo "---------------------"
    ab -n $TOTAL -c $CONCURRENT -q "$BASE_URL/api/v1/sal/objects/$SAMPLE_UUID" 2>/dev/null | grep -E "(Requests per second|Time per request|Failed requests)"

    echo ""
    echo "4. DOWNLOAD LOAD TEST"
    echo "---------------------"
    ab -n $TOTAL -c $CONCURRENT -q "$BASE_URL/api/v1/sal/objects/$SAMPLE_UUID/content" 2>/dev/null | grep -E "(Requests per second|Time per request|Failed requests)"
fi

# Search test
SEARCH_DATA='{"latestOnly":true,"page":0,"size":10}'
echo "$SEARCH_DATA" > /tmp/sal-search-data.json

echo ""
echo "5. SEARCH LOAD TEST (POST)"
echo "--------------------------"
ab -n $TOTAL -c $CONCURRENT -q -p /tmp/sal-search-data.json -T "application/json" \
    "$BASE_URL/api/v1/sal/search" 2>/dev/null | grep -E "(Requests per second|Time per request|Failed requests)"

# Cleanup
rm -f /tmp/sal-upload-data.json /tmp/sal-search-data.json

echo ""
echo "=========================================="
echo "Load Test Complete"
echo "=========================================="
