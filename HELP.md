# Nearby Market API

Backend-only REST API for nearby supplier products, customer carts, and supplier-managed orders. It uses Spring Boot, Spring Data JPA, Spring Security, and PostgreSQL by default. Set `PGHOST`, `PGPORT` (optional; defaults to `5432`), `PGDATABASE`, `PGUSER`, and `PGPASSWORD` for the database connection. SSL defaults to required; `PGSSLMODE` and `PGCHANNELBINDING` can override the defaults.

## Run

Install JDK 21 and set the PostgreSQL environment variables, then run from this directory. To use local file-backed H2 instead, activate the `dev` profile:

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

H2 stores data under `backend/data/marketplace`. The development console is at `/h2-console` with JDBC URL `jdbc:h2:file:./data/marketplace`, username `sa`, and an empty password.

## Authentication

Register at `POST /api/auth/register` with `name`, `email`, `password`, `role` (`SUPPLIER` or `CUSTOMER`), and optional `latitude`/`longitude`. Login at `POST /api/auth/login` verifies the credentials and returns the account profile. Protected endpoints use HTTP Basic authentication with that email and password; passwords are stored with BCrypt.

Set or refresh the authenticated account location with `PUT /api/account/location`, body `{"latitude":40.71,"longitude":-74.00}`.

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/products/search?query=apple&radiusKm=10` | Customer search around their saved location; defaults to a 10 km radius |
| GET, POST | `/api/supplier/products` | List or create the authenticated supplier's products |
| PUT, DELETE | `/api/supplier/products/{productId}` | Update or soft-delete an owned product |
| GET | `/api/customer/cart` | View the authenticated customer's cart |
| POST | `/api/customer/cart/items` | Add `{ "productId": 1, "quantity": 2 }` |
| PUT, DELETE | `/api/customer/cart/items/{itemId}` | Change quantity or remove a cart item |
| POST | `/api/customer/orders` | Place cart orders, split by supplier |
| GET | `/api/customer/orders` | View customer order history and current statuses |
| GET | `/api/customer/orders/{orderId}` | Track one customer order |
| GET | `/api/supplier/orders` | View orders for the authenticated supplier |
| PATCH | `/api/supplier/orders/{orderId}/decision` | Accept or reject with `{ "decision": "ACCEPT" }` or `REJECT` |

Order placement reserves inventory. Rejecting a pending order restores it; decisions are allowed once and only by the order's supplier. Customers must set a location before searching; search radius is capped at 100 km.