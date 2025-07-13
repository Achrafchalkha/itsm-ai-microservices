@echo off
echo Testing database connection for user-service...
echo.

REM Test PostgreSQL connection
echo Testing PostgreSQL connection to user_db...
psql -U postgres -h localhost -d user_db -c "SELECT 'Database connection successful!' as status;"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Database connection test PASSED!
    echo.
    
    echo Checking tables...
    psql -U postgres -h localhost -d user_db -c "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';"
    
    echo.
    echo Checking sample data...
    psql -U postgres -h localhost -d user_db -c "SELECT count(*) as user_count FROM users; SELECT count(*) as competence_count FROM competences;"
    
) else (
    echo.
    echo Database connection test FAILED!
    echo Please ensure:
    echo 1. PostgreSQL is running
    echo 2. user_db database exists
    echo 3. Credentials are correct
    echo 4. Run setup-database.sql first
)

echo.
pause
