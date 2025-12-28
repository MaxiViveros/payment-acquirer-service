# Payment Acquirer Service

## Descripción

Servicio backend que actúa como **acquirer** (adquirente) para pagos con tarjeta de crédito/débito de múltiples comercios. El servicio recibe solicitudes de pago desde comercios, valida datos, consulta un "issuer" (emisor) simulado y responde con autorización/rechazo, registrando cada transacción en una base de datos PostgreSQL.

## Arquitectura

```
payment-acquirer-service/
├── src/
│   ├── main/
│   │   ├── java/com/acquirer/
│   │   │   ├── controller/         # REST API endpoints
│   │   │   ├── service/            # Lógica de negocio
│   │   │   ├── repository/         # Acceso a datos (JPA)
│   │   │   ├── entity/             # Modelos de base de datos
│   │   │   ├── dto/                # Request/Response objects
│   │   │   ├── exception/          # Excepciones personalizadas
│   │   │   ├── config/             # Configuración
│   │   │   └── PaymentAcquirerApplication.java
│   │   └── resources/
│   │       ├── application.yml     # Configuración principal
│   │       ├── application-dev.yml # Configuración desarrollo
│   │       └── application-prod.yml # Configuración producción
│   └── test/                       # Tests unitarios e integración
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Tecnologías

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA** - Persistencia
- **PostgreSQL** - Base de datos
- **H2 Database** - Base de datos en memoria (desarrollo)
- **Maven** - Gestión de dependencias
- **Docker & Docker Compose** - Containerización
- **Swagger/OpenAPI** - Documentación API
- **Lombok** - Reducción de boilerplate
- **JUnit 5 & Mockito** - Testing

## Instalación y Ejecución

### Prerrequisitos

- **Java 17 o 21** (**Importante**: No compatible con Java 22+)
- Maven 3.6+
- Docker y Docker Compose (opcional, recomendado)

#### Verificar Versión de Java

```bash
# Ver versión actual de Java
java -version

# Listar versiones de Java instaladas
/usr/libexec/java_home -V

# Si tu sistema usa Java 22 o superior, necesitas cambiar a Java 21:
export JAVA_HOME=$(/usr/libexec/java_home -v21)
java -version  # Verificar que ahora usa Java 21
```

### Opción 1: Con Docker Compose

```bash
# Clonar el repositorio
git clone <repository-url>
cd payment-acquirer-service

# Construir y levantar servicios
docker-compose up --build

# La aplicación estará disponible en http://localhost:8080
```

### Opción 2: Ejecución Local

#### En Linux/macOS:

```bash
# Opción A: Usar el script incluido (recomendado)
./run-dev.sh

# Opción B: Ejecutar manualmente
# 1. Asegurar que usas Java 17 o 21
export JAVA_HOME=$(/usr/libexec/java_home -v21)

# 2. Compilar el proyecto
mvn clean compile -DskipTests

# 3. Ejecutar con perfil dev (H2 en memoria)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# La aplicación estará disponible en http://localhost:8080
```

#### En Windows:

```cmd
REM Opción A: Usar el script incluido (recomendado)
run-dev.bat

REM Opción B: Ejecutar manualmente
REM 1. Compilar el proyecto
mvn clean compile -DskipTests

REM 2. Ejecutar con perfil dev (H2 en memoria)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

REM La aplicación estará disponible en http://localhost:8080
```

> **Nota para Windows**: Asegúrate de tener configurada la variable de entorno `JAVA_HOME` apuntando a Java 17 o 21. Puedes verificarlo con `echo %JAVA_HOME%` y configurarlo en las variables de entorno del sistema si es necesario.

### Opción 3: Con PostgreSQL Local

```bash
# Iniciar PostgreSQL con Docker
docker run --name acquirer-postgres \
  -e POSTGRES_DB=acquirer_db \
  -e POSTGRES_USER=acquirer_user \
  -e POSTGRES_PASSWORD=acquirer_pass \
  -p 5432:5432 \
  -d postgres:16-alpine

# Ejecutar la aplicación
mvn spring-boot:run
```

## API Endpoints

### 1. Procesar Pago

**POST** `/payments`

Procesa una solicitud de pago desde un comercio.

**Request Body:**
```json
{
  "merchantId": "MERCHANT_001",
  "amount": 100.50,
  "currency": "USD",
  "cardToken": "tok_4532015112830366",
  "cardExpiry": "12/2025",
  "operationType": "PURCHASE"
}
```

**Response (201 Created):**
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "APPROVED",
  "responseCode": "00",
  "message": "Transaction approved",
  "timestamp": "2025-12-26T10:30:00",
  "merchantId": "MERCHANT_001",
  "amount": 100.50,
  "currency": "USD"
}
```

**Posibles Estados:**
- `APPROVED` - Transacción aprobada
- `DECLINED` - Transacción rechazada por el emisor
- `ERROR` - Error en el procesamiento

### 2. Consultar Transacción

**GET** `/payments/{transactionId}`

Obtiene los detalles de una transacción específica.

**Response (200 OK):**
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "APPROVED",
  "responseCode": "00",
  "message": "Transaction approved",
  "timestamp": "2025-12-26T10:30:00",
  "merchantId": "MERCHANT_001",
  "amount": 100.50,
  "currency": "USD"
}
```

### 3. Listar Transacciones con Filtros

**GET** `/payments?merchantId={merchantId}&status={status}`

Lista transacciones con filtros opcionales.

**Parámetros Query:**
- `merchantId` (opcional) - Filtrar por ID de comercio
- `status` (opcional) - Filtrar por estado (APPROVED, DECLINED, ERROR, PENDING)

**Ejemplos:**
```bash
# Todas las transacciones
GET /payments

# Transacciones de un comercio
GET /payments?merchantId=MERCHANT_001

# Transacciones aprobadas
GET /payments?status=APPROVED

# Transacciones aprobadas de un comercio
GET /payments?merchantId=MERCHANT_001&status=APPROVED
```

## Documentación Swagger

Una vez que el servicio esté corriendo, accede a la documentación interactiva:

- **Swagger UI:** http://localhost:8080/swagger-ui.html

## 🔧 Reglas de Negocio

### Validaciones Implementadas

1. **Límite de Monto por Comercio**
   - Cada comercio tiene un límite máximo de transacción
   - Por defecto: $10,000 USD
   - Configurable por comercio en la base de datos

2. **Tarjetas Bloqueadas**
   - Tarjetas que coinciden con patrones configurables son rechazadas
   - Configurado en `application.yml`:
     ```yaml
     acquirer:
       rules:
         blocked-card-patterns: "^4111111111111111$,^5555555555554444$"
     ```

3. **Monedas Soportadas**
   - USD, EUR, GBP, ARS, BRL
   - Extensible en `ValidationService`

4. **Validación de Comercio**
   - El comercio debe existir y estar activo

### Mock Issuer (Emisor Simulado)

El servicio incluye un emisor simulado que:
- Aprueba transacciones con 70% de probabilidad (configurable)
- Rechaza aleatoriamente con códigos reales:
  - `05` - Do not honor
  - `51` - Insufficient funds
  - `54` - Expired card
  - `61` - Exceeds withdrawal limit
  - `65` - Activity limit exceeded
- Simula latencia de red (100-300ms)

Configuración:
```yaml
acquirer:
  rules:
    issuer:
      approval-rate: 0.7  # 70% de aprobación
```

## Modelo de Datos

### Tabla: transactions

| Campo | Tipo | Descripción |
|-------|------|-------------|
| transaction_id | UUID | ID único de transacción |
| merchant_id | VARCHAR | ID del comercio |
| amount | DECIMAL(19,2) | Monto de la transacción |
| currency | VARCHAR(3) | Código de moneda (ISO 4217) |
| card_token | VARCHAR | Token o hash de la tarjeta |
| card_expiry | VARCHAR(7) | Fecha de expiración (MM/YYYY) |
| operation_type | VARCHAR(50) | Tipo de operación |
| status | VARCHAR(20) | Estado de la transacción |
| response_code | VARCHAR(10) | Código de respuesta |
| issuer_response | VARCHAR(20) | Respuesta del emisor |
| rejection_reason | VARCHAR(255) | Razón de rechazo |
| created_at | TIMESTAMP | Fecha de creación |
| updated_at | TIMESTAMP | Fecha de actualización |
| processed_at | TIMESTAMP | Fecha de procesamiento |

**Índices para alto volumen:**
- `idx_merchant_id` - Búsquedas por comercio
- `idx_status` - Búsquedas por estado
- `idx_created_at` - Consultas temporales
- `idx_merchant_status` - Búsquedas combinadas

### Tabla: merchants

| Campo | Tipo | Descripción |
|-------|------|-------------|
| merchant_id | VARCHAR | ID único del comercio |
| merchant_name | VARCHAR | Nombre del comercio |
| max_transaction_amount | DECIMAL(19,2) | Límite máximo por transacción |
| active | BOOLEAN | Estado del comercio |
| created_at | TIMESTAMP | Fecha de creación |
| updated_at | TIMESTAMP | Fecha de actualización |

## 🔍 Sistema de Logs y Trazabilidad

El servicio implementa un sistema de logging completo usando **MDC (Mapped Diagnostic Context)** de SLF4J:

### Características

- **TransactionId y MerchantId** en cada línea de log
- Logs estructurados para fácil análisis
- Niveles de log configurables
- Archivo de logs rotativo

### Formato de Log

```
2025-12-26 10:30:00.123 [http-nio-8080-exec-1] INFO  c.a.service.PaymentService - Payment request received - Merchant: MERCHANT_001, Amount: 100.00 USD, Card: **** 0366 - [TransactionId: 550e8400-e29b-41d4-a716-446655440000] [MerchantId: MERCHANT_001]
```

### Ejemplo de Flujo de Transacción en Logs

```
INFO  - === STARTING PAYMENT PROCESSING ===
INFO  - Payment request received - Merchant: MERCHANT_001, Amount: 100.00 USD
DEBUG - Transaction created with PENDING status
INFO  - Validating merchant
INFO  - Merchant validation PASSED - Merchant: Test Store Alpha is active
INFO  - Validating business rules
INFO  - Business rules validation PASSED
INFO  - Requesting authorization from issuer
DEBUG - Calling issuer for authorization - Card: **** 0366, Amount: 100.00 USD
INFO  - Issuer APPROVED transaction - Card: **** 0366
INFO  - Processing issuer response
INFO  - === PAYMENT PROCESSING COMPLETED - Status: APPROVED ===
```

### Configuración de Logs

Ubicación de logs: `logs/payment-acquirer.log`
Configuración en `application.yml`:
```yaml
logging:
  level:
    root: INFO
    com.acquirer: DEBUG
  file:
    name: logs/payment-acquirer.log
    max-size: 10MB
    max-history: 30
```

## Testing

### Ejecutar Tests

```bash
# Todos los tests
mvn test

# Tests con cobertura
mvn test jacoco:report

# Solo tests de integración
mvn test -Dtest=*ControllerTest
```

### Tests Implementados

1. **PaymentServiceTest** - Tests unitarios del servicio principal
2. **ValidationServiceTest** - Tests de reglas de negocio
3. **PaymentControllerTest** - Tests de integración de API

### Comercios de Prueba

El sistema inicializa automáticamente 3 comercios para testing:

| Merchant ID | Nombre | Límite Máximo |
|-------------|--------|---------------|
| MERCHANT_001 | Test Store Alpha | $5,000 |
| MERCHANT_002 | Test Store Beta | $10,000 |
| MERCHANT_003 | Test Store Gamma | $1,000 |

## Colección Postman

Importa la colección Postman incluida: `postman_collection.json`

### Ejemplos de Requests Incluidos:

1. **Payment - Successful Purchase** - Transacción exitosa
2. **Payment - Amount Exceeds Limit** - Monto excede límite
3. **Payment - Blocked Card** - Tarjeta bloqueada
4. **Get Transaction by ID** - Consultar transacción
5. **List All Transactions** - Listar todas
6. **Filter by Merchant** - Filtrar por comercio
7. **Filter by Status** - Filtrar por estado
8. **Health Check** - Verificar servicio

## Configuración

### Variables de Entorno

```bash
# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/acquirer_db
SPRING_DATASOURCE_USERNAME=acquirer_user
SPRING_DATASOURCE_PASSWORD=acquirer_pass

# Perfil activo
SPRING_PROFILES_ACTIVE=dev

# Puerto del servidor
SERVER_PORT=8080
```

### Perfiles de Spring

- **dev** - Desarrollo (H2 en memoria)
- **prod** - Producción (PostgreSQL)

## Troubleshooting

### Error: "Port 8080 already in use"

```bash
# Cambiar puerto
SERVER_PORT=8081 mvn spring-boot:run
```

### Error: "Connection refused" a PostgreSQL

```bash
# Verificar que PostgreSQL esté corriendo
docker ps | grep postgres

# Ver logs de PostgreSQL
docker logs acquirer-postgres
```

### Ver logs de la aplicación

```bash
# Logs en tiempo real con Docker
docker-compose logs -f payment-service

# Logs locales
tail -f logs/payment-acquirer.log
```
---

**Versión:** 1.0.0  
**Última actualización:** 28 de diciembre de 2025
