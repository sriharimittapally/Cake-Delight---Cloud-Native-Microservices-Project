# Testing Guide

Manual end-to-end test of the complete system, using the web application or Postman / curl. Start the system first (Docker Compose or Kubernetes). All API calls go to `http://localhost:8080`.

Use an email address you can open, so you can also check the confirmation email.

## 1. Pre-checks

| # | Check | Expected result |
|---|---|---|
| 1 | Docker Compose: `docker compose ps` / Kubernetes: `kubectl get pods -n cake-delight` | All components running |
| 2 | `GET http://localhost:8080/actuator/health` | `"status":"UP"` |
| 3 | `GET http://localhost:8080/api/cakes` | List of available cakes |
| 4 | Open `http://localhost:3000` | Cake Delight home page |

## 2. API test scenario

| Step | Request | Expected |
|---|---|---|
| 1 | `GET /api/cakes` | 200, list of cakes with `id`, `name`, `price` |
| 2 | `GET /api/cakes?category=Birthday&minPrice=300&maxPrice=800` | 200, only matching cakes |
| 3 | `GET /api/cakes/1` | 200, the cake with id 1 |
| 4 | `POST /api/baskets` | 201, `basketId`, status `ACTIVE`, no items |
| 5 | `POST /api/baskets/{basketId}/items` body `{"cakeId":1,"quantity":2}` | 200, one item, subtotal = price × 2 |
| 6 | `POST /api/baskets/{basketId}/items` with the same cake again, quantity 1 | 200, the item's quantity is now 3 |
| 7 | `PUT /api/baskets/{basketId}/items/{itemId}` body `{"quantity":1}` | 200, quantity 1, total updated |
| 8 | `GET /api/baskets/{basketId}` | 200, current basket |
| 9 | `POST /api/orders/checkout/{basketId}` with name, email, address | 201, status `PLACED`, order items copied from the basket |
| 10 | `GET /api/orders/customer?email=<your email>` | 200, your order |
| 11 | `GET /api/orders/{orderId}/verify-purchase?email=<your email>&cakeId=1` | 200, `true` |
| 12 | `POST /api/ratings` with that order and cake, rating 5 | 201, rating saved |
| 13 | `GET /api/ratings/cake/1` | 200, your review |
| 14 | `GET /api/ratings/cake/1/summary` | 200, `averageRating` 5.0, `totalRatings` 1 |
| 15 | `GET /api/ratings/summary` | 200, includes cake 1 |
| 16 | `GET /api/ratings/customer?email=<your email>` | 200, your rating |

## 3. Event and email test

After step 9:

| Check | How | Expected |
|---|---|---|
| Event published | Order Service logs: `Order completion event published` | Message present |
| Event consumed | Notification Service logs: `Received order completion event` | Message present |
| Email sent | Notification Service logs: `Order confirmation email sent successfully` | Message present |
| Mailbox | Open the customer's inbox | Email "Order Confirmation - Cake Delight \| Order #…" with the cake table and total |
| Database | `SELECT order_id, status FROM cake_notification_db.notifications;` | Status `SENT` |
| RabbitMQ | Management UI → Queues → `notification-queue` | Queue exists, 0 ready messages |

Log commands:

```bash
docker compose logs --tail 50 order-service
docker compose logs --tail 50 notification-service
```

```bash
kubectl logs -n cake-delight deployment/order-service --tail=50
kubectl logs -n cake-delight deployment/notification-service --tail=50
```

## 4. Admin test

| Step | Request | Expected |
|---|---|---|
| 1 | `GET /api/cakes/admin` | 200, all cakes |
| 2 | `POST /api/cakes` with a new cake (`available: true`) | 201, new cake with an `id` |
| 3 | `GET /api/cakes` | The new cake appears |
| 4 | `PATCH /api/cakes/{id}/availability` | 204 |
| 5 | `GET /api/cakes` | The cake is no longer listed |
| 6 | `GET /api/cakes/admin` | The cake is listed with `"available": false` |
| 7 | `PATCH /api/cakes/{id}/availability` again | 204; the cake is available again |
| 8 | `DELETE /api/cakes/{id}` | 204 |

## 5. Frontend test

| # | Action | Expected |
|---|---|---|
| 1 | Open `http://localhost:3000` | Home page |
| 2 | Go to **Cakes** | Cake cards with prices in ₹ |
| 3 | Use the filters (name, category, price range) | List narrows to matching cakes |
| 4 | Click **Add to basket** | Basket count increases |
| 5 | Open **Basket**, change a quantity, remove an item | Totals update |
| 6 | Fill name, email, address and **Check out** | Orders page opens with the new order |
| 7 | Check the mailbox | Confirmation email received |
| 8 | On **Orders**, click **Rate** on a cake, choose stars, add a review | Stars appear on that cake |
| 9 | Go to **Cakes** | The average rating shows on the cake |
| 10 | Refresh the browser on **Basket** after adding a cake | Basket is still there (saved in the browser) |
| 11 | Open **Admin Dashboard**: add a cake, switch it Unavailable, delete it | Table updates; Unavailable cakes disappear from **Cakes** |

## 6. Persistence test

1. Place an order.
2. Restart one service, for example `docker compose restart order-service` or `kubectl rollout restart deployment/order-service -n cake-delight`.
3. `GET /api/orders` still returns the order (data is in MySQL).

## 7. Kubernetes checks

| Check | Command | Expected |
|---|---|---|
| Namespace | `kubectl get namespaces` | `cake-delight` listed |
| Pods | `kubectl get pods -n cake-delight` | 8 pods `Running`, `1/1` |
| Services | `kubectl get svc -n cake-delight` | All `ClusterIP` |
| Secrets | `kubectl get secrets -n cake-delight` | `cake-delight-secrets` |
| Volume | `kubectl get pvc -n cake-delight` | `mysql-pvc` `Bound` |
| Port-forward | `kubectl port-forward -n cake-delight svc/api-gateway 8080:8080` and `svc/frontend 3000:80` | Application opens on `http://localhost:3000` |