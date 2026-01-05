@echo off
setlocal

echo === Budget Tuner Launcher ===

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: Maven is not installed or not in your PATH.
    echo Please install Maven to run this application.
    pause
    exit /b 1
)

if not exist target (
    echo Building project...
    call mvn clean install -DskipTests
    if %errorlevel% neq 0 (
        echo Build failed.
        pause
        exit /b 1
    )
) else (
    echo Project appears built. Skipping compilation.
)

echo Starting application...
call mvn javafx:run

endlocal
