@echo off
echo Starting AstroGlow Backend Server...
echo.
echo This script will start the backend server on port 8080
echo.

cd backend\AstroGlow

echo Checking if the Java process is already running...
netstat -ano | findstr :8080 >nul
if %errorlevel% equ 0 (
    echo PORT 8080 is already in use. Please close the application using this port first.
    echo.
    echo You can do this by:
    echo 1. Opening Task Manager
    echo 2. Going to Details tab
    echo 3. Finding java.exe or javaw.exe process
    echo 4. Right-click and select "End task"
    echo.
    pause
    exit /b
)

echo Starting Spring Boot application...
echo.
echo NOTE: This window must remain open while using the application
echo.

call mvnw.cmd spring-boot:run

pause 