# Docker Compose Setup

Runs the complete Cake Delight system (infrastructure, five backend applications and the frontend) with one command.

## 1. Contents of this folder

```text
docker/
├── docker-compose.yml          Definition of all containers
├── .env.example                Template for the environment file
├── .env                        Your real values (git-ignored, create it yourself)
├── init/
│   └── 01-create-databases.sql Creates the four databases on first MySQL start
└── README.md
```

## 2. Containers

| Service | Container name | Image | Host port → container port |
|---|---|---|---|
| mysql | `cake-delight-mysql` | `mysql:8.0` | 3307 → 3306 |
| rabbitmq | `cake-delight-rabbitmq` | `rabbitmq:4-management` | 5672 → 5672, 15672 → 15672 |
| catalog-service | `catalog-service` | `cake-delight-catalog-service` | 8081 → 8081 |
| order-service | `order-service` | `cake-delight-order-service` | 8082 → 8082 |
| notification-service | `notification-service` | `cake-delight-notification-service` | 8083 → 8083 |
| rating-service | `rating-service` | `cake-delight-rating-service` | 8084 → 8084 |
| api-gateway | `api-gateway` | `cake-delight-api-gateway` | 8080 → 8080 |
| frontend | `cake-delight-frontend` | `cake-delight-frontend` | 3000 → 80 |

- **Network:** `cake-delight-network` (bridge). Containers reach each other by service name, for example `http://catalog-service:8081`.
- **Volume:** `mysql_data` keeps the MySQL data between restarts.
- **Startup order (`depends_on`):** mysql and rabbitmq → catalog → order → rating and notification → api-gateway → frontend.

## 3. Prerequisites

- Docker Desktop running.
- Docker Compose v2 (`docker compose version`).
- A Gmail app password if you want confirmation emails.

## 4. Environment file

```bash
cd docker
cp .env.example .env
```

On Windows PowerShell: `copy .env.example .env`

Edit `.env`:

```properties
MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_mysql_password

CAKE_CATALOG_DB=cake_catalog_db
CAKE_ORDER_DB=cake_order_db
CAKE_NOTIFICATION_DB=cake_notification_db
CAKE_RATING_DB=cake_rating_db

RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=cakedelight
RABBITMQ_PASSWORD=your_rabbitmq_password

CATALOG_SERVICE_URL=http://catalog-service:8081
ORDER_SERVICE_URL=http://order-service:8082
RATING_SERVICE_URL=http://rating-service:8084

MAIL_USERNAME=your_gmail_address
MAIL_PASSWORD=your_gmail_app_password
```

| Variable | Meaning |
|---|---|
| `MYSQL_HOST` / `MYSQL_PORT` | Must be `mysql` / `3306` (the MySQL container, inside the network) |
| `MYSQL_PASSWORD` | Also becomes the MySQL `root` password |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | Created in RabbitMQ at first start and used by order and notification services |
| `*_SERVICE_URL` | Internal addresses by container name |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Gmail address and a Gmail **app password** |

> `.env` holds secrets and is git-ignored. Never commit it.

## 5. Build the images

```bash
docker compose build
docker images | grep cake-delight
```

On PowerShell use `docker images | findstr cake-delight`.

Build a single image:

```bash
docker compose build catalog-service
```

## 6. Start the system

```bash
docker compose up -d
```

Build and start together:

```bash
docker compose up -d --build
```

## 7. Check the system

```bash
docker compose ps
docker compose logs -f api-gateway
docker compose logs --tail 100 order-service
```

Press `Ctrl + C` to stop following logs.

## 8. Open the application

| What | URL |
|---|---|
| Web application | http://localhost:3000 |
| API (through the gateway) | http://localhost:8080/api/cakes |
| Gateway health | http://localhost:8080/actuator/health |
| RabbitMQ management | http://localhost:15672 (RabbitMQ user and password from `.env`) |
| MySQL from your computer | `localhost:3307` |

Service ports (`8081`–`8084`) are also published, so each service can be tested directly with Postman.

## 9. Everyday commands

| Task | Command |
|---|---|
| Stop and remove containers | `docker compose down` |
| Stop and delete all database data | `docker compose down -v` |
| Restart one service | `docker compose restart order-service` |
| Rebuild and restart one service | `docker compose up -d --build order-service` |
| Open a MySQL shell | `docker exec -it cake-delight-mysql mysql -uroot -p` |
| List databases | `SHOW DATABASES;` inside the MySQL shell |
| Follow all logs | `docker compose logs -f` |

## 10. Notes

- `init/01-create-databases.sql` runs only when MySQL starts with an **empty** data volume. To run it again, use `docker compose down -v` and start again.
- The images built here (`cake-delight-*:latest`) are the same images used by the Kubernetes manifests in `../k8s`.
- If a service container stops on the very first start because MySQL was not ready, run `docker compose up -d` again.
- Before deploying to Kubernetes, run `docker compose down` so ports `8080` and `3000` are free for the port-forwards.