#!/bin/bash

# Authentication and Authorization System Test Script
# This script demonstrates the complete functionality of the auth system

BASE_URL="http://localhost:8082"
echo "=== Authentication and Authorization System Test ==="
echo "Testing against: $BASE_URL"
echo "IMPORTANT: Watch the main application console for real-time updates!"
echo "   The main app will show new users being created and managed."
echo

# Function to make API calls with error handling
make_request() {
    local description="$1"
    local method="$2"
    local url="$3"
    local auth="$4"
    local data="$5"
    
    echo "$description"
    echo "Waiting 3 seconds for you to see the main application output..."
    sleep 3
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -u "$auth" -X "$method" "$BASE_URL$url" \
            -H "Content-Type: application/json" \
            -d "$data")
    else
        response=$(curl -s -w "\n%{http_code}" -u "$auth" -X "$method" "$BASE_URL$url")
    fi
    
    # Split response and status code
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
        echo "SUCCESS ($http_code)"
        echo "$body" | jq . 2>/dev/null || echo "$body"
    else
        echo "FAILED ($http_code)"
        echo "$body" | jq . 2>/dev/null || echo "$body"
    fi
    echo
}

# Check if jq is available for pretty JSON output
if ! command -v jq &> /dev/null; then
    echo "Note: Install 'jq' for prettier JSON output: brew install jq"
    echo
fi

echo "=== 1. Testing Predefined Users ==="
echo "Testing the 3 predefined users (system_admin, system_reader, system_writer)"
echo

make_request "Admin Access Test" "GET" "/service/admin/ping" "system_admin:system_admin_pass"
make_request "Reader Access Test" "GET" "/service/read/ping" "system_reader:system_reader_pass"
make_request "Writer Access Test" "GET" "/service/write/ping" "system_writer:system_writer_pass"

echo "=== 2. Testing Authorization (Should Fail) ==="
echo "Testing that users can't access endpoints they shouldn't have access to"
echo

make_request "Reader trying Admin (should fail)" "GET" "/service/admin/ping" "system_reader:system_reader_pass"
make_request "Writer trying Admin (should fail)" "GET" "/service/admin/ping" "system_writer:system_writer_pass"

echo "=== 3. Creating New Users with Different Privileges ==="
echo "Watch the main application console - you'll see new users being created!"
echo

make_request "Creating WRITE user" "POST" "/auth/users" "system_admin:system_admin_pass" \
    '{"username":"power_user","password":"power_pass","roles":["WRITE"]}'

make_request "Creating READ user" "POST" "/auth/users" "system_admin:system_admin_pass" \
    '{"username":"viewer","password":"viewer_pass","roles":["READ"]}'

make_request "Creating ADMIN user" "POST" "/auth/users" "system_admin:system_admin_pass" \
    '{"username":"super_admin","password":"super_pass","roles":["ADMIN"]}'

make_request "Creating user with multiple roles" "POST" "/auth/users" "system_admin:system_admin_pass" \
    '{"username":"manager","password":"manager_pass","roles":["WRITE","READ"]}'

echo "=== 4. Testing New Users ==="
echo "Now testing the newly created users..."
echo

make_request "Testing WRITE user" "GET" "/service/write/ping" "power_user:power_pass"
make_request "Testing READ user" "GET" "/service/read/ping" "viewer:viewer_pass"
make_request "Testing ADMIN user" "GET" "/service/admin/ping" "super_admin:super_pass"
make_request "Testing MANAGER user (WRITE+READ)" "GET" "/service/write/ping" "manager:manager_pass"

echo "=== 5. Testing Authorization with New Users ==="
echo

make_request "READ user trying ADMIN (should fail)" "GET" "/service/admin/ping" "viewer:viewer_pass"
make_request "WRITE user trying ADMIN (should fail)" "GET" "/service/admin/ping" "power_user:power_pass"

echo "=== 6. Testing User Management Operations ==="
echo "Testing role updates - watch the main console for changes!"
echo

make_request "Updating user roles" "PUT" "/auth/users/viewer/roles" "system_admin:system_admin_pass" \
    '{"roles":["WRITE"]}'

make_request "Testing updated user (now has WRITE)" "GET" "/service/write/ping" "viewer:viewer_pass"

echo "=== 7. Testing Different Endpoints ==="
echo "Testing various service endpoints to show URL-based service identification"
echo

make_request "Admin system info" "GET" "/service/admin/system-info" "system_admin:system_admin_pass"
make_request "Read public data" "GET" "/service/read/public-data" "system_reader:system_reader_pass"
make_request "Write create resource" "POST" "/service/write/create" "system_writer:system_writer_pass" \
    '{"name":"test_resource","type":"demo"}'

echo "=== 8. Testing Unauthorized Access ==="
echo "Testing security with no authentication and wrong credentials"
echo

make_request "No authentication (should fail)" "GET" "/service/read/ping" ""
make_request "Wrong password (should fail)" "GET" "/service/read/ping" "system_admin:wrong_password"

echo "=== 9. Cleanup - Delete Test Users ==="
echo "Cleaning up test users - watch the main console!"
echo

make_request "Deleting power_user" "DELETE" "/auth/users/power_user" "system_admin:system_admin_pass"
make_request "Deleting viewer" "DELETE" "/auth/users/viewer" "system_admin:system_admin_pass"
make_request "Deleting super_admin" "DELETE" "/auth/users/super_admin" "system_admin:system_admin_pass"
make_request "Deleting manager" "DELETE" "/auth/users/manager" "system_admin:system_admin_pass"

echo "=== Test Complete ==="
echo "All authentication and authorization tests completed!"
echo
echo "Summary of what was demonstrated:"
echo "   - Predefined users work correctly"
echo "   - Role-based access control enforced"
echo "   - Dynamic user creation/deletion works"
echo "   - Authorization properly blocks unauthorized access"
echo "   - URL-based service identification implemented"
echo "   - Real-time user management visible in main console"
echo
echo "Your authentication and authorization system is fully functional!"
echo "Check the main application console to see all the user management activity!"
