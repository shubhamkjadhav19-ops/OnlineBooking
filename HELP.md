# Nearby Market API

Backend-only REST API for nearby supplier products, customer carts, and supplier-managed orders. It uses Spring Boot, Spring Data JPA, Spring Security, and PostgreSQL. H2 is used only for tests.

## Run

Install JDK 21 and configure the PostgreSQL connection with `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password`, then run from this directory:

```powershell
.\mvnw.cmd spring-boot:run
```

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