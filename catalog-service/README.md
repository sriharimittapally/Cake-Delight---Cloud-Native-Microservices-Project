# Catalog Service

Manages the cake products of Cake Delight: listing and filtering cakes, adding and deleting cakes, and switching a cake between available and unavailable.

| | |
|---|---|
| Port | 8081 |
| Technology | Java 21, Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA, Hibernate, Bean Validation |
| Database | MySQL `cake_catalog_db`, table `cakes` |
| Called by | API Gateway (browser traffic) and Order Service (cake details) |

## 1. Responsibilities

- Return the cakes that customers can buy (available cakes only), with optional filters.
- Return all cakes, including unavailable ones, for the admin page.
- Return, add and delete a cake.
- Toggle the availability of a cake.
- Create the `cakes` table and insert five sample cakes at startup.

## 2. Project structure

```text
catalog-service/
├── src/main/java/com/cakedelight/catalog/
│   ├── CakeCatalogServiceApplication.java
│   ├── controller/CakeController.java
│   ├── dto/request/CreateCakeRequest.java
│   ├── dto/response/CakeResponse.java
│   ├── entity/Cake.java
│   ├── mapper/CakeMapper.java
│   ├── repository/CakeRepository.java
│   └── service/
│       ├── CakeService.java
│       └── impl/CakeServiceImpl.java
├── src/main/resources/
│   ├── application.properties
│   ├── schema.sql
│   └── data.sql
├── Dockerfile
├── pom.xml
└── README.md
```

## 3. Database

Database: `cake_catalog_db`  Table: `cakes`

| Column | Type | Description |
|---|---|---|
| `id` | BIGINT, PK, auto-increment | Cake ID |
| `name` | VARCHAR(150) | Cake name |
| `description` | TEXT | Description |
| `category` | VARCHAR(100) | Category, for example Birthday, Anniversary |
| `price` | DECIMAL(10,2) | Price |
| `available` | BOOLEAN | `true` = can be bought |
| `image_reference` | VARCHAR(500) | Image URL |

- `schema.sql` creates the table, `data.sql` inserts five sample cakes (Chocolate, Vanilla, Black Forest, Red Velvet, Strawberry) only if they do not already exist.
- Hibernate runs with `ddl-auto=validate`: it checks the entity against the table but never changes it.
- The database itself must exist before the service starts (created by `docker/init/01-create-databases.sql` or the `mysql-init` ConfigMap).

## 4. API

Direct base URL: `http://localhost:8081/api/cakes`
Through the gateway: `http://localhost:8080/api/cakes`

### 4.1 List available cakes

`GET /api/cakes`

| Query parameter | Type | Description |
|---|---|---|
| `name` | text | Part of the cake name (not case-sensitive) |
| `category` | text | Exact category (not case-sensitive) |
| `minPrice` | number | Minimum price |
| `maxPrice` | number | Maximum price |

All parameters are optional and can be combined.

```http
GET /api/cakes?category=Birthday&minPrice=300&maxPrice=800
```

**Response – 200 OK**

```json
[
  {
    "id": 1,
    "name": "Chocolate Cake",
    "description": "Rich chocolate cake with chocolate cream.",
    "category": "Birthday",
    "price": 500.00,
    "available": true,
    "imageReference": "https://example.com/images/chocolate-cake.jpg"
  },
  {
    "id": 2,
    "name": "Vanilla Cake",
    "description": "Soft vanilla sponge cake with vanilla frosting.",
    "category": "Birthday",
    "price": 400.00,
    "available": true,
    "imageReference": "https://example.com/images/vanilla-cake.jpg"
  }
]
```

### 4.2 List all cakes (admin)

`GET /api/cakes/admin`

Returns every cake, including cakes with `"available": false`.

**Response – 200 OK**

```json
[
  {
    "id": 1,
    "name": "Chocolate Cake",
    "description": "Rich chocolate cake with chocolate cream.",
    "category": "Birthday",
    "price": 500.00,
    "available": true,
    "imageReference": "https://example.com/images/chocolate-cake.jpg"
  },
  {
    "id": 5,
    "name": "Strawberry Cake",
    "description": "Fresh strawberry cake with cream.",
    "category": "Birthday",
    "price": 550.00,
    "available": false,
    "imageReference": "https://example.com/images/strawberry-cake.jpg"
  }
]
```

### 4.3 Get a cake

`GET /api/cakes/{id}`

```http
GET /api/cakes/1
```

**Response – 200 OK**

```json
{
  "id": 1,
  "name": "Chocolate Cake",
  "description": "Rich chocolate cake with chocolate cream.",
  "category": "Birthday",
  "price": 500.00,
  "available": true,
  "imageReference": "https://example.com/images/chocolate-cake.jpg"
}
```

### 4.4 Add a cake

`POST /api/cakes`

| Field | Type | Required | Rule |
|---|---|---|---|
| `name` | text | yes | up to 150 characters |
| `description` | text | no | |
| `category` | text | yes | up to 100 characters |
| `price` | number | yes | at least 0.01, up to 8 digits and 2 decimals |
| `available` | boolean | yes | |
| `imageReference` | text | no | up to 500 characters |

**Request**

```json
{
  "name": "Pineapple Cake",
  "description": "Soft sponge with pineapple cream.",
  "category": "Birthday",
  "price": 450.00,
  "available": true,
  "imageReference": "https://example.com/images/pineapple-cake.jpg"
}
```

**Response – 201 Created**

```json
{
  "id": 6,
  "name": "Pineapple Cake",
  "description": "Soft sponge with pineapple cream.",
  "category": "Birthday",
  "price": 450.00,
  "available": true,
  "imageReference": "https://example.com/images/pineapple-cake.jpg"
}
```

### 4.5 Toggle availability

`PATCH /api/cakes/{id}/availability`

No request body. An available cake becomes unavailable and an unavailable cake becomes available.

```http
PATCH /api/cakes/6/availability
```

**Response – 204 No Content** (empty body)

### 4.6 Delete a cake

`DELETE /api/cakes/{id}`

```http
DELETE /api/cakes/6
```

**Response – 204 No Content** (empty body)

## 5. Environment variables

| Variable | Meaning | Local example | Docker / Kubernetes |
|---|---|---|---|
| `MYSQL_HOST` | MySQL host | `localhost` | `mysql` |
| `MYSQL_PORT` | MySQL port | `3306` | `3306` |
| `CAKE_CATALOG_DB` | Database name | `cake_catalog_db` | `cake_catalog_db` |
| `MYSQL_USERNAME` | Database user | `root` | from Secret / `.env` |
| `MYSQL_PASSWORD` | Database password | your password | from Secret / `.env` |

## 6. Run locally with Maven

Prerequisites: JDK 21, MySQL running on port 3306.

1. Create the database:

```sql
CREATE DATABASE IF NOT EXISTS cake_catalog_db;
```

2. Set the variables and run.

Linux / macOS:

```bash
cd catalog-service
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export CAKE_CATALOG_DB=cake_catalog_db
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=your_mysql_password
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
cd catalog-service
$env:MYSQL_HOST="localhost"
$env:MYSQL_PORT="3306"
$env:CAKE_CATALOG_DB="cake_catalog_db"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="your_mysql_password"
.\mvnw.cmd spring-boot:run
```

3. Check: http://localhost:8081/api/cakes

## 7. Run with Docker

```bash
cd catalog-service
docker build -t cake-delight-catalog-service .
```

Normally the service is started with the whole system:

```bash
cd docker
docker compose up -d
```

## 8. Run on Kubernetes

Manifest: `k8s/catalog-service.yaml` (Deployment and ClusterIP Service `catalog-service`, port 8081; settings from the ConfigMap `cake-delight-config`, database login from the Secret `cake-delight-secrets`).

```bash
kubectl apply -f k8s/catalog-service.yaml
kubectl get pods -n cake-delight
kubectl logs -n cake-delight deployment/catalog-service
```

The service is internal. Reach it through the gateway port-forward: `http://localhost:8080/api/cakes`.