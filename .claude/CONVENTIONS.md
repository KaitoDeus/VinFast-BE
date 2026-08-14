# Engineering Conventions & Coding Guidelines

## 1. Java 21 & Spring Boot 3 Best Practices

### 1.1 Class & Component Architecture
- Prefer **immutable objects** where possible (using Java records or Lombok `@Builder` + `@Getter`).
- Keep services stateless and transactional:
  ```java
  @Service
  @Transactional
  public class VehicleService {
      private final VehicleRepository vehicleRepository;
      
      public VehicleService(VehicleRepository vehicleRepository) {
          this.vehicleRepository = vehicleRepository;
      }
  }
  ```

### 1.2 DTO & Presentation Layer
- **Mandate**: Never expose `@Entity` objects to REST responses.
- Implement explicit mappers (`*Mapper.java`) as Spring `@Component` beans.
- Avoid passing entity instances across architectural boundaries.

### 1.3 Validation & Preconditions
- Validate inputs using standard `jakarta.validation.constraints` (`@NotNull`, `@NotBlank`, `@Size`, `@Min`).
- Business rule violations must throw descriptive runtime exceptions:
  ```java
  if (!isValidVin(vin)) {
      throw new IllegalArgumentException("Invalid VIN format: " + vin);
  }
  ```

---

## 2. Testing Guidelines

### 2.1 Web Slice Tests (`@WebMvcTest`)
- Focus exclusively on the Controller layer, parameter parsing, HTTP status codes, and JSON serialization.
- Always include `.with(csrf())` for mutating verbs to account for Spring Security CSRF expectations in web slices:
  ```java
  mockMvc.perform(post("/api/v1/sc/vehicles")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(payload))
      .andExpect(status().isCreated());
  ```
- Use `@WithMockUser(roles = "SC_STAFF")` to simulate authenticated principals.

### 2.2 Integration Tests (`@SpringBootTest`)
- Use `@SpringBootTest` + `@AutoConfigureMockMvc` against the full application context.
- Verify end-to-end flows: Authentication -> JWT generation -> Protected endpoint consumption -> Audit record verification.

---

## 3. Git & Pull Request Standards

### 3.1 Commit Format
```
<type>(<scope>): <short summary in present tense>

- Detailed bullet point 1
- Detailed bullet point 2
```

### 3.2 Commit Types
- `feat`: Addition of new API endpoints, domain models, or business capabilities.
- `fix`: Resolution of bugs, exception fixes, or schema constraint corrections.
- `test`: Addition or refactoring of unit, integration, or security tests.
- `refactor`: Structural improvements with zero behavior change.
- `docs`: Documentation, swagger updates, `.claude` instructions.
- `chore`: Build dependencies, Flyway adjustments, CI scripts.
