# Additi Backend

A full-featured **e-commerce REST API** built with **Spring Boot 4**, providing product management, user authentication, cart & checkout, Bakong KHQR payments, and Cloudflare R2 file storage.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Features](#features)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Environment Variables](#environment-variables)
  - [Running Locally](#running-locally)
  - [Running with Docker](#running-with-docker)
- [API Endpoints](#api-endpoints)
  - [Authentication](#authentication)
  - [Users](#users)
  - [Products](#products)
  - [Categories](#categories)
  - [Orders & Checkout](#orders--checkout)
  - [Payments](#payments)
  - [Admin](#admin)
  - [Analytics](#analytics)
- [Database Schema](#database-schema)
- [Authentication & Authorization](#authentication--authorization)
- [Payment Integration (Bakong KHQR)](#payment-integration-bakong-khqr)
- [File Storage (Cloudflare R2)](#file-storage-cloudflare-r2)
- [Seed Data](#seed-data)
- [Error Handling](#error-handling)
- [Project Structure](#project-structure)

---

## Tech Stack

| Technology            | Description                                |
|-----------------------|--------------------------------------------|
| **Java 21**           | Programming language                       |
| **Spring Boot 4.0.2** | Application framework                     |
| **Spring Security**   | Authentication & authorization             |
| **Spring Data JPA**   | ORM / data access                          |
| **PostgreSQL**        | Relational database                        |
| **JWT (jjwt 0.11.5)** | Token-based authentication                |
| **AWS SDK for Java**  | Cloudflare R2 (S3-compatible) file storage |
| **Bakong KHQR**       | Cambodia NBC payment integration           |
| **Lombok**            | Boilerplate reduction                      |
| **Maven**             | Build & dependency management              |
| **Docker**            | Containerization                           |

---

## Features

- **User Registration & Authentication** — JWT access + refresh tokens via HTTP-only cookies
- **Role-Based Access Control** — `USER`, `ADMIN`, `MANAGER` roles
- **Product Management** — CRUD with variants (size, color, SKU), images, featured products, coming-soon status
- **Category Management** — CRUD with slug generation, activation toggle, product count
- **Shopping Cart** — Add/remove/update items with variant-aware pricing
- **Order & Checkout** — Create orders from cart, track order status lifecycle
- **Payment Processing** — Bakong KHQR QR code generation & verification, cash-on-delivery
- **File Upload** — Image upload to Cloudflare R2 with validation (type & size)
- **Product Analytics** — Dashboard stats, price analysis, category breakdown, best sellers
- **Advanced Filtering** — Search, filter by category/price/size/color/date, pagination & sorting
- **Seed Data** — Auto-seeds sample product and category on first run

---

## Architecture

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│   Client     │────▶│  Controller  │────▶│   Service    │
│  (Frontend)  │◀────│    Layer     │◀────│    Layer     │
└─────────────┘     └──────────────┘     └──────┬───────┘
                                                 │
                                    ┌────────────┼────────────┐
                                    │            │            │
                              ┌─────▼─────┐ ┌───▼────┐ ┌─────▼─────┐
                              │ Repository│ │  R2    │ │  Bakong   │
                              │  (JPA)    │ │ Storage│ │  KHQR API │
                              └─────┬─────┘ └────────┘ └───────────┘
                                    │
                              ┌─────▼─────┐
                              │ PostgreSQL│
                              └───────────┘
```

---

## Getting Started

### Prerequisites

- **Java 21** or later
- **Maven 3.9+**
- **PostgreSQL** database
- **Cloudflare R2** bucket (for image storage)
- *(Optional)* **Docker** for containerized deployment

### Environment Variables

Create an `env.properties` file in the project root (auto-imported via `spring.config.import`), or set the following environment variables:

| Variable               | Description                         | Default            |
|------------------------|-------------------------------------|--------------------|
| `PORT`                 | Server port                         | `8080`             |
| `DB_URL`               | PostgreSQL JDBC URL                 | —                  |
| `DB_USERNAME`          | Database username                   | —                  |
| `DB_PASSWORD`          | Database password                   | —                  |
| `JWT_SECRET`           | Secret key for signing JWTs         | —                  |
| `R2_ACCOUNT_ID`        | Cloudflare account ID               | —                  |
| `R2_ACCESS_KEY_ID`     | R2 access key                       | —                  |
| `R2_SECRET_ACCESS_KEY` | R2 secret key                       | —                  |
| `R2_BUCKET_NAME`       | R2 bucket name                      | —                  |
| `R2_ENDPOINT`          | R2 S3-compatible endpoint URL       | —                  |
| `R2_PUBLIC_URL`        | Public URL prefix for stored files  | —                  |
| `R2_REGION`            | R2 region                           | —                  |
| `BAKONG_TOKEN`         | Bakong API bearer token             | —                  |
| `BAKONG_MERCHANT_ID`   | Bakong merchant ID                  | —                  |
| `BAKONG_MERCHANT_NAME` | Bakong merchant display name        | `Additi Store`     |
| `BAKONG_ACCOUNT_ID`    | Bakong account ID                   | —                  |
| `BAKONG_CURRENCY`      | Payment currency                    | `USD`              |
| `BAKONG_API`           | Bakong API base URL                 | —                  |
| `MAX_FILE_SIZE`        | Max upload file size                | `10MB`             |
| `MAX_REQUEST_SIZE`     | Max request size                    | `10MB`             |

### Running Locally

```bash
# Clone the repository
git clone https://github.com/<your-username>/additi-backend.git
cd additi-backend

# Create env.properties with your configuration
cp env.properties.example env.properties   # then fill in values

# Build the project
./mvnw clean package -DskipTests

# Run the application
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080` by default.

### Running with Docker

```bash
# Build the Docker image
docker build -t additi-backend .

# Run the container
docker run -d \
  --name additi-backend \
  -p 8080:8080 \
  --env-file env.properties \
  additi-backend
```

The Dockerfile uses a **multi-stage build** with Amazon Corretto 21 and runs as a non-root user for security.

---

## API Endpoints

### Authentication

| Method | Endpoint              | Auth   | Description                                  |
|--------|-----------------------|--------|----------------------------------------------|
| POST   | `/api/auth/login`     | Public | Login with email & password; sets JWT cookies |
| POST   | `/api/auth/logout`    | Public | Revoke refresh token & clear cookies         |
| POST   | `/api/auth/refresh`   | Public | Refresh access token using refresh cookie    |
| GET    | `/api/auth/me`        | Auth   | Get current authenticated user profile       |

### Users

| Method | Endpoint              | Auth  | Description                              |
|--------|-----------------------|-------|------------------------------------------|
| POST   | `/api/users/register` | Public | Register a new user (multipart + photo)  |
| POST   | `/api/users`          | ADMIN  | Create user with assigned roles          |
| GET    | `/api/users`          | ADMIN  | List all users (paginated, sortable)     |
| GET    | `/api/users/{id}`     | ADMIN  | Get user by ID                           |
| PUT    | `/api/users/me`       | Auth   | Update own profile                       |
| PUT    | `/api/users/{id}`     | ADMIN  | Update any user                          |
| DELETE | `/api/users/{id}`     | ADMIN  | Delete a user                            |

### Products

| Method | Endpoint                           | Auth   | Description                              |
|--------|------------------------------------|--------|------------------------------------------|
| POST   | `/api/products`                    | ADMIN  | Create product (multipart: JSON + images)|
| GET    | `/api/products`                    | Public | List products (filter, search, paginate) |
| GET    | `/api/products/{id}`               | Public | Get product detail + related products    |
| GET    | `/api/products/best-sellers`       | Public | Get best-selling products                |
| GET    | `/api/products/featured`           | Public | Get featured products                    |
| GET    | `/api/products/coming-soon`        | Public | Get coming-soon products                 |
| PUT    | `/api/products/{id}`               | ADMIN  | Update product with images               |
| DELETE | `/api/products/{id}`               | ADMIN  | Delete product (+ R2 images)             |
| PUT    | `/api/admin/products/{id}/featured`| ADMIN  | Set featured status & order              |
| PUT    | `/api/admin/products/{id}/status`  | ADMIN  | Set product status & available date      |

**Filtering query parameters:** `search`, `categoryId`, `minPrice`, `maxPrice`, `size`, `color`, `startDate`, `endDate`, `page`, `size`, `sort`

### Categories

| Method | Endpoint               | Auth   | Description                              |
|--------|------------------------|--------|------------------------------------------|
| POST   | `/api/categories`      | ADMIN  | Create category                          |
| GET    | `/api/categories`      | Public | List categories (filterable)             |
| GET    | `/api/categories/{id}` | Public | Get category by ID (with product count)  |
| PUT    | `/api/categories/{id}` | ADMIN  | Update category                          |
| DELETE | `/api/categories/{id}` | ADMIN  | Delete category (blocked if products exist)|

### Orders & Checkout

| Method | Endpoint                    | Auth  | Description                           |
|--------|-----------------------------|-------|---------------------------------------|
| POST   | `/api/orders/checkout`      | Auth  | Create order from cart items          |
| GET    | `/api/orders`               | Auth  | Get current user's orders             |
| GET    | `/api/orders/all`           | ADMIN | Get all orders (filter by userId)     |
| GET    | `/api/orders/{orderId}`     | Auth  | Get single order                      |
| PUT    | `/api/orders/{orderId}/status` | Auth | Update order status                |

**Order Statuses:** `PENDING` → `CONFIRMED` → `SHIPPED` → `DELIVERED` → `CANCELLED`

### Payments

| Method | Endpoint                                              | Auth | Description                        |
|--------|-------------------------------------------------------|------|------------------------------------|
| POST   | `/api/orders/{orderId}/payment`                       | Auth | Create a generic payment record    |
| POST   | `/api/orders/{orderId}/payment/khqr`                  | Auth | Generate Bakong KHQR QR code      |
| POST   | `/api/orders/{orderId}/payment/cash`                  | Auth | Process cash-on-delivery payment   |
| GET    | `/api/orders/{orderId}/payment/{paymentId}/verify`    | Auth | Verify KHQR payment status (poll)  |

### Admin

| Method | Endpoint              | Auth    | Description         |
|--------|-----------------------|---------|---------------------|
| GET    | `/api/admin/home`     | ADMIN   | Admin home endpoint |
| GET    | `/api/admin/manager`  | MANAGER | Manager endpoint    |

### Analytics

| Method | Endpoint                  | Auth  | Description                             |
|--------|---------------------------|-------|-----------------------------------------|
| GET    | `/api/products/dashboard` | ADMIN | Product analytics & dashboard stats     |

Returns product counts, price statistics (min/max/avg), category breakdown, recent products, top products, and more.

---

## Database Schema

```
┌──────────┐       ┌──────────┐       ┌───────────────┐
│  users   │──M:M──│  roles   │       │refresh_tokens │
│          │       │          │       │               │
│ id       │       │ id       │       │ id            │
│ username │       │ name     │       │ token         │
│ email    │       └──────────┘       │ revoked       │
│ password │                          │ expires_at    │
│ phone    │◀──────────────────────── │ user_id (FK)  │
│ address  │                          └───────────────┘
│ photo    │
│ bio      │
└────┬─────┘
     │ 1:M
     ▼
┌──────────┐      ┌────────────┐      ┌──────────────────┐
│  orders  │──1:M─│ order_items│      │    categories    │
│          │      │            │      │                  │
│ id       │      │ id         │      │ id               │
│ total    │      │ quantity   │      │ name             │
│ status   │      │ price      │      │ slug             │
│ user_id  │      │ order_id   │      │ description      │
└────┬─────┘      │ product_id │      │ is_active        │
     │ 1:1        │ variant_id │      │ created_by (FK)  │
     ▼            └────────────┘      └────────┬─────────┘
┌──────────┐                                   │ 1:M
│ payments │                                   ▼
│          │      ┌────────────┐      ┌──────────────────┐
│ id       │      │   carts    │──1:M─│    products      │
│ method   │      │            │      │                  │
│ status   │      │ id         │      │ id               │
│ amount   │      │ user_id    │      │ name             │
│ md5_hash │      │ total_price│      │ description      │
│ khqr_code│      └─────┬──────┘      │ price            │
│ order_id │            │ 1:M         │ brand            │
└──────────┘            ▼             │ status           │
                  ┌────────────┐      │ is_featured      │
                  │ cart_items │      │ category_id (FK) │
                  │            │      └────────┬─────────┘
                  │ id         │               │ 1:M
                  │ quantity   │               ▼
                  │ price      │      ┌──────────────────┐
                  │ cart_id    │      │ product_variants │
                  │ product_id │      │                  │
                  │ variant_id │      │ id               │
                  └────────────┘      │ size             │
                                      │ color            │
                                      │ sku (unique)     │
                                      │ stock_quantity   │
                                      │ price_adjustment │
                                      │ product_id (FK)  │
                                      └────────┬─────────┘
                                               │ 1:M
                                               ▼
                                      ┌──────────────────┐
                                      │ product_images   │
                                      │                  │
                                      │ id               │
                                      │ image_url        │
                                      │ image_key        │
                                      │ variant_id (FK)  │
                                      └──────────────────┘
```

---

## Authentication & Authorization

- **Mechanism:** JWT with HTTP-only cookies (`accessToken` + `refreshToken`)
- **Cookie config:** `SameSite=None; Secure; HttpOnly; Path=/`
- **Access token lifetime:** 10 minutes (configurable)
- **Refresh token lifetime:** 2 hours (configurable, stored in database)
- **Password encoding:** BCrypt

### Role Hierarchy

| Role      | Access Level                                           |
|-----------|--------------------------------------------------------|
| `USER`    | Profile management, cart, checkout, orders, payments   |
| `ADMIN`   | All user permissions + product/category/user CRUD, analytics |
| `MANAGER` | Manager-specific endpoints                             |

### Public Endpoints (No Auth Required)

- `POST /api/auth/login`, `/api/auth/logout`, `/api/auth/refresh`
- `POST /api/users/register`
- `GET /api/products/**`, `GET /api/categories/**`
- `GET /api/public/**`

### CORS

Allowed origins: `localhost:3000`, `localhost:5173`, `localhost:4200`, `https://aduti-frontend.vercel.app`

---

## Payment Integration (Bakong KHQR)

[Bakong KHQR](https://bakong.nbc.gov.kh/) is Cambodia's national payment system by the National Bank of Cambodia.

### Payment Flow

```
1. Client calls POST /api/orders/{orderId}/payment/khqr
2. Server generates KHQR QR code via Bakong API (with local fallback)
3. Server returns QR code string + MD5 hash + payment ID
4. Client displays QR code for user to scan
5. Client polls GET /api/orders/{orderId}/payment/{paymentId}/verify
6. Server checks Bakong API for transaction status by MD5 hash
7. On success → payment status set to COMPLETED, order CONFIRMED
```

### Supported Payment Methods

| Method             | Description                           |
|--------------------|---------------------------------------|
| `KHQR`             | Bakong KHQR QR code payment           |
| `CASH_ON_DELIVERY`  | Cash payment on delivery              |

---

## File Storage (Cloudflare R2)

Images are stored in **Cloudflare R2** (S3-compatible object storage).

- **Allowed file types:** `.jpg`, `.jpeg`, `.png`, `.gif`, `.webp`
- **Max file size:** 10MB
- **Storage paths:**
  - Product images: `products/{productId}/variants/{variantId}/{timestamp}_{uuid}.{ext}`
  - User avatars: `users/avatar/{timestamp}_{uuid}.{ext}`
- **Cleanup:** Images are automatically deleted from R2 when their associated entity is deleted

---

## Seed Data

On first startup (when the `products` table is empty), the application seeds sample data:

| Entity            | Details                                                  |
|-------------------|----------------------------------------------------------|
| **Category**      | "Shoes" (slug: `shoes`)                                  |
| **Product**       | "Nike Air Max" — $120.00, brand: NIKE                    |
| **Variant 1**     | Size: M, Color: BLACK, SKU: `NIKEAIRMAX-BLK-M`, stock: 10 |
| **Variant 2**     | Size: L, Color: WHITE, SKU: `NIKEAIRMAX-WHT-L`, stock: 5, +$5.00 adjustment |

---

## Error Handling

The API uses a global exception handler returning consistent error responses:

| Exception                      | HTTP Status | Response                                 |
|--------------------------------|-------------|------------------------------------------|
| `ResourceNotFoundException`    | 404         | Resource not found message               |
| `FileStorageException`         | 500         | File storage error details               |
| `MaxUploadSizeExceededException`| 500        | "File size exceeds maximum limit of 10MB"|
| `IllegalArgumentException`     | 400         | Validation error message                 |
| `BadCredentialsException`      | 401         | "Invalid username or password"           |
| `BusinessValidationException`  | 500         | Business logic error                     |
| Unhandled exceptions           | 500         | "An unexpected error occurred"           |

### Standard API Response Format

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

---

## Project Structure

```
src/main/java/groupproject/additibackend/
├── AdditiBackendApplication.java      # Application entry point
├── config/
│   ├── BakongProperties.java          # Bakong KHQR configuration properties
│   ├── JwtFilter.java                 # JWT authentication filter
│   ├── JwtProperties.java             # JWT configuration properties
│   ├── R2Config.java                  # Cloudflare R2 S3 client configuration
│   ├── SecurityConfig.java            # Spring Security configuration
│   └── SeedDataConfig.java            # Initial data seeder
├── controller/
│   ├── AdminAPIRestController.java    # Admin-only endpoints
│   ├── AdminProductController.java    # Admin product management
│   ├── AuthController.java            # Authentication endpoints
│   ├── CategoryController.java        # Category CRUD
│   ├── OrderController.java           # Orders & payments
│   ├── ProductAnalyticsController.java# Analytics dashboard
│   ├── ProductController.java         # Product CRUD & search
│   ├── PublicAPIRestController.java   # Public/health endpoints
│   ├── TestController.java            # Testing endpoints
│   └── UserController.java            # User management
├── enumeration/                        # Enums (inline in models)
├── exception/
│   ├── BusinessValidationException.java
│   ├── FileStorageException.java
│   ├── GlobalExceptionHandler.java    # Centralized error handling
│   └── ResourceNotFoundException.java
├── khqr/                              # Bakong KHQR implementation
│   ├── BakongKHQR.java                # QR code generator/decoder
│   ├── CRCValidation.java             # CRC16-CCITT checksum
│   ├── IndividualInfo.java            # Individual payment info
│   ├── KHQRCurrency.java             # Currency enum (USD/KHR)
│   ├── KHQRData.java                 # QR data model
│   ├── KHQRDecodeData.java           # Decoded QR data
│   ├── KHQRResponse.java             # KHQR API response
│   └── KHQRStatus.java               # Transaction status
├── mapper/                            # Entity ↔ DTO mappers
├── model/                             # JPA entities
├── repository/                        # Spring Data JPA repositories
├── request/                           # Request DTOs
├── response/                          # Response DTOs
├── service/                           # Service interfaces
│   └── impl/                          # Service implementations
└── util/                              # Utility classes
```

---

## License

This project was developed as part of the **ADDITI Academy Group Project**.
