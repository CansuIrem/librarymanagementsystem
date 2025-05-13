# Library Management System

This project is a **comprehensive Library Management System** built with modern technologies such as **Spring Boot 3.3.11**, **Java 21**, and **PostgreSQL**. The system is designed to streamline the management of books, users, and borrowing processes within a library environment. It supports both librarians and patrons, providing secure and role-based access to different features.

## ✨ Key Features

### 📚 Book Management
- Add new books with detailed information (title, author, ISBN, publication date, genre).
- View book details.
- Search books by title, author, ISBN, or genre (with pagination).
- Update book information.
- Delete books.

### 👤 User Management
- Register users (librarians and patrons).
- Role-based access control for librarians and patrons.
- View, update, and delete user information (restricted to librarians).
- Secure JWT-based authentication.

### 🔄 Borrowing and Returning
- Borrow and return books with real-time stock tracking.
- Track due dates and borrowing history.
- Manage overdue books with report generation for librarians.

### 🔐 Security
- Spring Security with JWT for authentication and role-based authorization.
- Only authenticated users can access protected resources.
- Authorization ensures only librarians can manage sensitive resources.

### 📊 Monitoring & Observability
- Integrated **Prometheus** for metrics collection via Spring Boot Actuator.
- Integrated **Grafana** for visualizing application metrics and health.

### ⚙️ Developer Tooling
- RESTful API design with standard HTTP status codes.
- API documentation powered by **Swagger UI (OpenAPI)**.
- Postman collection for all endpoints.
- Structured logging for debugging and auditing.
- Extensive unit and integration testing using JUnit 5, Mockito, and H2 database.

### 🐳 Dockerized Deployment
- Docker Compose setup for PostgreSQL, Spring Boot App, Prometheus, and Grafana.
- Environment variables configured for easy local setup.

---

## 🛠️ Technology Stack

### Backend
- **Java 21**
- **Spring Boot 3.3.11**
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Validation
  - Spring WebFlux (for reactive features)
  - Spring Actuator (for monitoring)
- **MapStruct** – Object mapping
- **JWT (jjwt)** – Authentication and token handling
- **Lombok** – Boilerplate code reduction

### Database
- **PostgreSQL** – Relational database for production
- **H2** – In-memory database used for testing

### Monitoring
- **Prometheus** – Metrics collection
- **Grafana** – Metrics visualization
- **Micrometer** – Metrics instrumentation for Spring Boot

### Testing
- **JUnit 5**
- **Mockito**
- **Spring Boot Test**
- **Spring Security Test**
- **Reactor Test** – For WebFlux components

### Documentation
- **Springdoc OpenAPI** – Auto-generated Swagger UI for REST API

### Containerization
- **Docker** – For containerizing the application and services
- **Docker Compose** – For orchestrating multi-service environment

### Build Tool
- **Maven**

---

## 🚀 Running the Application Locally

You can run the application either **locally using Maven** or via **Docker Compose**.

---

### 🔧 Option 1: Using Maven (Local Development)

#### Prerequisites
- Java 21
- Maven
- PostgreSQL running on port `5432` (or configure the application to match your local setup)

#### Steps
1. **Clone the repository:**
   ```bash
   git clone <your-repo-url>
   cd libmanage
2. **Set up the PostgreSQL database manually:**
   ```bash
   CREATE DATABASE libmanage;
   CREATE USER postgres WITH PASSWORD '1Q2W3E4R5';
   GRANT ALL PRIVILEGES ON DATABASE libmanage TO postgres;
3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
4. **Access the application:**
   - Base URL: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html

---

### 🐳 Option 2: Using Docker Compose (Recommended)

#### Prerequisites
- Docker
- Docker Compose

#### Steps
1. **Go to the project root directory**

2. **Run the containers using Docker Compose:**
   ```bash
   docker-compose up --build
4. **Access the services:**
- **Application API**: [http://localhost:8080](http://localhost:8080)
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **PostgreSQL**: available at `localhost:5433`
- **Prometheus**: [http://localhost:9090](http://localhost:9090)
- **Grafana**: [http://localhost:3000](http://localhost:3000)
  - Login credentials: `admin / admin`

> ℹ️ Environment variables such as `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` are configured in `compose.yaml`.

---

## 📘 API Endpoints & Documentation

This project provides a fully documented REST API using **Swagger (OpenAPI 3)**.  
You can interact with the endpoints and explore request/response schemas using the integrated Swagger UI.

### 🔗 Swagger UI
- [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

### 📚 Main Endpoint Groups

| Tag | Description |
|-----|-------------|
| **Authentication** | Handles user registration and login, returns JWT tokens |
| **User Management** | Endpoints for librarians to manage users |
| **Book Management** | Create, read, update, delete, and search books |
| **Borrowing Management** | Borrow/return books, view borrowing history, and generate overdue reports |
| **Reactive Book Management** | Stream real-time updates for book availability using reactive programming |

---

### 🛡️ Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user (returns JWT token) |
| POST | `/api/auth/login` | Log in and retrieve JWT token |

---

### 👤 User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create a new user |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |

---

### 📘 Book Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/books` | Add a new book |
| GET | `/api/books/{id}` | Get book details |
| PUT | `/api/books/{id}` | Update a book |
| DELETE | `/api/books/{id}` | Delete a book |
| GET | `/api/books/search?keyword={value}&type={field}` | Search books by title, author, genre, or ISBN |

---

### 🔁 Borrowing Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/borrowings/borrow?userId={uuid}&bookId={uuid}` | Borrow a book |
| POST | `/api/borrowings/return/{borrowingId}` | Return a borrowed book |
| GET | `/api/borrowings/user/{userId}` | Get borrowing history of a user |
| GET | `/api/borrowings/stats` | Get system-wide borrowing statistics |
| GET | `/api/borrowings/overdue` | Get all overdue borrowings |
| GET | `/api/borrowings/overdue/report` | Generate a textual overdue report |

---

### ⚡ Reactive Book Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reactive/books/stream` | Stream real-time updates of book availability |

---
---

## 🗂️ Database Schema & Design

The application uses a relational database structure designed for flexibility and integrity. It is implemented using **PostgreSQL**, and entity relationships are managed via **Spring Data JPA**.

### 🧩 Entities & Relationships

Below is a textual representation of the core database schema:

---

#### 📚 Book

| Field           | Type        | Description               |
|----------------|-------------|---------------------------|
| `id`           | UUID        | Primary Key               |
| `title`        | String      | Title of the book         |
| `author`       | String      | Author of the book        |
| `isbn`         | String (13) | International identifier  |
| `genre`        | String      | Genre/category            |
| `publicationDate` | Date     | Date of publication       |
| `availableCount` | Integer   | Available copies to lend  |
| `totalCount`     | Integer   | Total number of copies    |

---

#### 👤 User

| Field           | Type   | Description               |
|----------------|--------|---------------------------|
| `id`           | UUID   | Primary Key               |
| `name`         | String | Full name                 |
| `email`        | String | Unique email address      |
| `password`     | String | Hashed password           |
| `phoneNumber`  | String | Contact number            |
| `role`         | Enum   | `LIBRARIAN` or `PATRON`   |

---

#### 🔁 Borrowing

| Field         | Type   | Description                         |
|--------------|--------|-------------------------------------|
| `id`         | UUID   | Primary Key                         |
| `userId`     | UUID   | References `User(id)`               |
| `bookId`     | UUID   | References `Book(id)`               |
| `borrowDate` | Date   | When the book was borrowed          |
| `dueDate`    | Date   | When the book is due to return      |
| `returnDate` | Date   | When the book was actually returned |
| `overdue`    | Bool   | Whether the book was overdue        |

---

### 🔗 Relationships

- One **User** can borrow many **Books** → `One-to-Many`
- One **Book** can appear in many **Borrowings** → `One-to-Many`
- All foreign key relationships are enforced via UUID references.

---

### 📝 Notes
- UUIDs are used for all entity IDs to ensure uniqueness.
- Borrowing logic includes validation for overdue status and availability.
- The schema is compatible with both PostgreSQL (prod) and H2 (test) environments.

---

## 📎 Additional Information

### 📏 Business Rules & Constraints

The application enforces several domain-specific business rules to ensure consistent and realistic library operations:

| Rule Description |
|------------------|
| 📚 A user cannot borrow more than **5 books** at the same time. |
| 📕 A book cannot be borrowed if `availableCount` is **0**. |
| ➕ When adding or updating a book, `availableCount` **cannot exceed** `totalCount`. |
| 🔁 A user cannot borrow the **same book more than once** before returning it. |
| 🛑 Only **librarians** can create, update, or delete users and books. |
| ✅ Authentication is required for **all operations**, except `/auth/register` and `/auth/login`. |
| ⏱️ Overdue status is automatically calculated based on `dueDate` and `returnDate`. |
| 📄 Borrowing reports and statistics are only accessible to users with the `LIBRARIAN` role. |

These rules are enforced through service-layer validation and custom exception handling.

### ✅ Validation & Error Handling
- All input fields (such as ISBN, email, phone number) are validated using **Spring Validation**.
- Global exception handling is implemented using `@ControllerAdvice`.
- Responses for validation and business logic errors are returned in a structured JSON format.

---

### 🔐 Security
- Authentication is implemented using **JWT**.
- Authorization is enforced with **Spring Security 6**, restricting access by roles (`LIBRARIAN`, `PATRON`).
- All endpoints (except auth) require a valid token in the `Authorization: Bearer <token>` header.

---

### 🧪 Testing
- Unit tests are written using **JUnit 5** and **Mockito**.
- Integration tests run against an **H2 in-memory database** and include full controller-service-repository coverage.
- JWT authentication is mocked during tests to simulate secure access.

---

### 📦 API Testing with Postman
- A **complete Postman Collection** is included for:
  - Auth flow (register/login)
  - CRUD operations for books and users
  - Borrowing and returning
  - Reactive stream testing
- Tokens are pre-configured via environments or can be manually set in the headers.

---

### 📈 Monitoring
- Application metrics are exposed via Spring Boot Actuator at:
  - `/actuator/prometheus`
- **Prometheus** collects these metrics and stores them.
- **Grafana** dashboards visualize:
  - HTTP request metrics
  - JVM memory usage
  - Book service activity (real-time with WebFlux)

---

### 🐳 Docker Tips
- All services (Spring Boot app, PostgreSQL, Prometheus, Grafana) are orchestrated via **Docker Compose**.
- To reset the system:
  ```bash
  docker-compose down -v
  docker-compose up --build
