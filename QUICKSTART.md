# Quick Start Guide

## Importante: Requisitos de Java

Este proyecto requiere **Java 17 o 21**. No es compatible con Java 22 o superior.

```bash
# Verificar tu versión de Java
java -version

# Si tienes Java 22+, cambia a Java 21:
export JAVA_HOME=$(/usr/libexec/java_home -v21)
```

## Inicio Rápido

### Opción 1: Desarrollo Local

#### macOS/Linux

```bash
# 1. Ejecutar el script de desarrollo
./run-dev.sh

# 2. La API estará disponible en http://localhost:8080

# 3. Ver documentación interactiva
open http://localhost:8080/swagger-ui.html
```

#### Windows

```cmd
# 1. Ejecutar el script de desarrollo
run-dev.bat

# 2. La API estará disponible en http://localhost:8080

# 3. Ver documentación interactiva
start http://localhost:8080/swagger-ui.html
```

**O ejecutar manualmente con Maven:**

```cmd
mvn clean compile -DskipTests
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Opción 2: Con Docker

```bash
# 1. Ejecutar el script de inicio
./start.sh

# 2. La API estará disponible en http://localhost:8080

# 3. Ver documentación
open http://localhost:8080/swagger-ui.html
```

## Primeros Pasos

### 1. Verificar que el servicio está corriendo

```bash
curl http://localhost:8080/payments/health
```

Respuesta esperada: `Payment Acquirer Service is running`

### 2. Crear tu primera transacción

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "MERCHANT_001",
    "amount": 100.50,
    "currency": "USD",
    "cardToken": "tok_4532015112830366",
    "cardExpiry": "12/2025",
    "operationType": "PURCHASE"
  }'
```

### 3. Consultar la transacción

```bash
# Reemplaza {transactionId} con el ID de la respuesta anterior
curl http://localhost:8080/payments/{transactionId}
```

### 4. Listar todas las transacciones

```bash
curl http://localhost:8080/payments
```

## Comercios de Prueba Disponibles

| Merchant ID | Nombre | Límite Máximo |
|-------------|--------|---------------|
| MERCHANT_001 | Test Store Alpha | $5,000 |
| MERCHANT_002 | Test Store Beta | $10,000 |
| MERCHANT_003 | Test Store Gamma | $1,000 |

## Probar Diferentes Escenarios

### Transacción Exitosa

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "MERCHANT_001",
    "amount": 100.00,
    "currency": "USD",
    "cardToken": "tok_4532015112830366",
    "cardExpiry": "12/2025",
    "operationType": "PURCHASE"
  }'
```

### Monto Excede Límite

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "MERCHANT_003",
    "amount": 1500.00,
    "currency": "USD",
    "cardToken": "tok_4532015112830366",
    "cardExpiry": "12/2025",
    "operationType": "PURCHASE"
  }'
```

### Tarjeta Bloqueada

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "MERCHANT_001",
    "amount": 100.00,
    "currency": "USD",
    "cardToken": "4111111111111111",
    "cardExpiry": "12/2025",
    "operationType": "PURCHASE"
  }'
```

## Importar Colección de Postman

1. Abrir Postman
2. Click en "Import"
3. Seleccionar el archivo `postman_collection.json`

## Ver Logs en Tiempo Real

```bash
# Con Docker
docker-compose logs -f payment-service

# Local
tail -f logs/payment-acquirer.log
```

## Acceso a Base de Datos

### PostgreSQL (Docker)

```bash
docker exec -it acquirer-postgres psql -U acquirer_user -d acquirer_db
```

### H2 Console (Modo dev)

http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: (vacío)

## Detener Servicios

```bash
# Docker
docker-compose down

# Detener y eliminar datos
docker-compose down -v
```

## Recursos Útiles

- **README completo**: [README.md](README.md)
- **Postman Collection**: [postman_collection.json](postman_collection.json)
- **Docker Compose**: [docker-compose.yml](docker-compose.yml)
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## Troubleshooting Rápido

### Puerto 8080 ocupado

```bash
# Cambiar puerto
docker-compose down
# Editar docker-compose.yml: "8081:8080" en lugar de "8080:8080"
docker-compose up -d
```

### No puedo conectar a la API

```bash
# Verificar que el servicio está corriendo
docker ps

# Ver logs para errores
docker-compose logs payment-service
```

### Resetear todo

```bash
docker-compose down -v
./start.sh
```
