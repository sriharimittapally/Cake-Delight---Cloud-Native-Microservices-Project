# Order Service

Manages baskets and orders. A customer creates a basket, adds cakes, and checks out. Checkout creates the order and publishes an event to RabbitMQ so the Notification Service can send the confirmation email.

| | |
|---|---|
| Port | 8082 |
| Technology | Java 21, Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA, Spring AMQP |
| Database | MySQL `cake_order_db`: `baskets`, `basket_items`, `orders`, `order_items` |
| Calls | Catalog Service (REST), RabbitMQ (publish) |
| Called by | API Gateway, Rating Service (purchase verification) |

## 1. Responsibilities

- Create baskets, add cakes, change quantities, remove items.
- Read cake name, price and availability from the Catalog Service when a cake is added.
- Check out a basket into an order and mark the basket as checked out.
- Publish the `order.completed` event to RabbitMQ.
- Return orders (all, by ID, by customer email).
- Verify that a customer bought a given cake in a given order.

## 2. Project structure

```text
order-service/
├── src/main/java/com/cakedelight/order/
│   ├── OrderServiceApplication.java
│   ├── client/CatalogClient.java            REST client for the Catalog Service
│   ├── client/dto/CatalogCakeResponse.java
│   ├── config/RabbitMQConfig.java           Exchange and JSON converter
│   ├── config/RestClientConfig.java
│   ├── controller/BasketController.java
│   ├── controller/OrderController.java
│   ├── dto/request/                         AddBasketItemRequest, UpdateBasketItemRequest, CheckoutRequest
│   ├── dto/response/                        BasketResponse, BasketItemResponse, OrderResponse, OrderItemResponse
│   ├── entity/                              Basket, BasketItem, Order, OrderItem
│   ├── event/                               OrderCompletedEvent, OrderEventPublisher
│   ├── exception/                           GlobalExceptionHandler, ApiErrorResponse
│   ├── repository/                          BasketRepository, BasketItemRepository, OrderRepository
│   ├── service/                             BasketService, OrderService (+ impl/)
│   └── util/                                BasketMapper, OrderMapper
├── src/main/resources/application.properties
├── Dockerfile
├── pom.xml
└── README.md
```

## 3. Business rules

- A new basket has status `ACTIVE`. After checkout it becomes `CHECKED_OUT`.
- Only `ACTIVE` baskets can be changed or checked out.
- Adding a cake that is already in the basket increases its quantity.
- Only available cakes can be added.
- Basket items store a copy of the cake name and unit price. Order items copy these values from the basket.
- A new order has status `PLACED`. The order total is the sum of the item subtotals.

## 4. Database (`cake_order_db`)

| Table | Columns |
|---|---|
| `baskets` | `basket_id` (PK), `status`, `created_at`, `updated_at` |
| `basket_items` | `id` (PK), `basket_id` (FK), `cake_id`, `cake_name`, `unit_price`, `quantity`; unique (`basket_id`, `cake_id`) |
| `orders` | `id` (PK), `basket_id` (FK, unique), `customer_name`, `customer_email`, `delivery_address`, `total_amount`, `status`, `created_at` |
| `order_items` | `id` (PK), `order_id` (FK), `cake_id`, `cake_name`, `unit_price`, `quantity`, `subtotal` |

Tables are created by Hibernate (`ddl-auto=update`).

## 5. API

Direct base URL: `http://localhost:8082`
Through the gateway: `http://localhost:8080`

### 5.1 Create a basket

`POST /api/baskets`

No request body.

**Response – 201 Created**

```json
{
  "basketId": 1,
  "status": "ACTIVE",
  "items": [],
  "totalAmount": 0
}
```

### 5.2 Add a cake to the basket

`POST /api/baskets/{basketId}/items`

| Field | Type | Rule |
|---|---|---|
| `cakeId` | number | required, an existing available cake |
| `quantity` | number | required, at least 1 |

**Request**

```json
{
  "cakeId": 1,
  "quantity": 2
}
```

**Response – 200 OK**

```json
{
  "basketId": 1,
  "status": "ACTIVE",
  "items": [
    {
      "itemId": 1,
      "cakeId": 1,
      "cakeName": "Chocolate Cake",
      "unitPrice": 500.00,
      "quantity": 2,
      "subtotal": 1000.00
    }
  ],
  "totalAmount": 1000.00
}
```

### 5.3 Get a basket

`GET /api/baskets/{basketId}`

**Response – 200 OK**

```json
{
  "basketId": 1,
  "status": "ACTIVE",
  "items": [
    {
      "itemId": 1,
      "cakeId": 1,
      "cakeName": "Chocolate Cake",
      "unitPrice": 500.00,
      "quantity": 2,
      "subtotal": 1000.00
    }
  ],
  "totalAmount": 1000.00
}
```

### 5.4 Change the quantity of an item

`PUT /api/baskets/{basketId}/items/{itemId}`

**Request**

```json
{
  "quantity": 3
}
```

**Response – 200 OK**

```json
{
  "basketId": 1,
  "status": "ACTIVE",
  "items": [
    {
      "itemId": 1,
      "cakeId": 1,
      "cakeName": "Chocolate Cake",
      "unitPrice": 500.00,
      "quantity": 3,
      "subtotal": 1500.00
    }
  ],
  "totalAmount": 1500.00
}
```

### 5.5 Remove an item

`DELETE /api/baskets/{basketId}/items/{itemId}`

**Response – 204 No Content** (empty body)

### 5.6 Check out

`POST /api/orders/checkout/{basketId}`

| Field | Type | Rule |
|---|---|---|
| `customerName` | text | required |
| `customerEmail` | text | required, valid email |
| `deliveryAddress` | text | required |

**Request**

```json
{
  "customerName": "Srihari",
  "customerEmail": "customer@example.com",
  "deliveryAddress": "Karimnagar, Telangana"
}
```

**Response – 201 Created**

```json
{
  "orderId": 1,
  "customerName": "Srihari",
  "customerEmail": "customer@example.com",
  "deliveryAddress": "Karimnagar, Telangana",
  "totalAmount": 1500.00,
  "status": "PLACED",
  "createdAt": "2026-11-17T10:15:30.123456",
  "items": [
    {
      "orderItemId": 1,
      "cakeId": 1,
      "cakeName": "Chocolate Cake",
      "unitPrice": 500.00,
      "quantity": 3,
      "subtotal": 1500.00
    }
  ]
}
```

After this response, an `order.completed` event is published to RabbitMQ (see section 6).

### 5.7 List all orders

`GET /api/orders`

**Response – 200 OK** – an array of orders in the same format as 5.6.

### 5.8 Get an order

`GET /api/orders/{orderId}`

**Response – 200 OK** – one order in the format of 5.6.

### 5.9 Orders of a customer

`GET /api/orders/customer?email=customer@example.com`

The email is matched without case sensitivity; the newest order comes first.

**Response – 200 OK**

```json
[
  {
    "orderId": 1,
    "customerName": "Srihari",
    "customerEmail": "customer@example.com",
    "deliveryAddress": "Karimnagar, Telangana",
    "totalAmount": 1500.00,
    "status": "PLACED",
    "createdAt": "2026-11-17T10:15:30.123456",
    "items": [
      {
        "orderItemId": 1,
        "cakeId": 1,
        "cakeName": "Chocolate Cake",
        "unitPrice": 500.00,
        "quantity": 3,
        "subtotal": 1500.00
      }
    ]
  }
]
```

### 5.10 Verify a purchase

`GET /api/orders/{orderId}/verify-purchase?email=customer@example.com&cakeId=1`

Used by the Rating Service. Returns `true` when the order belongs to that email and contains that cake.

**Response – 200 OK**

```json
true
```

## 6. Event published to RabbitMQ

| Property | Value |
|---|---|
| Exchange | `cake-delight` (topic, durable) |
| Routing key | `order.completed` |
| Format | JSON |

```json
{
  "orderId": 1,
  "customerName": "Srihari",
  "customerEmail": "customer@example.com",
  "deliveryAddress": "Karimnagar, Telangana",
  "totalAmount": 1500.00,
  "items": [
    {
      "orderItemId": 1,
      "cakeId": 1,
      "cakeName": "Chocolate Cake",
      "unitPrice": 500.00,
      "quantity": 3,
      "subtotal": 1500.00
    }
  ]
}
```

The Notification Service consumes this event.

## 7. Environment variables

| Variable | Meaning | Local example | Docker / Kubernetes |
|---|---|---|---|
| `MYSQL_HOST`, `MYSQL_PORT` | MySQL address | `localhost`, `3306` | `mysql`, `3306` |
| `CAKE_ORDER_DB` | Database name | `cake_order_db` | `cake_order_db` |
| `MYSQL_USERNAME`, `MYSQL_PASSWORD` | Database login | `root`, your password | Secret / `.env` |
| `RABBITMQ_HOST`, `RABBITMQ_PORT` | RabbitMQ address | `localhost`, `5672` | `rabbitmq`, `5672` |
| `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | RabbitMQ login | `guest`, `guest` | Secret / `.env` |
| `CATALOG_SERVICE_URL` | Catalog Service address | `http://localhost:8081` | `http://catalog-service:8081` |

## 8. Run locally with Maven

Prerequisites: JDK 21, MySQL (port 3306), RabbitMQ (port 5672), Catalog Service running on 8081.

```sql
CREATE DATABASE IF NOT EXISTS cake_order_db;
```

Linux / macOS:

```bash
cd order-service
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export CAKE_ORDER_DB=cake_order_db
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=your_mysql_password
export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest
export CATALOG_SERVICE_URL=http://localhost:8081
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
cd order-service
$env:MYSQL_HOST="localhost"
$env:MYSQL_PORT="3306"
$env:CAKE_ORDER_DB="cake_order_db"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="your_mysql_password"
$env:RABBITMQ_HOST="localhost"
$env:RABBITMQ_PORT="5672"
$env:RABBITMQ_USERNAME="guest"
$env:RABBITMQ_PASSWORD="guest"
$env:CATALOG_SERVICE_URL="http://localhost:8081"
.\mvnw.cmd spring-boot:run
```

Check: `POST http://localhost:8082/api/baskets`

## 9. Run with Docker

```bash
cd order-service
docker build -t cake-delight-order-service .
```

Normally started with the whole system:

```bash
cd docker
docker compose up -d
```

## 10. Run on Kubernetes

Manifest: `k8s/order-service.yaml` (Deployment and ClusterIP Service `order-service`, port 8082; ConfigMap for hosts and URLs, Secret for database and RabbitMQ login).

```bash
kubectl apply -f k8s/order-service.yaml
kubectl get pods -n cake-delight
kubectl logs -n cake-delight deployment/order-service
```

Reach it through the gateway port-forward (`http://localhost:8080/api/baskets`, `/api/orders`).