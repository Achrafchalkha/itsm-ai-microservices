@echo off
echo Testing PostgreSQL Connection...
echo.

echo 1. Testing if PostgreSQL is running on port 5432...
netstat -an | findstr :5432

echo.
echo 2. Testing database connection with psql...
echo Please enter your PostgreSQL password when prompted.
psql -h localhost -p 5432 -U postgres -d postgres -c "\l"

echo.
echo 3. Checking if auth_db exists...
psql -h localhost -p 5432 -U postgres -d postgres -c "SELECT datname FROM pg_database WHERE datname = 'auth_db';"

echo.
echo 4. If auth_db doesn't exist, creating it...
psql -h localhost -p 5432 -U postgres -d postgres -c "CREATE DATABASE auth_db;"

echo.
echo 5. Verifying auth_db creation...
psql -h localhost -p 5432 -U postgres -d postgres -c "SELECT datname FROM pg_database WHERE datname = 'auth_db';"

pause
