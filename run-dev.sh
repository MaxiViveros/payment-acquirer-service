#!/bin/bash

# Run with local Maven (development mode with H2 database)

echo "Starting Payment Acquirer Service in development mode..."
echo "   Using H2 in-memory database"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven or use Docker:"
    echo "   brew install maven"
    echo ""
    echo "Or run with Docker:"
    echo "   ./start.sh"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
echo "Detected Java version: $JAVA_VERSION"

if [ "$JAVA_VERSION" -ge 22 ]; then
    echo "Java $JAVA_VERSION detected. This project requires Java 17 or 21."
    echo "   Attempting to switch to Java 21..."
    echo ""
    
    # Try to switch to Java 21
    if /usr/libexec/java_home -v21 &> /dev/null; then
        export JAVA_HOME=$(/usr/libexec/java_home -v21)
        echo "Switched to Java 21: $JAVA_HOME"
    elif /usr/libexec/java_home -v17 &> /dev/null; then
        export JAVA_HOME=$(/usr/libexec/java_home -v17)
        echo "Switched to Java 17: $JAVA_HOME"
    else
        echo "Java 17 or 21 not found. Please install:"
        echo "   brew install openjdk@21"
        exit 1
    fi
    echo ""
fi

# Clean, compile and run
echo "Compiling project..."
mvn clean compile -DskipTests

if [ $? -ne 0 ]; then
    echo "Compilation failed"
    exit 1
fi

echo ""
echo "Starting server..."
echo "   API: http://localhost:8080/payments"
echo "   Swagger UI: http://localhost:8080/swagger-ui.html"
echo "   H2 Console: http://localhost:8080/h2-console"
echo ""

mvn spring-boot:run -Dspring-boot.run.profiles=dev

echo ""
echo "Service stopped"
