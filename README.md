# SplitBilling

A full-stack **group expense sharing and settlement platform** built using a microservices architecture.

SplitBilling allows users to create groups, manage members, record shared expenses, track balances, and settle outstanding amounts. The system is designed as a distributed application with independent backend services communicating through REST APIs and event-driven messaging.

---

## Overview

Managing shared expenses across friends, roommates, trips, and groups can become complicated when multiple people contribute different amounts and need to settle balances later.

**SplitBilling** provides a centralized platform to:

- Create and manage expense groups
- Add and manage group members
- Record shared expenses
- Track individual balances
- Calculate outstanding amounts
- Manage settlements
- Support payment processing
- Handle authentication and user management
- Send notifications for relevant events

The backend is divided into independent microservices, while the frontend provides a responsive web interface for interacting with the platform.

---

## Architecture

SplitBilling follows a **microservices architecture** where business capabilities are separated into independently deployable services.

### High-Level Architecture

```text
                         ┌─────────────────────┐
                         │      Frontend       │
                         │ React + Vite +      │
                         │ Tailwind CSS        │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    API Gateway      │
                         │ Spring Cloud        │
                         │ Gateway              │
                         │      :8080          │
                         └──────────┬──────────┘
                                    │
          ┌─────────────────────────┼─────────────────────────┐
          │                         │                         │
          ▼                         ▼                         ▼
 ┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
 │  Auth Service   │       │  User Service   │       │  Group Service  │
 │      :8088      │       │      :8081      │       │      :8082      │
 └─────────────────┘       └─────────────────┘       └─────────────────┘
          │                         │                         │
          │                         │                         │
          └─────────────────────────┼─────────────────────────┘
                                    │
                         ┌──────────▼──────────┐
                         │   Expense Service   │
                         │       :8083         │
                         └──────────┬──────────┘
                                    │
                                    ▼
                              ┌───────────┐
                              │   Kafka   │
                              └─────┬─────┘
                                    │
                  ┌─────────────────┼─────────────────┐
                  │                 │                 │
                  ▼                 ▼                 ▼
        ┌─────────────────┐ ┌───────────────┐ ┌────────────────────┐
        │ Balance Service │ │ Notification  │ │ Settlement Service │
        │      :8084      │ │   Service     │ │       :8085        │
        └────────┬────────┘ │     :8087     │ └──────────┬─────────┘
                 │          └───────────────┘            │
                 ▼                                       ▼
          ┌─────────────┐                       ┌─────────────────┐
          │    Redis    │                       │ Payment Service │
          │    :6379    │                       │      :8086      │
          └─────────────┘                       └─────────────────┘

                       ┌─────────────────────┐
                       │       MySQL         │
                       │   Database Server   │
                       │       :3307         │
                       └─────────────────────┘
```

---

## Microservices

| Service | Port | Responsibility |
|---|---:|---|
| API Gateway | `8080` | Single entry point, routing, CORS and JWT resource-server integration |
| User Service | `8081` | User management and user data |
| Group Service | `8082` | Group creation and group membership |
| Expense Service | `8083` | Expense creation and expense-related operations |
| Balance Service | `8084` | Balance calculation and balance management |
| Settlement Service | `8085` | Settlement and outstanding balance management |
| Payment Service | `8086` | Payment-related operations and Razorpay integration |
| Notification Service | `8087` | Notification processing |
| Auth Service | `8088` | Authentication, JWT/JWK support and Google authentication integration |

---

## Technology Stack

### Backend

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Cloud Gateway
- Spring Kafka
- Flyway
- Maven

### Frontend

- React
- React DOM
- Vite
- Tailwind CSS
- Axios
- React Router
- React Hot Toast

### Infrastructure

- Docker
- Docker Compose
- MySQL
- Apache Kafka
- Redis
- Nginx

### Authentication & Payments

- JWT
- JWK endpoint
- Google Identity integration
- Razorpay test integration

---

## Service Responsibilities

### API Gateway

The API Gateway acts as the single entry point for the frontend.

Responsibilities include:

- Routing requests to backend services
- Centralizing API access
- CORS configuration
- JWT resource-server integration
- JWK-based token validation
- Hiding internal service endpoints from the frontend

Frontend requests are routed through:

```text
http://localhost:8080
```

---

### Auth Service

The authentication service is responsible for authentication-related functionality.

Key responsibilities include:

- Authentication
- JWT token handling
- JWK endpoint
- Google authentication integration
- Communication with the User Service

The service runs on:

```text
http://localhost:8088
```

---

### User Service

The User Service manages application users and user-related data.

Responsibilities include:

- User creation
- User lookup
- User information management
- User persistence

The service runs on:

```text
http://localhost:8081
```

---

### Group Service

The Group Service manages groups and their members.

Responsibilities include:

- Group creation
- Group retrieval
- Group membership
- Group ownership
- Member-related operations

The service runs on:

```text
http://localhost:8082
```

---

### Expense Service

The Expense Service manages shared expenses.

Responsibilities include:

- Creating expenses
- Managing expense information
- Associating expenses with groups
- Publishing expense-related events

The service runs on:

```text
http://localhost:8083
```

Expense events are published through Kafka for asynchronous processing by other services.

---

### Balance Service

The Balance Service is responsible for calculating and maintaining balances resulting from shared expenses.

Responsibilities include:

- Balance tracking
- Balance calculation
- Maintaining settlement-related balance information
- Consuming Kafka events
- Using Redis where appropriate for fast-access data

The service runs on:

```text
http://localhost:8084
```

---

### Settlement Service

The Settlement Service manages settlement operations between users.

Responsibilities include:

- Tracking settlement information
- Processing settlement requests
- Communicating with the Balance Service
- Maintaining settlement state

The service runs on:

```text
http://localhost:8085
```

---

### Payment Service

The Payment Service handles payment-related functionality.

Responsibilities include:

- Payment initiation
- Payment processing workflow
- Razorpay integration
- Communication with the Settlement Service

The service runs on:

```text
http://localhost:8086
```

---

### Notification Service

The Notification Service processes notification-related events.

It consumes events from Kafka and handles notification processing independently from the main request flow.

The service runs on:

```text
http://localhost:8087
```

---

## Event-Driven Communication

SplitBilling uses **Apache Kafka** for asynchronous communication between services.

A simplified event flow is:

```text
Expense Service
      │
      │ Expense Event
      ▼
    Kafka
      │
      ├──────────────► Balance Service
      │
      └──────────────► Notification Service
```

This approach reduces direct coupling between services and allows downstream processing to happen asynchronously.

Kafka is available internally to Docker services through:

```text
kafka:29092
```

and externally through:

```text
localhost:9092
```

---

## Data Storage

The application uses MySQL for persistent relational data.

Separate databases are maintained for the individual backend services:

```text
user_db
auth_db
group_db
expense_db
balance_db
settlement_db
payment_db
notification_db
```

This follows the microservices principle of keeping service-owned data separated.

### Database

MySQL runs inside Docker and is exposed on:

```text
localhost:3307
```

The internal Docker connection uses:

```text
mysql:3306
```

Database schema management is handled using **Flyway**.

---

## Redis

Redis is used by the Balance Service for fast-access data and caching-related functionality.

Docker services access Redis through:

```text
redis:6379
```

The host exposes Redis on:

```text
localhost:6379
```

---

## Frontend

The frontend is a React application built with Vite.

Main frontend technologies:

- React
- Vite
- Tailwind CSS
- Axios
- React Router
- React Hot Toast

The frontend communicates with the backend through the API Gateway:

```text
Frontend
   │
   ▼
API Gateway :8080
```

During Docker-based execution, the frontend is served using Nginx.

---

## Dockerized Architecture

The complete application can be run using Docker Compose.

The Docker environment contains:

```text
Frontend
API Gateway
Auth Service
User Service
Group Service
Expense Service
Balance Service
Settlement Service
Payment Service
Notification Service
MySQL
Kafka
Redis
```

### Container Communication

Services communicate using Docker Compose service names rather than `localhost`.

For example:

```text
user-service:8081
group-service:8082
expense-service:8083
balance-service:8084
settlement-service:8085
payment-service:8086
notification-service:8087
auth-service:8088
mysql:3306
kafka:29092
redis:6379
```

This allows the services to communicate correctly inside the Docker network.

---

## Running the Project with Docker

### Prerequisites

Install:

- Docker Desktop
- Git
- Java 22
- Maven
- Node.js 22+ if running the frontend separately

Docker Desktop should be running before starting the application.

---

### 1. Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd SplitBilling
```

---

### 2. Configure Environment Variables

Create a `.env` file in the project root.

Example:

```env
DB_USERNAME=root
DB_PASSWORD=<your-password>
MYSQL_ROOT_PASSWORD=<your-password>

AUTH_ISSUER=http://localhost:8088

GOOGLE_CLIENT_ID=<your-google-client-id>
GOOGLE_CLIENT_SECRET=<your-google-client-secret>

RAZORPAY_KEY_ID=<your-razorpay-test-key>
RAZORPAY_KEY_SECRET=<your-razorpay-test-secret>
```

> Do not commit real credentials or secrets to Git.

---

### 3. Build Backend JAR Files

Each backend service is packaged using Maven.

Example:

```bash
cd backend/services/user-service
mvn -B -DskipTests package
```

Repeat for the backend services.

The Dockerfiles use the generated JAR files as the runtime artifacts.

---

### 4. Build and Start the Docker Stack

Return to the project root:

```bash
cd ../../../..
```

Then run:

```bash
docker compose build
docker compose up -d
```

Check the running containers:

```bash
docker compose ps
```

---

### 5. Access the Application

Frontend:

```text
http://localhost:5173
```

API Gateway:

```text
http://localhost:8080
```

Gateway health endpoint:

```text
http://localhost:8080/actuator/health
```

---

## Useful Docker Commands

### View running services

```bash
docker compose ps
```

### View logs

```bash
docker compose logs -f
```

### View logs for a specific service

```bash
docker compose logs -f balance-service
```

### Restart the stack

```bash
docker compose restart
```

### Stop the stack

```bash
docker compose down
```

### Stop and remove containers

```bash
docker compose down
```

### Rebuild a specific service

```bash
docker compose build balance-service
```

### Recreate containers

```bash
docker compose up -d --force-recreate
```

---

## Project Structure

```text
SplitBilling/
│
├── backend/
│   └── services/
│       ├── api-gateway/
│       ├── auth-service/
│       ├── balance-service/
│       ├── expense-service/
│       ├── group-service/
│       ├── notification-service/
│       ├── payment-service/
│       ├── settlement-service/
│       └── user-service/
│
├── frontend/
│
├── infrastructure/
│   └── mysql/
│       └── init/
│           └── 01-create-databases.sql
│
├── docs/
│
├── docker-compose.yml
│
└── README.md
```

---

## API Structure

The API Gateway exposes service endpoints using versioned API paths.

Examples:

```text
/api/v1/auth/**
/api/v1/users/**
/api/v1/groups/**
/api/v1/expenses/**
/api/v1/balances/**
/api/v1/settlements/**
/api/v1/payments/**
/api/v1/notifications/**
```

The gateway routes these requests to the appropriate internal service.

---

## Security

Security-related components include:

- Spring Security
- JWT authentication
- JWK-based token validation
- Configurable JWT issuer
- Google Identity integration
- CORS configuration at the API Gateway

The API Gateway acts as the external entry point while internal services communicate over the Docker network.

---

## Database Migrations

Database schema changes are managed with **Flyway**.

Each service that owns a relational database maintains its migration scripts under:

```text
src/main/resources/db/migration/
```

Example:

```text
V1__create_balances_table.sql
```

Flyway validates and applies migrations when the service starts.

---

## Production Deployment Note

The project is fully Dockerized for local/integrated demonstration and development.

The complete architecture contains multiple backend services plus infrastructure components such as MySQL, Kafka and Redis. Deploying every component to a public cloud environment would require additional production infrastructure, networking, secrets management, observability and deployment configuration.

For the portfolio demonstration, the project is therefore demonstrated through the **Docker Compose environment**, while the separate deployed project provides the public deployment example.

---

## Screenshots

> Screenshots will be added after the final project demonstration recording.

Planned screenshots:

- Login / authentication
- Dashboard
- Group management
- Expense creation
- Expense history
- Balance view
- Settlement flow
- Payment flow
- Docker containers
- API / architecture overview

---

## Demo Video

> Project demonstration video will be added here.

**Demo:** Coming soon

The video will demonstrate:

1. Application login
2. Dashboard
3. Group creation and management
4. Adding members
5. Creating shared expenses
6. Balance calculation
7. Settlement workflow
8. Payment workflow
9. Dockerized microservices architecture

---

## Key Engineering Concepts Demonstrated

This project demonstrates practical experience with:

- Microservices architecture
- RESTful APIs
- API Gateway pattern
- JWT authentication
- JWK-based security
- Event-driven architecture
- Apache Kafka
- Redis
- MySQL
- Database-per-service architecture
- Flyway database migrations
- Docker
- Docker Compose
- Docker networking
- React frontend development
- REST client integration using Axios
- Asynchronous service communication
- Payment integration
- Service separation and independent responsibilities

---

## Challenges Solved

During Dockerization and integration, several real-world distributed-system issues were addressed, including:

### Service-to-Service Networking

Services initially designed around local development addresses needed to communicate correctly inside Docker.

The configuration was changed to use Docker Compose service names such as:

```text
mysql
kafka
redis
auth-service
user-service
```

instead of relying on container-local `localhost`.

### Kafka Listener Configuration

Kafka was configured with separate internal and external listeners so that:

- Docker services can communicate using `kafka:29092`
- Host applications can communicate using `localhost:9092`

### Database Initialization

The Docker environment automatically creates the service databases through:

```text
infrastructure/mysql/init/01-create-databases.sql
```

### Database Migration Consistency

Flyway migrations were aligned with the entity schema to ensure Hibernate validation succeeds when containers start.

### Frontend and Gateway Integration

The frontend communicates through the API Gateway rather than directly accessing individual backend services.

---

## Future Improvements

Potential future improvements include:

- Production cloud deployment
- Centralized logging
- Distributed tracing
- Metrics and monitoring
- Automated CI/CD pipeline
- Kubernetes deployment
- Improved fault tolerance
- Rate limiting
- Service-level observability
- Automated integration testing
- More comprehensive automated test coverage
- Production-grade secrets management

---

## Status

**Project Status: Functional and Dockerized**

The complete application stack has been integrated and tested locally using Docker Compose.

The project is currently maintained as a portfolio demonstration of full-stack development and microservices engineering.

---
## Author

**BALAJI GOND**

GitHub: [github.com/BalajiGond747](https://github.com/BalajiGond747)

LinkedIn: [linkedin.com/in/balajigond](https://linkedin.com/in/balajigond)

---

## License

This project is intended for educational and portfolio purposes.