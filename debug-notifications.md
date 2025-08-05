# 🔍 Debug Notification Flow - Step by Step Guide

## 🎯 Issue: User not receiving status change notifications when technician clicks "Démarrer"

## 1. 📊 Monitor Kafka Events in Real-Time

Open **3 separate terminals** and run these commands:

### Terminal 1: Monitor Status Changes
```bash
cd C:\kafka
.\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic ticket.status.changed --from-beginning
```

### Terminal 2: Monitor Assignment Events
```bash
cd C:\kafka
.\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic assignment.created --from-beginning
```

### Terminal 3: Monitor Note Events
```bash
cd C:\kafka
.\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic ticket.note.added --from-beginning
```

## 2. 🧪 Test Scenario

### Step 1: Create a Ticket (as User)
1. Login as **UTILISATEUR**
2. Create a new ticket
3. **Expected**: Assignment event should appear in Terminal 2

### Step 2: Start Work (as Technician)
1. Login as **TECHNICIEN**
2. Go to assigned tickets
3. Click **"Démarrer"** button
4. **Expected**: Status change event should appear in Terminal 1

### Step 3: Check User Notifications
1. Login as **UTILISATEUR** (ticket owner)
2. Check notification bell
3. **Expected**: Should see status change notification

## 3. 🔍 Debug Logs to Check

### Ticket Service Logs (when clicking "Démarrer"):
Look for these log messages:
```
🔔 PUBLISHED STATUS CHANGE EVENT: ticket=..., user=..., oldStatus=EN_COURS, newStatus=OUVERT, technician=...
```

### Notifications Service Logs:
Look for these log messages:
```
🔔 RECEIVED STATUS CHANGE EVENT: topic=ticket.status.changed, ticketId=..., user=..., EN_COURS -> OUVERT, technician=...
🔔 CREATING USER STATUS NOTIFICATION: user=..., ticket=..., EN_COURS -> OUVERT, technician=...
✅ USER STATUS notification created: id=..., user=..., ticket=..., EN_COURS -> OUVERT, technician=...
```

## 4. 🚨 Common Issues to Check

### Issue 1: User receiving wrong notifications
**Symptom**: User sees assignment notifications instead of status notifications
**Check**: Look for these logs that should NOT appear for users:
```
🔔 CREATING TECHNICIAN ASSIGNMENT NOTIFICATION: technician=..., ticket=..., NOT for user=...
🔔 CREATING MANAGER ASSIGNMENT NOTIFICATION: team=..., ticket=..., NOT for user=...
```

### Issue 2: Events not being published
**Check**: Ticket service logs should show:
```
🔔 PUBLISHED STATUS CHANGE EVENT: ticket=..., user=..., oldStatus=EN_COURS, newStatus=OUVERT
```

### Issue 3: Events not being consumed
**Check**: Notifications service logs should show:
```
🔔 RECEIVED STATUS CHANGE EVENT: topic=ticket.status.changed
```

### Issue 4: Notification preferences blocking notifications
**Check**: Notifications service logs for:
```
User ... doesn't want ticket status change notifications
```

## 5. 🔧 Quick Fixes

### Fix 1: Clear Notification Preferences
If user preferences are blocking notifications:
```sql
-- Connect to notifications database
DELETE FROM notification_preferences WHERE user_id = 'USER_UUID';
```

### Fix 2: Restart Services in Order
```bash
# Stop all services
# Start in this order:
1. user-service
2. ticket-service  
3. assignment-service
4. notifications-service
```

### Fix 3: Check Database Connections
Ensure all services can connect to their databases and Kafka.

## 6. 📝 Expected Notification Flow

```
1. User creates ticket
   ↓
2. Assignment service assigns to technician
   ↓ (publishes assignment.created)
3. Notifications service creates:
   - Technician notification: "Nouveau ticket assigné"
   - Manager notification: "Nouveau ticket assigné dans votre équipe"
   ↓
4. Technician clicks "Démarrer"
   ↓ (publishes ticket.status.changed)
5. Notifications service creates:
   - User notification: "Statut de votre ticket modifié"
```

## 7. 🎯 Success Criteria

✅ **Kafka events appear in monitoring terminals**
✅ **Correct log messages in service logs**
✅ **User receives status change notification (not assignment notification)**
✅ **Notification appears in user dashboard**
✅ **Notification persists until user clicks "Lue"**

## 8. 🆘 If Still Not Working

1. **Check service startup order**
2. **Verify database connections**
3. **Check Kafka broker health**
4. **Verify JWT tokens are valid**
5. **Check user roles in database**
6. **Restart all services and test again**

## 9. 📞 Debug Commands

### Check Kafka Consumer Groups
```bash
.\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --list
.\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --describe --group notifications-service-group
```

### Check Topic Partitions
```bash
.\bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --describe --topic ticket.status.changed
```

### Reset Consumer Group (if needed)
```bash
.\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --group notifications-service-group --reset-offsets --to-earliest --topic ticket.status.changed --execute
```
