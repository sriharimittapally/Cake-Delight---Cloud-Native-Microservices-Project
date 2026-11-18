# Cake Delight – Frontend

The web application of Cake Delight. Customers browse and filter cakes, fill a basket, check out, find their orders by email and rate the cakes they bought. A demo **Admin Dashboard** adds, deletes and switches the availability of cakes.

| | |
|---|---|
| Technology | React 19.3.1, React Router 7.18.5, Vite 8.3.8, plain CSS |
| Node | 22.12 or newer (22.22.0 used) |
| Runtime in containers | Nginx (alpine), port 80 |
| Backend | API Gateway at `http://localhost:8080/api` |

Only three runtime packages are used (`react`, `react-dom`, `react-router-dom`). There is no UI library, CSS framework or state library.

## 1. Pages

| Page | Route | What it does |
|---|---|---|
| Home | `/` | Welcome page and how the shop works |
| Cakes | `/cakes` | List cakes, filter by name, category and price range, add to basket, see average ratings |
| Basket | `/basket` | Change quantities, remove items, see the total, check out (name, email, delivery address) |
| Orders | `/orders` | Enter an email, see its orders, rate each purchased cake |
| Admin Dashboard | `/admin` | Demo page: add a cake, delete a cake, switch a cake Available / Unavailable |

### How the customer is identified (no accounts)

- **Basket:** the backend returns a `basketId` when a basket is created. The browser keeps it in `localStorage`, so the basket survives a page refresh. After checkout it is forgotten and the next "Add to basket" starts a new basket.
- **Customer:** the name, email and address typed at checkout.
- **Orders and ratings:** on the Orders page the customer types the email used at checkout. The page loads that email's orders and the ratings already given. Each cake in an order shows **Rate**, or the stars already given.
- After checkout the Orders page opens by itself with the new order.

## 2. Project structure

```text
cake-delight-frontend/
├── index.html
├── package.json / package-lock.json
├── vite.config.js
├── Dockerfile
├── .dockerignore
├── .env.example
└── src/
    ├── main.jsx                 Starts the app (router + basket provider)
    ├── App.jsx                  Navbar and the list of pages
    ├── styles/global.css        Theme colours, reset, shared styles
    ├── api/
    │   ├── client.js            One fetch helper: base URL, JSON handling
    │   ├── cakesApi.js          List/filter, admin list, add, toggle availability, delete
    │   ├── basketApi.js         Create basket, add / update / remove items
    │   ├── ordersApi.js         Checkout, orders by email
    │   └── ratingsApi.js        Submit rating, summaries, ratings by email
    ├── context/BasketContext.jsx   Basket state for the whole app
    ├── utils/format.js          Price (₹) and date formatting
    ├── components/              One folder per component: Name.jsx + Name.css
    │   ├── Navbar  CakeCard  CakeFilters  CakeImage  StarRating  QuantityStepper
    │   ├── CheckoutForm  OrderCard  RatingModal  Modal  Message  Loader
    │   └── AddCakeForm  AdminCakeTable
    └── pages/                   One folder per page: Name.jsx + Name.css
        └── Home  Cakes  Basket  Orders  AdminDashboard
```

Rules used everywhere: a component and its CSS live together; class names start with the component name; pages fetch data and components only display it; every backend call goes through `src/api`.

## 3. Backend endpoints used

| Screen | Calls (through the gateway) |
|---|---|
| Cakes | `GET /api/cakes?name=&category=&minPrice=&maxPrice=`, `GET /api/ratings/summary` |
| Add to basket | `POST /api/baskets`, `POST /api/baskets/{id}/items` |
| Basket | `GET /api/baskets/{id}`, `PUT` and `DELETE /api/baskets/{id}/items/{itemId}` |
| Checkout | `POST /api/orders/checkout/{basketId}` |
| Orders | `GET /api/orders/customer?email=`, `GET /api/ratings/customer?email=` |
| Rate | `POST /api/ratings` |
| Admin | `GET /api/cakes/admin`, `POST /api/cakes`, `PATCH /api/cakes/{id}/availability`, `DELETE /api/cakes/{id}` |

Request and response bodies are documented in the service READMEs.

## 4. Configuration

The API address is read at **build time** from `VITE_API_BASE_URL`. If it is not set, `http://localhost:8080/api` is used.

```properties
VITE_API_BASE_URL=http://localhost:8080/api
```

The Docker image is built without this variable, so it uses the default `http://localhost:8080/api`. The browser calls the gateway directly, so the gateway must be reachable on `localhost:8080` (Docker Compose publishes it; on Kubernetes it is port-forwarded).

The gateway allows only the origins `http://localhost:3000` (Docker / Kubernetes), `http://localhost:5173` (`npm run dev`) and `http://localhost:4173` (`npm run preview`). Vite is configured with `strictPort`, so it stops instead of silently using another port.

## 5. Run for development

Prerequisites: Node 22.12 or newer, and the backend running (Docker Compose, or the gateway on port 8080).

```bash
cd cake-delight-frontend
npm install
npm run dev
```

Open http://localhost:5173.

| Command | What it does |
|---|---|
| `npm run dev` | Development server on port 5173 |
| `npm run build` | Production build into `dist/` |
| `npm run preview` | Serves the production build on port 4173 |

To use a different gateway address, copy `.env.example` to `.env`, change `VITE_API_BASE_URL` and restart `npm run dev`.

## 6. Run with Docker

The `Dockerfile` has two stages:

1. `node:22-alpine`: `npm install` and `npm run build`.
2. `nginx:alpine`: serves the built files from `/usr/share/nginx/html` on port 80.

Build the image:

```bash
cd cake-delight-frontend
docker build -t cake-delight-frontend .
```

Normally it is started with the whole system, published on port 3000:

```bash
cd docker
docker compose up -d
```

Open http://localhost:3000.

## 7. Run on Kubernetes

Manifest: `k8s/frontend.yaml` (Deployment and ClusterIP Service `frontend`, port 80).

```bash
kubectl apply -f k8s/frontend.yaml
kubectl get pods -n cake-delight
```

Open the application with two port-forwards (two terminals):

```bash
kubectl port-forward -n cake-delight svc/api-gateway 8080:8080
kubectl port-forward -n cake-delight svc/frontend 3000:80
```

Open http://localhost:3000.

## 8. Good to know

- **Cake pictures:** when an image cannot load, a placeholder is shown.
- **Prices** are shown in rupees (₹). Change `currency` in `src/utils/format.js` to use another currency.
- **Unavailable cakes** are hidden from the Cakes page; use the Admin Dashboard to switch them back.
- **Admin page:** a simulation without login.
- **Page refresh in the Nginx container:** opening a sub-page address directly (for example `/basket`) or refreshing it returns "404 Not Found". Open `http://localhost:3000/` and move between pages with the menu.