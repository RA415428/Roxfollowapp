#!/data/data/com.termux/files/usr/bin/bash
set -e

echo "===== BACKUP ====="
cp server.ts server.ts.fcm-backup
cp src/utils/notifications.ts src/utils/notifications.ts.fcm-backup

echo
echo "===== NOTIFICATION FUNCTION ====="
grep -n -A10 -B5 "broadcastAdminNotification" src/utils/notifications.ts || true

echo
echo "===== ADMIN NOTIFICATION CALLS ====="
grep -n -A10 -B5 "broadcastAdminNotification" src/components/Admin/AdminAnnouncements.tsx || true

echo
echo "===== SERVER CONFIG ====="
grep -n "globalAdminConfig\|app.post('/api/config'\|app.get('/api/config'" server.ts | head -40 || true

echo
echo "===== ADMIN COMPONENT START ====="
sed -n '1,130p' src/components/Admin/AdminAnnouncements.tsx

echo
echo "===== CHECK COMPLETE ====="
