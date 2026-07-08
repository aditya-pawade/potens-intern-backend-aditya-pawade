# Government Scheme Recommender API

A Spring Boot REST API for recommending government schemes based on user profiles. 
Built as an assessment for Potens IT Services.

## Tech Stack
* **Java:** 8
* **Framework:** Spring Boot 2.7.14
* **Security:** Spring Security 5.7.x with JWT (Stateless)
* **Database:** MySQL 8.0 (Port 3307)
* **ORM:** Spring Data JPA
* **Migrations:** Flyway
* **API Docs:** OpenAPI / Swagger 3 (springdoc-openapi 1.7.0)

## Features
1. **Dynamic Rule Engine:** Recommends schemes based on 10 profile attributes using 5 rule types (`ENUM_MATCH`, `RANGE_CHECK`, `MIN_VALUE`, `MAX_VALUE`, `BOOLEAN_CHECK`).
2. **Scoring & Ranking:** Schemes are scored, percentage-ranked, and limited to the top 3 best matches.
3. **Admin Dashboard:** Full CRUD for government schemes and eligibility rules.
4. **Security:** JWT-based authentication. Roles `ADMIN` and `USER`.
5. **Seed Data:** 16 real-world Indian government schemes (PM-KISAN, Ayushman Bharat, etc.) seeded automatically via `DataLoader`.
6. **Detailed Explanations:** `explainScheme` endpoint reveals exactly why a profile qualifies or disqualifies.

## Setup Instructions

### 1. Database Configuration
The application expects a MySQL database running on **port 3307** with the following credentials:
* **Host:** localhost
* **Port:** 3307
* **Username:** root
* **Password:** root
* **Database:** `scheme_db`

Create the database before starting the application:
```sql
CREATE DATABASE scheme_db;
```

### 2. Build and Run
```bash
# Clean and build the project (runs 29 unit and integration tests)
mvn clean install

# Run the application
mvn spring-boot:run
```

### 3. Migrations & Seed Data
Flyway automatically creates tables (`V1__create_tables.sql`) and inserts seed users (`V2__seed_users.sql`).
The `DataLoader` component automatically populates 16 government schemes on the first run.

## Authentication (Seed Users)
Two users are pre-seeded in the database:

| Role  | Username | Password |
|-------|----------|----------|
| ADMIN | admin    | admin123 |
| USER  | user     | user123  |

Use the login endpoint to receive a JWT token:
```bash
POST http://localhost:8080/api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

## API Documentation
Interactive Swagger documentation is available at:
* **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

*Note: Click the "Authorize" button in Swagger UI and paste your JWT token to access protected Admin endpoints.*

## Key Architectural Decisions
1. **String-based Rule Evaluation**: Profile values and rule constraints are evaluated as Strings for simplicity and uniformity. 
2. **Explicit DAO Provider**: Used explicitly declared `DaoAuthenticationProvider` bean for deterministic Spring Security 5.7 configuration.
3. **No MapStruct**: Mappers are manually written for better interview explainability and to avoid annotation magic.
4. **Consistent Error Handling**: `GlobalExceptionHandler`, `JwtAuthenticationEntryPoint`, and `CustomAccessDeniedHandler` all return a unified JSON `ErrorResponse`.

## Stretch Goals & What is Unfinished
* **OpenAPI Spec (Completed)**: I successfully implemented the OpenAPI stretch goal. Auto-generated Swagger documentation is available at `/v3/api-docs` and a UI at `/swagger-ui/index.html`.
* **Caching Layer (Unfinished)**: I did not implement the caching stretch goal for the `/recommend` endpoint. 
    * *What I would have done*: I would integrate Spring Boot `@EnableCaching` with Redis. Since scheme eligibility rules change infrequently, caching the scheme list in Redis (with a TTL of 24 hours) would prevent the engine from hitting the MySQL database on every single recommendation request.
* **Webhook Subscription (Unfinished)**: I did not implement the `/subscribe` webhook stretch goal.
    * *What I would have done*: I would create a new `Subscription` entity linked to an `AppUser` containing a JSON payload of their profile. When a new scheme is created via `POST /api/admin/schemes`, an async Spring `@EventListener` or Kafka producer would trigger a background job to run the recommendation engine against all saved subscriptions, firing an HTTP POST request via Spring `WebClient` to any webhooks that match the new scheme.

## What I Would Build Next
1. **Typed Rule Values**: Refactor the rule engine to use strongly-typed values (`Integer`, `Double`, `Boolean`) instead of parsing strings during evaluation for better performance.
2. **User Registration & Profile Persistence**: Allow users to create accounts, save their profiles, and retrieve saved scheme recommendations later.
3. **Rate Limiting**: Implement Bucket4j to rate-limit the unauthenticated `/api/recommend` endpoint to prevent abuse.

## AI USE LOG
* **Tool used**: Google DeepMind Agent (Antigravity IDE)
* **Approximate usage**: 1 continuous agentic session (~55 messages/prompts).
* **What I used it for**: Pair-programming to architect the domain model, write boilerplate Spring Boot CRUD logic, configure the Spring Security 5.7 JWT filter chain, and write the 29-test automated test suite. Heavy usage for syntax recall and testing boilerplate.
