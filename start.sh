#!/bin/bash

# Build and Run Payment Acquirer Service with Docker Compose

echo "Building and starting Payment Acquirer Service..."
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker Desktop."
    exit 1
fi

# Stop and remove existing containers
echo "Cleaning up existing containers..."
docker-compose down -v

# Build and start services
echo "Building services..."
docker-compose up --build -d

# Wait for services to be healthy
echo ""
echo "Waiting for services to be ready..."
sleep 10

# Check health
echo ""
echo "Checking service health..."
curl -s http://localhost:8080/payments/health

echo ""
echo ""
echo "Services are running!"
echo ""
echo "Access points:"
echo "   - API: http://localhost:8080/payments"
echo "   - Swagger UI: http://localhost:8080/swagger-ui.html"
echo "   - Health Check: http://localhost:8080/payments/health"
echo ""
echo "View logs:"
echo "   docker-compose logs -f payment-service"
echo ""
echo "To stop services:"
echo "   docker-compose down"
