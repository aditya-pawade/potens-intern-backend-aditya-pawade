# System Architecture and Design Plan

This document outlines the architectural decisions, data modeling, and business logic for the Government Scheme Recommender backend, tracing the design from initial concept (Phase 1) through the final implementation.

## 1. Tech Stack
- **Language**: Java 8
- **Framework**: Spring Boot 2.7.14
- **Security**: Spring Security 5.7.x
- **Database**: MySQL 8.0
- **Migrations**: Flyway
- **Documentation**: OpenAPI / Swagger 3

---

## 2. Domain Modeling (Phase 1-4)

The data model is designed to be highly flexible, allowing administrators to configure arbitrary eligibility rules for government schemes without altering the database schema.

### Core Entities
1. **AppUser**: Handles authentication and authorization.
   - `id`, `username`, `password` (BCrypt), `role` (Enum: ADMIN, USER)
2. **Scheme**: Represents a government scheme.
   - `id`, `name`, `description`, `category`, `ministry`, `isActive`
3. **SchemeRule**: Represents a specific eligibility condition for a scheme.
   - `id`, `attribute` (Enum: AGE, INCOME, GENDER, etc.)
   - `ruleType` (Enum: ENUM_MATCH, MIN_VALUE, MAX_VALUE, RANGE_CHECK, BOOLEAN_CHECK)
   - `value` (String representation of the constraint)
   - `weight` (Integer: Points awarded if the rule passes)
   - `isMandatory` (Boolean: If true and rule fails, the user is instantly disqualified)

**Relationships:** A `Scheme` has a One-To-Many bidirectional relationship with `SchemeRule` with `CascadeType.ALL` and `orphanRemoval=true`.

---

## 3. Dynamic Rule Engine (Phase 5-7)

Instead of hardcoding business logic for every single government scheme, we implemented a **Dynamic Recommendation Engine**. 

### How it Works:
1. **Profile Parsing**: The user submits a JSON `ProfileRequest` (age, income, gender, state, rural status, etc.).
2. **Rule Evaluation**: The `RuleEvaluator` component interprets the string-based `value` of a `SchemeRule` based on its `ruleType`:
   - `RANGE_CHECK`: Parses `value` as `min,max` and mathematically checks if the profile attribute falls within the range.
   - `MIN_VALUE` / `MAX_VALUE`: Parses both the profile attribute and rule value as `Double` and performs boundary comparisons.
   - `ENUM_MATCH`: Performs strict string equality checks.
   - `BOOLEAN_CHECK`: Parses string values to native Booleans for true/false matching.
3. **Scoring Logic**:
   - The engine iterates through every active scheme and evaluates its rules against the profile.
   - If a rule passes, its `weight` is added to the `totalScore`.
   - If a rule marked as `isMandatory` fails, the scheme is instantly disqualified, regardless of other matching rules.
   - A final percentage is calculated: `percentage = totalScore / maxPossibleScore`.

---

## 4. Recommendation & API Layer (Phase 8-9)

### Service Layer Logic
The `RecommendationService` applies the final business constraints:
1. **Threshold Filtering**: Any scheme where the match percentage is below a configurable threshold (e.g., 50%) is discarded.
2. **Ranking**: Schemes are sorted in descending order by their match percentage. If percentages tie, the absolute `totalScore` is used as a tie-breaker.
3. **Truncation**: Only the **Top 3** schemes are returned to the user to prevent decision fatigue.

### REST API Endpoints
- **Public**:
  - `POST /api/recommend`: Accepts a user profile and returns the top 3 ranked schemes.
  - `POST /api/auth/login`: Accepts credentials and issues a JWT.
  - `POST /api/schemes/{id}/explain`: Runs a dry-run evaluation for a specific scheme and returns a detailed breakdown of which rules passed/failed and why.
- **Protected (Admin Only)**:
  - `GET/POST/PUT/DELETE /api/admin/schemes/**`: Standard CRUD endpoints for managing schemes and rules.

---

## 5. Security & Authentication (Phase 10-11)

The API boundary is protected using **Stateless JWT Authentication**.
1. **Filter Chain**: The `JwtAuthenticationFilter` intercepts HTTP requests, extracts the `Bearer` token, and parses the claims (username and role).
2. **Context Injection**: Valid tokens result in a `UsernamePasswordAuthenticationToken` being injected into the `SecurityContextHolder`.
3. **Deterministic Auth Manager**: For Spring Security 5.7 compatibility, a `DaoAuthenticationProvider` is explicitly configured to prevent auto-wiring ambiguity.
4. **Error Handling**: Missing tokens trigger a custom `JwtAuthenticationEntryPoint` (401), while permission failures trigger a `CustomAccessDeniedHandler` (403), ensuring the API always returns standard JSON instead of raw HTML error pages.

---

## 6. Global Exception Handling

A `@RestControllerAdvice` guarantees that the API degrades gracefully when bad data is provided.
- `MethodArgumentNotValidException` (400) -> Extracts and lists all missing/invalid fields.
- `HttpMessageNotReadableException` (400) -> Flags malformed JSON (e.g., sending "INVALID" for an Enum).
- `ResourceNotFoundException` (404) -> Safely rejects missing DB records.
- `DuplicateResourceException` (409) -> Prevents duplicate scheme names.
