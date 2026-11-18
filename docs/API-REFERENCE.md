# API Reference (Gateway Level)

All calls go to the API Gateway: **`http://localhost:8080`**.
The detailed request and response bodies are in each service README; this file is the single list of every route and where it goes.

## 1. Conventions

| Item | Rule |
|---|---|
| Base URL | `http://localhost:8080` |
| Format | JSON (`Content-Type: application/json` for requests with a body) |
| Money | decimal numbers with two decimals, for example `500.00` (rupees) |
| Dates | ISO local date-time, for example `2026-11-17T10:15:30.123456` |
| IDs | numbers (`Long`) |
| Authentication | none |

Success status codes used: `200 OK`, `201 Created`, `204 No Content` (empty body).

## 2. Routing

| Path prefix | Service | Details |
|---|---|---|
| `/api/cakes/**` | Catalog Service | [catalog-service/README.md](../catalog-service/README.md) |
| `/api/baskets/**` | Order Service | [order-service/README.md](../order-service/README.md) |
| `/api/orders/**` | Order Service | [order-service/README.md](../order-service/README.md) |
| `/api/ratings/**` | Rating Service | [rating-service/README.md](../rating-service/README.md) |

## 3. All endpoints

### Catalog

| Method | Path | Description | Success |
|---|---|---|---|
| GET | `/api/cakes` | Available cakes, filters `name`, `category`, `minPrice`, `maxPrice` | 200 |
| GET | `/api/cakes/admin` | All cakes including unavailable | 200 |
| GET | `/api/cakes/{id}` | One cake | 200 |
| POST | `/api/cakes` | Add a cake | 201 |
| PATCH | `/api/cakes/{id}/availability` | Toggle availability | 204 |
| DELETE | `/api/cakes/{id}` | Delete a cake | 204 |

### Basket

| Method | Path | Description | Success |
|---|---|---|---|
| POST | `/api/baskets` | Create a basket | 201 |
| POST | `/api/baskets/{basketId}/items` | Add a cake | 200 |
| GET | `/api/baskets/{basketId}` | Get a basket | 200 |
| PUT | `/api/baskets/{basketId}/items/{itemId}` | Change quantity | 200 |
| DELETE | `/api/baskets/{basketId}/items/{itemId}` | Remove an item | 204 |

### Order

| Method | Path | Description | Success |
|---|---|---|---|
| POST | `/api/orders/checkout/{basketId}` | Check out | 201 |
| GET | `/api/orders` | All orders | 200 |
| GET | `/api/orders/{orderId}` | One order | 200 |
| GET | `/api/orders/customer?email=` | Orders of an email | 200 |
| GET | `/api/orders/{orderId}/verify-purchase?email=&cakeId=` | `true` / `false` | 200 |

### Rating

| Method | Path | Description | Success |
|---|---|---|---|
| POST | `/api/ratings` | Submit a rating | 201 |
| GET | `/api/ratings/cake/{cakeId}` | Reviews of a cake | 200 |
| GET | `/api/ratings/cake/{cakeId}/summary` | Average and count of a cake | 200 |
| GET | `/api/ratings/summary` | Summaries of all rated cakes | 200 |
| GET | `/api/ratings/customer?email=` | Ratings of an email | 200 |

### Gateway

| Method | Path | Description | Success |
|---|---|---|---|
| GET | `/actuator/health` | Health | 200 |
| GET | `/actuator/info` | Info | 200 |
| GET | `/actuator/gateway/routes` | Routes | 200 |

## 4. Quick calls (curl)

```bash
curl http://localhost:8080/api/cakes
curl "http://localhost:8080/api/cakes?category=Birthday&minPrice=300&maxPrice=800"
curl http://localhost:8080/api/cakes/1

curl -X POST http://localhost:8080/api/baskets

curl -X POST http://localhost:8080/api/baskets/1/items \
  -H "Content-Type: application/json" \
  -d '{"cakeId":1,"quantity":2}'

curl -X POST http://localhost:8080/api/orders/checkout/1 \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Srihari","customerEmail":"customer@example.com","deliveryAddress":"Karimnagar, Telangana"}'

curl "http://localhost:8080/api/orders/customer?email=customer@example.com"

curl -X POST http://localhost:8080/api/ratings \
  -H "Content-Type: application/json" \
  -d '{"orderId":1,"cakeId":1,"customerName":"Srihari","customerEmail":"customer@example.com","rating":5,"review":"Very tasty and fresh."}'

curl http://localhost:8080/api/ratings/summary
```

On Windows PowerShell use `curl.exe` instead of `curl`, or use Postman.

## 5. Typical call order of the frontend

1. `GET /api/cakes` and `GET /api/ratings/summary` – Cakes page.
2. `POST /api/baskets` – first "Add to basket" (basket ID is stored in the browser).
3. `POST /api/baskets/{id}/items` – add cakes; `PUT` / `DELETE` on items – edit the basket.
4. `POST /api/orders/checkout/{basketId}` – place the order.
5. `GET /api/orders/customer?email=` and `GET /api/ratings/customer?email=` – Orders page.
6. `POST /api/ratings` – rate a purchased cake.
7. Admin: `GET /api/cakes/admin`, `POST /api/cakes`, `PATCH /api/cakes/{id}/availability`, `DELETE /api/cakes/{id}`.