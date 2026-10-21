# Nearby Market API

Backend-only REST API for nearby supplier products, customer carts, and supplier-managed orders. It uses Spring Boot, Spring Data JPA, Spring Security, and a file-backed H2 database.

## Run

Install JDK 21, then run from this directory. Add the `dev` profile only when you need the H2 web console:

```powershell
.\mvnw.cmd spring-boot:run
\.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

H2 stores data under `backend/data/nearby-market`. The development console is at `/h2-console` with JDBC URL `jdbc:h2:file:./data/nearby-market`, username `sa`, and an empty password. The console is disabled by default; change the database credentials before deploying outside development.

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