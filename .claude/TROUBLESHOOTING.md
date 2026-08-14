# Troubleshooting Runbook & Known Architectural Patterns

This guide documents recurring technical issues, debugging playbooks, and solutions encountered in the EV Warranty System.

---

## 1. Security & Authentication Pitfalls

### 1.1 HTTP 403 Forbidden in Web Slice Tests (`@WebMvcTest`)
- **Symptom**: `mockMvc.perform(post(...))` or `patch(...)` returns `403 Forbidden` despite having `@WithMockUser`.
- **Root Cause**: Spring Security enables CSRF defense by default in web slice tests unless explicitly suppressed or accompanied by a CSRF token.
- **Resolution**: Always include `.with(csrf())` from `SecurityMockMvcRequestPostProcessors`:
  ```java
  import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

  mockMvc.perform(patch("/api/v1/sc/vehicles/1/mileage")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"mileage\": 20000}"))
      .andExpect(status().isOk());
  ```

### 1.2 AccessDeniedException Mapping (403 vs 500)
- **Symptom**: Method security failure throwing `AccessDeniedException` resulting in HTTP 500 instead of HTTP 403.
- **Root Cause**: Unhandled exception bubbling up past the Spring Security Filter Chain.
- **Resolution**: `GlobalExceptionHandler` must explicitly catch `org.springframework.security.access.AccessDeniedException` and return `ResponseEntity.status(HttpStatus.FORBIDDEN)`.

---

## 2. Jackson & JPA Entity Serialization

### 2.1 Infinite Recursion / StackOverflowError
- **Symptom**: Infinite recursion during JSON serialization of entities having bidirectional associations (`User` <-> `Role`, `Vehicle` <-> `VehiclePart`).
- **Root Cause**: Jackson attempts to serialize circular object graphs indefinitely.
- **Resolution**: Place `@JsonIgnore` on the non-owning side of the association:
  ```java
  @JsonIgnore
  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<VehiclePart> installedParts = new ArrayList<>();
  ```
- **Best Practice**: Always return DTOs (`VehicleDTO`, `VehiclePartDTO`) instead of direct entities.

### 2.2 Password Field Leakage in REST Output
- **Symptom**: Hashed password string exposed in `/api/v1/auth/me` or user listings.
- **Resolution**: Annotate `password` in `User.java` with `@JsonIgnore`.

---

## 3. Database & Flyway Schema Migrations

### 3.1 Migration Checksum Mismatch (`FlywayValidateException`)
- **Symptom**: `Validate failed: Migrations have failed validation: Migration checksum mismatch for migration version X`.
- **Root Cause**: An already executed migration script in `db/migration/` was modified locally after being recorded in the `flyway_schema_history` table.
- **Resolution**: 
  - Never edit an existing migration script.
  - If necessary in development, execute a cleanup query on `flyway_schema_history` to delete the offending row, or create a corrective forward migration (e.g. `V4__...sql`).

### 3.2 JPA `@JoinColumn` vs Database Column Mismatch
- **Symptom**: `null value in column "claim_id" violates not-null constraint`.
- **Root Cause**: `@JoinColumn(name = "warranty_claim_id")` used in Java while PostgreSQL table defined `claim_id`.
- **Resolution**: Align the entity annotation `@JoinColumn(name = "claim_id")` with the exact column name in `V1__init_schema.sql`.

---

## 4. Testing Execution Recipes

```bash
# Execute unit tests for Vehicle domain only
mvn test -Dtest=Vehicle*

# Execute integration tests only
mvn test -Dtest=*IntegrationTest

# Run all tests with debug stacktraces
mvn test -e
```
