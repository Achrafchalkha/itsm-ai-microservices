# Complete Notification System Testing Guide

## Overview
This guide tests all notification scenarios implemented in the ITSM system.

## Test Scenarios

### 1. Manager Notifications for Team Assignments

**Scenario**: When a ticket is assigned to a technician, the manager should receive a notification.

**Test Steps**:
1. Create a ticket as a user
2. Verify ticket gets assigned to a technician
3. Check that the manager receives a notification about the assignment

**Expected Result**: Manager sees notification "Ticket assigné à votre équipe"

### 2. User Notifications for Technician Notes

**Scenario**: When a technician adds a note to a ticket, the user should receive a notification.

**Test Steps**:
1. Login as technician
2. Add a work note to an assigned ticket
3. Check that the ticket owner (user) receives a notification

**Expected Result**: User sees notification "Note ajoutée à votre ticket"

### 3. User Notifications for Status Changes

**Scenario**: When a technician changes ticket status, the user should receive a notification.

**Test Steps**:
1. Login as technician
2. Start working on a ticket (status: NOUVEAU → OUVERT)
3. Resolve the ticket (status: OUVERT → RESOLU)
4. Check that the user receives notifications for both status changes

**Expected Result**: 
- User sees "Le technicien a commencé le travail"
- User sees "Le ticket a été résolu"

## Backend Components to Verify

### 1. Ticket Service Event Publishing
- ✅ `TicketEventPublisher` publishes events for:
  - Note additions (`ticket.note.added`)
  - Status changes (`ticket.status.changed`)
  - General updates (`ticket.updated`)

### 2. Notifications Service Event Handling
- ✅ `AssignmentEventListener` handles:
  - Assignment events (technician + manager notifications)
  - Note added events (user notifications)
  - Status changed events (user notifications)

### 3. User Service Manager Lookup
- ✅ `/api/public/assignment/teams/{teamId}/manager` endpoint
- ✅ Returns manager information for team notifications

## Frontend Components to Verify

### 1. Manager Dashboard Notifications
- ✅ Notification bell with unread count
- ✅ Notification dropdown with proper styling
- ✅ Real-time notification updates
- ✅ Mark as read functionality

### 2. User Dashboard Notifications
- ✅ Should work with existing technician dashboard implementation
- ✅ Users should see notifications for their tickets

## Testing Commands

### Start All Services
```bash
# Start user-service
cd itsm-ai-microservices/user-service
./mvnw spring-boot:run

# Start ticket-service  
cd itsm-ai-microservices/ticket-service
./mvnw spring-boot:run

# Start assignment-service
cd itsm-ai-microservices/assignment-service
./mvnw spring-boot:run

# Start notifications-service
cd itsm-ai-microservices/notifications-service
./mvnw spring-boot:run

# Start frontend
cd itsm-frontend-clean
npm start
```

### Test API Endpoints

#### 1. Test Manager Lookup
```bash
# Get manager for team (replace with actual team ID)
curl -X GET "http://localhost:8081/api/public/assignment/teams/{teamId}/manager"
```

#### 2. Test Technician Info
```bash
# Get technician info (replace with actual technician ID)
curl -X GET "http://localhost:8081/api/public/assignment/technicians/{technicianId}/info"
```

#### 3. Test Notifications
```bash
# Get notifications for user (replace with actual user ID)
curl -X GET "http://localhost:8084/api/notifications/user/{userId}"
```

## Verification Checklist

### Backend Verification
- [ ] Kafka topics are created and accessible
- [ ] Event publishing works in ticket-service
- [ ] Event consumption works in notifications-service
- [ ] Manager lookup works in user-service
- [ ] Notifications are created and stored correctly

### Frontend Verification
- [ ] Manager dashboard shows notification bell
- [ ] Notification count updates correctly
- [ ] Notification dropdown displays properly
- [ ] Notifications can be marked as read
- [ ] Real-time updates work via WebSocket/polling

### End-to-End Verification
- [ ] Create ticket → Assignment → Manager notification
- [ ] Add note → User notification
- [ ] Change status → User notification
- [ ] All notifications appear in correct dashboards

## Common Issues and Solutions

### Issue: Manager notifications not working
**Solution**: Check if team has a manager assigned and manager lookup endpoint works

### Issue: User notifications not appearing
**Solution**: Verify event publishing in ticket-service and consumption in notifications-service

### Issue: Frontend notifications not updating
**Solution**: Check WebSocket connection and notification service integration

### Issue: Kafka events not flowing
**Solution**: Verify Kafka is running and topics are created correctly

## Success Criteria

✅ **Complete Success**: All notification types work end-to-end
- Managers receive assignment notifications
- Users receive note and status change notifications  
- Frontend displays notifications correctly
- Real-time updates work properly

The notification system provides comprehensive coverage for all stakeholder communication needs in the ITSM system.
