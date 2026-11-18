# Troubleshooting

## 1. Docker Compose

| Problem | Likely cause | Fix |
|---|---|---|
| `port is already allocated` / `address already in use` for 3000, 8080, 3307, 5672 … | Another program or an older stack uses the port | Stop it, or run `docker compose down`, then `docker compose up -d` |
| A service container has `Exited` right after the first start | It started before MySQL or RabbitMQ was ready | `docker compose up -d` again, or `docker compose restart <service>` |
| `variable is not set` warnings, empty passwords | `docker/.env` missing | `cp .env.example .env` and fill it in |
| Databases are missing (`Unknown database`) | `init/01-create-databases.sql` runs only on an empty volume | `docker compose down -v`, then `docker compose up -d` (deletes data) |
| Access denied for user `root` | `MYSQL_PASSWORD` was changed after the volume was created | `docker compose down -v` and start again, or use the original password |
| Code change not visible | Old image used | `docker compose up -d --build <service>` |
| Build is slow the first time | Maven downloads dependencies | Wait; later builds reuse the cache |

## 2. Kubernetes

| Problem | Likely cause | Fix |
|---|---|---|
| `error: context "docker-desktop" not found` | Kubernetes is not enabled | Docker Desktop → Settings → Kubernetes → Enable Kubernetes |
| `kubectl get nodes` shows `NotReady` | Cluster still starting | Wait a minute and retry |
| `ErrImagePull` / `ImagePullBackOff` | Image not available locally | `docker images \| grep cake-delight`; build with `docker compose build`; then `kubectl rollout restart deployment/<name> -n cake-delight` |
| Pod `CreateContainerConfigError` | Secret or ConfigMap missing | `kubectl get secrets,configmap -n cake-delight`; apply `config.yaml` and `secret.yaml`; check key names |
| `namespaces "cake-delight" not found` when applying | Namespace not created yet | `kubectl apply -f k8s/namespace.yaml` first |
| Pod in `CrashLoopBackOff` right after deploy | Service started before MySQL / RabbitMQ was ready | Wait; Kubernetes restarts it. Check `kubectl logs <pod> -n cake-delight` |
| Pod stays `Pending` | PVC not bound or cluster out of resources | `kubectl describe pod <pod> -n cake-delight`; `kubectl get pvc -n cake-delight` |
| `secret.yaml` missing in a fresh clone | The file is git-ignored | Create it from the template in `k8s/README.md` |
| Changed config but pods use old values | Pods read environment at start | `kubectl rollout restart deployment/<name> -n cake-delight` |
| `Unable to listen on port 8080` | Port used by Docker Compose or another app | `docker compose down`; stop the other program |
| Port-forward stops (`lost connection to pod`) | Pod restarted or terminal idle | Run the port-forward command again |
| MySQL data lost after cleanup | `kubectl delete namespace` removes the PVC | Expected; redeploy to start with fresh data |

## 3. Application

| Problem | Likely cause | Fix |
|---|---|---|
| Frontend shows "Cannot reach the server" | Gateway not reachable on `localhost:8080`, or the page is not opened on an allowed origin | Start the gateway (Compose) or the gateway port-forward (Kubernetes); open `http://localhost:3000` |
| Browser console: "blocked by CORS policy" | Frontend opened on a port the gateway does not allow | Use `localhost:3000` (Docker / Kubernetes), `5173` (dev) or `4173` (preview) |
| Cakes page empty | Catalog service down or no available cakes | `curl http://localhost:8080/api/cakes`; check catalog logs; check `GET /api/cakes/admin` |
| "Add to basket" fails | Order or Catalog Service not ready | Check `order-service` and `catalog-service` logs |
| Refreshing `/basket` or `/orders` shows "404 Not Found" | The Nginx container serves only the files, not the app routes | Open `http://localhost:3000/` and navigate with the menu |
| Order created but no email | Wrong Gmail address or app password, or Gmail blocked the login | Check `MAIL_USERNAME` / `MAIL_PASSWORD`; use a Gmail **app password**; read the Notification Service logs; `notifications.status` shows `FAILED` |
| No notification record at all | Event did not reach the service | RabbitMQ UI → Queues → `notification-queue`; check `RABBITMQ_*` values in Order and Notification Services |
| Rating is rejected | The email does not match the order's email, or the cake is not in that order, or it was already rated | Use exactly the checkout email; rate each cake once per order |
| Order Service cannot reach Catalog | Wrong `CATALOG_SERVICE_URL` | Docker / Kubernetes: `http://catalog-service:8081` |
| Catalog fails at startup | Database does not exist | Create `cake_catalog_db`; the service validates the schema created by `schema.sql` |
| Cake images show a placeholder | The sample image links are not reachable | Normal; use a working image URL when adding a cake |

## 4. Local run with Maven

| Problem | Likely cause | Fix |
|---|---|---|
| `Could not resolve placeholder 'MYSQL_HOST'` | Environment variables not set | Set them as shown in the service README |
| `Communications link failure` | MySQL not running on the given host and port | Start MySQL; check `MYSQL_HOST` and `MYSQL_PORT` |
| `Connection refused` to RabbitMQ | RabbitMQ not running | Start RabbitMQ; check `RABBITMQ_*` |
| `Unsupported class file major version` | Wrong Java version | Use JDK 21 |

## 5. Handy diagnostic commands

```bash
docker compose ps
docker compose logs --tail 100 <service>
docker images | grep cake-delight

kubectl get all -n cake-delight
kubectl describe pod <pod-name> -n cake-delight
kubectl logs -n cake-delight <pod-name> --previous
kubectl get events -n cake-delight --sort-by=.lastTimestamp
```