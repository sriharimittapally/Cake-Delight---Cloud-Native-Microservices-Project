# Rating Service

Lets customers rate and review the cakes they bought, and provides average ratings for the shop.

| | |
|---|---|
| Port | 8084 |
| Technology | Java 21, Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA, Bean Validation |
| Database | MySQL `cake_rating_db`, table `ratings` |
| Calls | Order Service (purchase verification) |
| Called by | API Gateway |

## 1. Responsibilities

- Accept a rating (1 to 5) with an optional review.
- Confirm with the Order Service that the customer bought that cake in that order.
- Allow only one rating per cake per order.
- Return reviews of a cake, the average rating and rating count of a cake, summaries of all rated cakes, and all ratings given by an email.

## 2. Project structure

```text
rating-service/
├── src/main/java/com/cakedelight/rating/
│   ├── RatingServiceApplication.java
│   ├── client/OrderServiceClient.java
│   ├── controller/RatingController.java
│   ├── dto/request/CreateRatingRequest.java
│   ├── dto/response/                 RatingResponse, RatingSummaryResponse
│   ├── entity/Rating.java
│   ├── repository/RatingRepository.java
│   └── service/                      RatingService, impl/RatingServiceImpl
├── src/main/resources/application.properties
├── Dockerfile
├── pom.xml
└── README.md
```

## 3. Business rules

1. The Order Service must confirm `orderId` + `customerEmail` + `cakeId` (the cake was bought by that email in that order).
2. A cake can be rated only once per order (unique `order_id` + `cake_id`).
3. The rating value is between 1 and 5; the review is optional, up to 1000 characters.
4. The average rating is rounded to one decimal place; a cake without ratings has average 0.0 and count 0.

## 4. Database

Database: `cake_rating_db`  Table: `ratings` (created by Hibernate, `ddl-auto=update`)

| Column | Type | Description |
|---|---|---|
| `id` | BIGINT, PK | Rating ID |
| `order_id` | BIGINT | Order that contained the cake |
| `cake_id` | BIGINT | Rated cake |
| `customer_name` | VARCHAR(150) | Customer name |
| `customer_email` | VARCHAR(255) | Customer email |
| `rating` | INT | 1 to 5 |
| `review` | VARCHAR(1000) | Optional review |
| `created_at` | DATETIME | Time of rating |

Unique constraint `uk_order_cake_rating` on (`order_id`, `cake_id`).

## 5. API

Direct base URL: `http://localhost:8084/api/ratings`
Through the gateway: `http://localhost:8080/api/ratings`

### 5.1 Submit a rating

`POST /api/ratings`

| Field | Type | Required | Rule |
|---|---|---|---|
| `orderId` | number | yes | |
| `cakeId` | number | yes | |
| `customerName` | text | yes | up to 150 characters |
| `customerEmail` | text | yes | must match the order's email |
| `rating` | number | yes | 1 to 5 |
| `review` | text | no | up to 1000 characters |

**Request**

```json
{
  "orderId": 1,
  "cakeId": 1,
  "customerName": "Srihari",
  "customerEmail": "customer@example.com",
  "rating": 5,
  "review": "Very tasty and fresh."
}
```

**Response – 201 Created**

```json
{
  "id": 1,
  "orderId": 1,
  "cakeId": 1,
  "customerName": "Srihari",
  "rating": 5,
  "review": "Very tasty and fresh.",
  "createdAt": "2026-11-17T10:30:12.456789"
}
```

### 5.2 Reviews of a cake

`GET /api/ratings/cake/{cakeId}`

Newest first.

**Response – 200 OK**

```json
[
  {
    "id": 2,
    "orderId": 2,
    "cakeId": 1,
    "customerName": "Ravi",
    "rating": 4,
    "review": "Good taste.",
    "createdAt": "2026-11-17T11:02:45.100000"
  },
  {
    "id": 1,
    "orderId": 1,
    "cakeId": 1,
    "customerName": "Srihari",
    "rating": 5,
    "review": "Very tasty and fresh.",
    "createdAt": "2026-11-17T10:30:12.456789"
  }
]
```

### 5.3 Summary of one cake

`GET /api/ratings/cake/{cakeId}/summary`

**Response – 200 OK**

```json
{
  "cakeId": 1,
  "averageRating": 4.5,
  "totalRatings": 2
}
```

### 5.4 Summaries of all rated cakes

`GET /api/ratings/summary`

Used by the Cakes page to show the stars.

**Response – 200 OK**

```json
[
  {
    "cakeId": 1,
    "averageRating": 4.5,
    "totalRatings": 2
  },
  {
    "cakeId": 3,
    "averageRating": 5.0,
    "totalRatings": 1
  }
]
```

### 5.5 Ratings given by a customer

`GET /api/ratings/customer?email=customer@example.com`

Used by the Orders page to show which cakes were already rated.

**Response – 200 OK**

```json
[
  {
    "id": 1,
    "orderId": 1,
    "cakeId": 1,
    "customerName": "Srihari",
    "rating": 5,
    "review": "Very tasty and fresh.",
    "createdAt": "2026-11-17T10:30:12.456789"
  }
]
```

## 6. Environment variables

| Variable | Meaning | Local example | Docker / Kubernetes |
|---|---|---|---|
| `MYSQL_HOST`, `MYSQL_PORT` | MySQL address | `localhost`, `3306` | `mysql`, `3306` |
| `CAKE_RATING_DB` | Database name | `cake_rating_db` | `cake_rating_db` |
| `MYSQL_USERNAME`, `MYSQL_PASSWORD` | Database login | `root`, your password | Secret / `.env` |
| `ORDER_SERVICE_URL` | Order Service address | `http://localhost:8082` | `http://order-service:8082` |

## 7. Run locally with Maven

Prerequisites: JDK 21, MySQL (port 3306), Order Service running on 8082 (and Catalog Service for creating test orders).

```sql
CREATE DATABASE IF NOT EXISTS cake_rating_db;
```

Linux / macOS:

```bash
cd rating-service
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export CAKE_RATING_DB=cake_rating_db
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=your_mysql_password
export ORDER_SERVICE_URL=http://localhost:8082
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
cd rating-service
$env:MYSQL_HOST="localhost"
$env:MYSQL_PORT="3306"
$env:CAKE_RATING_DB="cake_rating_db"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="your_mysql_password"
$env:ORDER_SERVICE_URL="http://localhost:8082"
.\mvnw.cmd spring-boot:run
```

Check: http://localhost:8084/api/ratings/summary

## 8. Run with Docker

```bash
cd rating-service
docker build -t cake-delight-rating-service .
```

Normally started with the whole system:

```bash
cd docker
docker compose up -d
```

## 9. Run on Kubernetes

Manifest: `k8s/rating-service.yaml` (Deployment and ClusterIP Service `rating-service`, port 8084; ConfigMap for hosts and URLs, Secret for database login).

```bash
kubectl apply -f k8s/rating-service.yaml
kubectl get pods -n cake-delight
kubectl logs -n cake-delight deployment/rating-service
```

Reach it through the gateway port-forward: `http://localhost:8080/api/ratings/summary`.