# API Gateway

The single entry point of Cake Delight. It receives every `/api/**` request from the browser and forwards it to the right microservice. It contains no business logic.

| | |
|---|---|
| Port | 8080 |
| Technology | Java 21, Spring Boot 4.1.2, Spring Cloud Gateway Server WebMVC |
| Database | none |

## 1. Responsibilities

- Route requests to the Catalog, Order and Rating services.
- Apply CORS so the web frontend can call the API from the browser.
- Log every incoming request (method and URI).
- Expose Actuator health and route information.

## 2. Project structure

```text
api-gateway/
├── src/main/java/com/cakedelight/gateway/
│   ├── ApiGatewayApplication.java
│   ├── config/CorsConfig.java
│   └── filter/RequestLoggingFilter.java
├── src/main/resources/application.properties
├── Dockerfile
├── pom.xml
└── README.md
```

## 3. Routes

| Route ID | Path | Forwarded to |
|---|---|---|
| `catalog-service` | `/api/cakes/**` | `CATALOG_SERVICE_URL` |
| `order-service` | `/api/orders/**` | `ORDER_SERVICE_URL` |
| `basket-service` | `/api/baskets/**` | `ORDER_SERVICE_URL` |
| `rating-service` | `/api/ratings/**` | `RATING_SERVICE_URL` |

The path is forwarded unchanged: `GET http://localhost:8080/api/cakes/1` becomes `GET /api/cakes/1` on the Catalog Service.

## 4. CORS

| Setting | Value |
|---|---|
| Applies to | `/api/**` |
| Allowed origins | `http://localhost:3000`, `http://localhost:5173`, `http://localhost:4173` |
| Allowed methods | GET, POST, PUT, PATCH, DELETE, OPTIONS |
| Allowed headers | all |

Port `3000` is the Docker and Kubernetes frontend, `5173` is `npm run dev`, `4173` is `npm run preview`.

## 5. Actuator endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/actuator/health` | Gateway health with details |
| GET | `/actuator/info` | Application info |
| GET | `/actuator/gateway/routes` | Configured routes (read-only) |

**Request**

```http
GET http://localhost:8080/actuator/health
```

**Response – 200 OK** (shortened)

```json
{
  "status": "UP"
}
```

## 6. Environment variables

| Variable | Example (Docker / Kubernetes) |
|---|---|
| `CATALOG_SERVICE_URL` | `http://catalog-service:8081` |
| `ORDER_SERVICE_URL` | `http://order-service:8082` |
| `RATING_SERVICE_URL` | `http://rating-service:8084` |

## 7. Run locally with Maven

Start the Catalog, Order and Rating services first, then:

Linux / macOS:

```bash
cd api-gateway
export CATALOG_SERVICE_URL=http://localhost:8081
export ORDER_SERVICE_URL=http://localhost:8082
export RATING_SERVICE_URL=http://localhost:8084
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
cd api-gateway
$env:CATALOG_SERVICE_URL="http://localhost:8081"
$env:ORDER_SERVICE_URL="http://localhost:8082"
$env:RATING_SERVICE_URL="http://localhost:8084"
.\mvnw.cmd spring-boot:run
```

Check: http://localhost:8080/actuator/health

## 8. Run with Docker

```bash
cd api-gateway
docker build -t cake-delight-api-gateway .
```

Normally the gateway is started together with the other services:

```bash
cd docker
docker compose up -d
```

The `Dockerfile` builds the jar with Maven 3.9.9 (Java 21) and runs it on `eclipse-temurin:21-jre`, exposing port 8080.

## 9. Run on Kubernetes

Manifest: `k8s/api-gateway.yaml` (Deployment and ClusterIP Service `api-gateway`, port 8080, configuration from the ConfigMap `cake-delight-config`).

```bash
kubectl apply -f k8s/api-gateway.yaml
kubectl get pods -n cake-delight
kubectl port-forward -n cake-delight svc/api-gateway 8080:8080
```

Then open http://localhost:8080/api/cakes.