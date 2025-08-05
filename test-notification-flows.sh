#!/bin/bash

# Complete Notification System Test Script
# Tests all notification flows end-to-end

echo "🔔 Starting Complete Notification System Tests"
echo "=============================================="

# Configuration
BASE_URL="http://localhost"
USER_SERVICE_PORT="8081"
TICKET_SERVICE_PORT="8082"
ASSIGNMENT_SERVICE_PORT="8083"
NOTIFICATIONS_SERVICE_PORT="8084"

# Test data
MANAGER_EMAIL="manager@test.com"
TECHNICIAN_EMAIL="tech@test.com"
USER_EMAIL="user@test.com"

echo "📋 Test Configuration:"
echo "  User Service: ${BASE_URL}:${USER_SERVICE_PORT}"
echo "  Ticket Service: ${BASE_URL}:${TICKET_SERVICE_PORT}"
echo "  Assignment Service: ${BASE_URL}:${ASSIGNMENT_SERVICE_PORT}"
echo "  Notifications Service: ${BASE_URL}:${NOTIFICATIONS_SERVICE_PORT}"
echo ""

# Function to make HTTP requests with error handling
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local headers=$4
    
    echo "🌐 ${method} ${url}"
    
    if [ -n "$data" ]; then
        if [ -n "$headers" ]; then
            curl -s -X ${method} "${url}" -H "Content-Type: application/json" -H "${headers}" -d "${data}"
        else
            curl -s -X ${method} "${url}" -H "Content-Type: application/json" -d "${data}"
        fi
    else
        if [ -n "$headers" ]; then
            curl -s -X ${method} "${url}" -H "${headers}"
        else
            curl -s -X ${method} "${url}"
        fi
    fi
}

# Test 1: Health Checks
echo "🏥 Test 1: Service Health Checks"
echo "--------------------------------"

echo "Checking User Service..."
make_request "GET" "${BASE_URL}:${USER_SERVICE_PORT}/api/public/assignment/health"
echo ""

echo "Checking Ticket Service..."
make_request "GET" "${BASE_URL}:${TICKET_SERVICE_PORT}/api/tickets/health"
echo ""

echo "Checking Notifications Service..."
make_request "GET" "${BASE_URL}:${NOTIFICATIONS_SERVICE_PORT}/api/notifications/health"
echo ""

# Test 2: Manager Lookup Test
echo "👨‍💼 Test 2: Manager Lookup"
echo "----------------------------"

# This would need actual team IDs from the database
echo "Testing manager lookup endpoint..."
echo "Note: Replace {teamId} with actual team ID from database"
echo "curl -X GET '${BASE_URL}:${USER_SERVICE_PORT}/api/public/assignment/teams/{teamId}/manager'"
echo ""

# Test 3: Technician Info Test
echo "🔧 Test 3: Technician Info Lookup"
echo "----------------------------------"

echo "Testing technician info endpoint..."
echo "Note: Replace {technicianId} with actual technician ID from database"
echo "curl -X GET '${BASE_URL}:${USER_SERVICE_PORT}/api/public/assignment/technicians/{technicianId}/info'"
echo ""

# Test 4: Notification Creation Test
echo "📬 Test 4: Notification System"
echo "------------------------------"

echo "Testing notification endpoints..."
echo "Note: Replace {userId} with actual user ID from database"
echo "curl -X GET '${BASE_URL}:${NOTIFICATIONS_SERVICE_PORT}/api/notifications/user/{userId}'"
echo ""

# Test 5: Event Flow Simulation
echo "🔄 Test 5: Event Flow Simulation"
echo "--------------------------------"

echo "To test the complete notification flow:"
echo ""
echo "1. Create a ticket (as user):"
echo "   - Login to frontend as user"
echo "   - Create a new ticket"
echo "   - Verify ticket gets assigned to technician"
echo "   - Check manager dashboard for assignment notification"
echo ""
echo "2. Add technician note (as technician):"
echo "   - Login to frontend as technician"
echo "   - Add work note to assigned ticket"
echo "   - Check user dashboard for note notification"
echo ""
echo "3. Change ticket status (as technician):"
echo "   - Start working on ticket (NOUVEAU → OUVERT)"
echo "   - Resolve ticket (OUVERT → RESOLU)"
echo "   - Check user dashboard for status change notifications"
echo ""

# Test 6: Frontend Integration Test
echo "🖥️  Test 6: Frontend Integration"
echo "--------------------------------"

echo "Frontend testing steps:"
echo ""
echo "1. Manager Dashboard:"
echo "   - Open http://localhost:4200/manager-dashboard"
echo "   - Login as manager"
echo "   - Check notification bell in top-right"
echo "   - Click bell to see notification dropdown"
echo "   - Verify notifications appear and can be marked as read"
echo ""
echo "2. User Dashboard:"
echo "   - Open http://localhost:4200/user-dashboard"
echo "   - Login as user"
echo "   - Check for notifications about ticket updates"
echo ""

# Test 7: Database Verification
echo "🗄️  Test 7: Database Verification"
echo "---------------------------------"

echo "Check database tables for notification data:"
echo ""
echo "1. Connect to notifications database:"
echo "   psql -h localhost -U itsm_user -d notifications_db"
echo ""
echo "2. Check notifications table:"
echo "   SELECT * FROM notifications ORDER BY created_at DESC LIMIT 10;"
echo ""
echo "3. Check notification preferences:"
echo "   SELECT * FROM notification_preferences;"
echo ""

# Test 8: Kafka Event Verification
echo "📡 Test 8: Kafka Event Verification"
echo "-----------------------------------"

echo "Monitor Kafka topics for events:"
echo ""
echo "1. List topics:"
echo "   kafka-topics --bootstrap-server localhost:9092 --list"
echo ""
echo "2. Monitor assignment events:"
echo "   kafka-console-consumer --bootstrap-server localhost:9092 --topic assignment.created --from-beginning"
echo ""
echo "3. Monitor ticket events:"
echo "   kafka-console-consumer --bootstrap-server localhost:9092 --topic ticket.note.added --from-beginning"
echo "   kafka-console-consumer --bootstrap-server localhost:9092 --topic ticket.status.changed --from-beginning"
echo ""

echo "✅ Notification System Test Guide Complete!"
echo ""
echo "🎯 Success Criteria:"
echo "  ✅ All services are healthy and responding"
echo "  ✅ Manager receives notifications for team assignments"
echo "  ✅ Users receive notifications for notes and status changes"
echo "  ✅ Frontend displays notifications correctly"
echo "  ✅ Real-time updates work via WebSocket"
echo "  ✅ Notifications can be marked as read"
echo ""
echo "📝 Next Steps:"
echo "  1. Start all microservices"
echo "  2. Start frontend application"
echo "  3. Follow the test scenarios above"
echo "  4. Verify each notification type works end-to-end"
echo ""
echo "🐛 Troubleshooting:"
echo "  - Check service logs for errors"
echo "  - Verify Kafka is running and topics exist"
echo "  - Check database connections"
echo "  - Verify WebSocket connections in browser dev tools"
