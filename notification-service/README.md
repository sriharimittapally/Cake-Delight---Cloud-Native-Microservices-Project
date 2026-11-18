# Notification Service

A background service that sends the order-confirmation email. It has no REST API: it listens to RabbitMQ for `order.completed` events published by the Order Service.

| | |
|---|---|
| Port | 8083 (no public endpoints) |
| Technology | Java 21, Spring Boot 4.1.2, Spring AMQP, Spring Mail, Spring Data JPA |
| Database | MySQL `cake_notification_db`, table `notifications` |
| Input | RabbitMQ queue `notification-queue` |
| Output | Email through Gmail SMTP |

## 1. Responsibilities

- Receive the `order.completed` event.
- Save a notification record with status `PENDING`.
- Send an HTML confirmation email to the customer.
- Update the record to `SENT`, or `FAILED` if the email could not be sent.

A mail problem never affects the order: the order is already saved when the event is published.

## 2. Project structure

```text
notification-service/
├── src/main/java/com/cakedelight/notification/
│   ├── NotificationServiceApplication.java
│   ├── config/RabbitMQConfig.java            Exchange, queue, binding, JSON converter
│   ├── dto/response/OrderItemResponse.java
│   ├── entity/Notification.java
│   ├── event/OrderCompletedEvent.java
│   ├── listener/OrderCompletedEventListener.java
│   ├── repository/NotificationRepository.java
│   └── service/EmailService.java
├── src/main/resources/application.properties
├── Dockerfile
├── pom.xml
└── README.md
```

## 3. Messaging

| Item | Value |
|---|---|
| Exchange | `cake-delight` (topic, durable) |
| Queue | `notification-queue` (durable) |
| Binding / routing key | `order.completed` |
| Message format | JSON |

**Event received**

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

## 4. The email

| Part | Content |
|---|---|
| To | `customerEmail` of the order |
| Subject | `Order Confirmation - Cake Delight \| Order #<orderId>` |
| Body (HTML) | Greeting, order ID, customer name, email, delivery address, a table of cakes (name, quantity, subtotal) and the total |

SMTP: `smtp.gmail.com`, port 587, STARTTLS.

### Create a Gmail app password

1. Open your Google Account → Security.
2. Turn on 2-Step Verification.
3. Open App passwords and create one for "Mail".
4. Use the 16-character value as `MAIL_PASSWORD` and your Gmail address as `MAIL_USERNAME`.

## 5. Database

Database: `cake_notification_db`  Table: `notifications` (created by Hibernate, `ddl-auto=update`)

| Column | Description |
|---|---|
| `id` | Primary key |
| `order_id` | Order that triggered the notification |
| `customer_name` | Customer name |
| `customer_email` | Recipient |
| `subject` | Email subject |
| `message` | Reserved text column |
| `status` | `PENDING` → `SENT` or `FAILED` |
| `created_at` | Time the event was received |

## 6. Check that it works

1. Place an order (frontend or `POST /api/orders/checkout/{basketId}`) with an email address you can open.
2. The logs show the event and the result:

```bash
docker compose logs -f notification-service
```

```bash
kubectl logs -n cake-delight deployment/notification-service -f
```

Expected log lines: `Received order completion event. Order ID: ...` then `Order confirmation email sent successfully. Order ID: ...`

3. Check the mailbox of the customer email.
4. Check the database:

```sql
USE cake_notification_db;
SELECT id, order_id, customer_email, status, created_at FROM notifications;
```

`status` should be `SENT`.

5. Optional: in the RabbitMQ management UI (http://localhost:15672) open the Queues tab and see `notification-queue`.

## 7. Environment variables

| Variable | Meaning | Local example | Docker / Kubernetes |
|---|---|---|---|
| `MYSQL_HOST`, `MYSQL_PORT` | MySQL address | `localhost`, `3306` | `mysql`, `3306` |
| `CAKE_NOTIFICATION_DB` | Database name | `cake_notification_db` | `cake_notification_db` |
| `MYSQL_USERNAME`, `MYSQL_PASSWORD` | Database login | `root`, your password | Secret / `.env` |
| `RABBITMQ_HOST`, `RABBITMQ_PORT` | RabbitMQ address | `localhost`, `5672` | `rabbitmq`, `5672` |
| `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | RabbitMQ login | `guest`, `guest` | Secret / `.env` |
| `MAIL_USERNAME` | Gmail address | your Gmail | Secret / `.env` |
| `MAIL_PASSWORD` | Gmail app password | your app password | Secret / `.env` |

## 8. Run locally with Maven

Prerequisites: JDK 21, MySQL (port 3306), RabbitMQ (port 5672).

```sql
CREATE DATABASE IF NOT EXISTS cake_notification_db;
```

Linux / macOS:

```bash
cd notification-service
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export CAKE_NOTIFICATION_DB=cake_notification_db
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=your_mysql_password
export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest
export MAIL_USERNAME=your_gmail_address
export MAIL_PASSWORD=your_gmail_app_password
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
cd notification-service
$env:MYSQL_HOST="localhost"
$env:MYSQL_PORT="3306"
$env:CAKE_NOTIFICATION_DB="cake_notification_db"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="your_mysql_password"
$env:RABBITMQ_HOST="localhost"
$env:RABBITMQ_PORT="5672"
$env:RABBITMQ_USERNAME="guest"
$env:RABBITMQ_PASSWORD="guest"
$env:MAIL_USERNAME="your_gmail_address"
$env:MAIL_PASSWORD="your_gmail_app_password"
.\mvnw.cmd spring-boot:run
```

The queue `notification-queue` is created automatically when the service starts.

## 9. Run with Docker

```bash
cd notification-service
docker build -t cake-delight-notification-service .
```

Normally started with the whole system:

```bash
cd docker
docker compose up -d
```

## 10. Run on Kubernetes

Manifest: `k8s/notification-service.yaml` (Deployment on port 8083; ConfigMap for hosts, Secret for database, RabbitMQ and mail login).

```bash
kubectl apply -f k8s/notification-service.yaml
kubectl get pods -n cake-delight
kubectl logs -n cake-delight deployment/notification-service -f
```

The service is not reached over HTTP; it works through RabbitMQ.