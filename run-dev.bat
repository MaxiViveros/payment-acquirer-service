@echo off
REM Run with local Maven (development mode with H2 database)

echo Starting Payment Acquirer Service in development mode...
echo    Using H2 in-memory database
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo Maven is not installed. Please install Maven or use Docker:
    echo    Download from: https://maven.apache.org/download.cgi
    echo.
    echo Or run with Docker:
    echo    docker-compose up
    exit /b 1
)

REM Check Java version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
set JAVA_VERSION=%JAVA_VERSION:"=%
for /f "delims=. tokens=1" %%v in ("%JAVA_VERSION%") do set JAVA_MAJOR=%%v

echo Detected Java version: %JAVA_VERSION%

if %JAVA_MAJOR% geq 22 (
    echo.
    echo WARNING: Java %JAVA_MAJOR% detected. This project requires Java 17 or 21.
    echo Please set JAVA_HOME to Java 17 or 21 installation.
    echo.
    echo Example:
    echo    set JAVA_HOME=C:\Program Files\Java\jdk-21
    echo    set PATH=%%JAVA_HOME%%\bin;%%PATH%%
    echo.
    pause
    exit /b 1
)

REM Clean, compile and run
echo Compiling project...
call mvn clean compile -DskipTests

if %errorlevel% neq 0 (
    echo Compilation failed
    pause
    exit /b 1
)

echo.
echo Starting server...
echo    API: http://localhost:8080/payments
echo    Swagger UI: http://localhost:8080/swagger-ui.html
echo    H2 Console: http://localhost:8080/h2-console
echo.

call mvn spring-boot:run -Dspring-boot.run.profiles=dev

echo.
echo Service stopped
pause
