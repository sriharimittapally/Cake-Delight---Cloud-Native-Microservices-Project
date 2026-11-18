# Kubernetes Deployment (Docker Desktop)

Deploys Cake Delight into the namespace `cake-delight`. All Services are `ClusterIP`; the application is reached from `localhost` with `kubectl port-forward` for the **api-gateway** and the **frontend**.

## 1. Files

| File | Creates |
|---|---|
| `namespace.yaml` | Namespace `cake-delight` |
| `config.yaml` | ConfigMap `cake-delight-config` (non-secret settings) |
| `secret.yaml` | Secret `cake-delight-secrets` (passwords, mail login) – git-ignored |
| `mysql.yaml` | ConfigMap `mysql-init`, PVC `mysql-pvc` (1Gi), Deployment and Service `mysql` |
| `rabbitmq.yaml` | Deployment and Service `rabbitmq` (ports 5672 and 15672) |
| `catalog-service.yaml` | Deployment and Service `catalog-service` (8081) |
| `order-service.yaml` | Deployment and Service `order-service` (8082) |
| `rating-service.yaml` | Deployment and Service `rating-service` (8084) |
| `notification-service.yaml` | Deployment (and Service) `notification-service` (8083) |
| `api-gateway.yaml` | Deployment and Service `api-gateway` (8080) |
| `frontend.yaml` | Deployment and Service `frontend` (80) |

## 2. How configuration is supplied

**ConfigMap `cake-delight-config`**

| Key | Value |
|---|---|
| `MYSQL_HOST` | `mysql` |
| `MYSQL_PORT` | `3306` |
| `CAKE_CATALOG_DB` | `cake_catalog_db` |
| `CAKE_ORDER_DB` | `cake_order_db` |
| `CAKE_NOTIFICATION_DB` | `cake_notification_db` |
| `CAKE_RATING_DB` | `cake_rating_db` |
| `RABBITMQ_HOST` | `rabbitmq` |
| `RABBITMQ_PORT` | `5672` |
| `CATALOG_SERVICE_URL` | `http://catalog-service:8081` |
| `ORDER_SERVICE_URL` | `http://order-service:8082` |
| `RATING_SERVICE_URL` | `http://rating-service:8084` |

**Secret `cake-delight-secrets`**

| Key | Used by |
|---|---|
| `MYSQL_USERNAME`, `MYSQL_PASSWORD` | mysql (password), catalog, order, rating, notification |
| `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | rabbitmq, order, notification |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | notification |

The ConfigMap is loaded into the services with `envFrom`; the secret keys are injected as individual environment variables. The api-gateway only needs the ConfigMap (service URLs). The frontend needs neither.

### Secret template

`secret.yaml` is git-ignored, so create it yourself in this folder:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: cake-delight-secrets
  namespace: cake-delight

type: Opaque

stringData:
  MYSQL_USERNAME: "root"
  MYSQL_PASSWORD: "your_mysql_password"

  RABBITMQ_USERNAME: "cakedelight"
  RABBITMQ_PASSWORD: "your_rabbitmq_password"

  MAIL_USERNAME: "your_gmail_address"
  MAIL_PASSWORD: "your_gmail_app_password"
```

> Use a Gmail **app password**, not your normal password, and never commit this file.

## 3. Prerequisites

- Docker Desktop with **Kubernetes enabled** (Settings → Kubernetes → Enable Kubernetes).
- `kubectl` installed.
- All six images built locally (`cake-delight-*:latest`). The manifests use `imagePullPolicy: IfNotPresent`, so Docker Desktop uses these local images without a registry.
- Docker Compose containers stopped (`docker compose down` in `../docker`), so ports `8080` and `3000` are free.

Build the images if needed:

```bash
cd docker
docker compose build
cd ..
docker images | grep cake-delight
```

## 4. Select the cluster

Run all commands from the project root.

```bash
kubectl config get-contexts
kubectl config use-context docker-desktop
kubectl config current-context
kubectl cluster-info
kubectl get nodes
```

The node `docker-desktop` must be `Ready`.

## 5. Deploy – step by step

### 5.1 Namespace

```bash
kubectl apply -f k8s/namespace.yaml
kubectl get namespaces
```

Optional: use `cake-delight` as the default namespace for this context.

```bash
kubectl config set-context --current --namespace=cake-delight
```

### 5.2 ConfigMap and Secret

```bash
kubectl apply -f k8s/config.yaml
kubectl apply -f k8s/secret.yaml
kubectl get configmap -n cake-delight
kubectl get secrets -n cake-delight
```

### 5.3 Infrastructure

```bash
kubectl apply -f k8s/mysql.yaml
kubectl apply -f k8s/rabbitmq.yaml
kubectl get pods -n cake-delight
kubectl get pvc -n cake-delight
```

Wait until `mysql` and `rabbitmq` are `Running`.

### 5.4 Backend

```bash
kubectl apply -f k8s/catalog-service.yaml
kubectl apply -f k8s/order-service.yaml
kubectl apply -f k8s/rating-service.yaml
kubectl apply -f k8s/notification-service.yaml
kubectl apply -f k8s/api-gateway.yaml
```

### 5.5 Frontend

```bash
kubectl apply -f k8s/frontend.yaml
```

### 5.6 Verify

```bash
kubectl get pods -n cake-delight
kubectl get pods -n cake-delight -w
kubectl get svc -n cake-delight
kubectl get deployments -n cake-delight
kubectl get all -n cake-delight
```

Every pod should be `Running` and `1/1`. A few restarts in the first minute are normal when a service starts before MySQL or RabbitMQ is ready.

## 6. Access the application – port-forward

Use **two terminals** and keep both open.

Terminal 1 – API Gateway:

```bash
kubectl port-forward -n cake-delight svc/api-gateway 8080:8080
```

Terminal 2 – Frontend:

```bash
kubectl port-forward -n cake-delight svc/frontend 3000:80
```

| What | URL |
|---|---|
| Web application | http://localhost:3000 |
| API through the gateway | http://localhost:8080/api/cakes |
| Gateway health | http://localhost:8080/actuator/health |

Keep the ports exactly `8080` and `3000`: the browser calls the gateway at `http://localhost:8080/api`, and the gateway allows the origin `http://localhost:3000`.

Optional RabbitMQ management UI (third terminal):

```bash
kubectl port-forward -n cake-delight svc/rabbitmq 15672:15672
```

Open http://localhost:15672 and use the RabbitMQ values from `secret.yaml`.

## 7. Everyday commands

| Task | Command |
|---|---|
| Pods | `kubectl get pods -n cake-delight` |
| Services | `kubectl get svc -n cake-delight` |
| Secrets (names only) | `kubectl get secrets -n cake-delight` |
| ConfigMaps | `kubectl get configmap -n cake-delight` |
| Pod details and events | `kubectl describe pod <pod-name> -n cake-delight` |
| Logs of a service | `kubectl logs -n cake-delight deployment/order-service` |
| Follow logs | `kubectl logs -n cake-delight deployment/notification-service -f` |
| Restart a service | `kubectl rollout restart deployment/order-service -n cake-delight` |
| Scale a service | `kubectl scale deployment/catalog-service --replicas=2 -n cake-delight` |
| MySQL shell | `kubectl exec -it deployment/mysql -n cake-delight -- mysql -uroot -p` |

After rebuilding an image with `docker compose build <service>`, run `kubectl rollout restart deployment/<name> -n cake-delight` so the pod uses the new image.

## 8. Remove everything

```bash
kubectl delete namespace cake-delight
```

This removes all pods, services, the secret, the ConfigMaps and the MySQL volume claim (all data).

## 9. Notes

- Replicas are 1 for every component.
- `mysql-init` creates the four databases the first time MySQL starts with an empty volume.
- No NodePort or Ingress is used by design; access is only through the two port-forwards.