# Design: Authentication Implementation

## Context
The application is a Spring Boot 2.7.18 web application with:
- REST API endpoints for file processing (`/process-roster`, `/download/{transactionId}`)
- Static web UI (`/index.html`) for file upload
- Currently no authentication or authorization
- Java 8 compatibility requirement
- No database layer (uses in-memory processing)

## Goals / Non-Goals
**Goals**:
- Add simple username/password authentication
- Protect all web pages and API endpoints
- Use session-based authentication (no JWT, no OAuth)
- Hardcoded credentials for MVP (admin/admin)
- Minimal UI changes to existing application

**Non-Goals**:
- User registration or password management
- Role-based access control (single admin user)
- Password encryption beyond basic encoding (hardcoded credentials acceptable for internal tool)
- Remember-me functionality
- API for external authentication systems

## Decisions

### 1. Authentication Framework: Spring Security
**Decision**: Use Spring Security for authentication and authorization.

**Rationale**:
- Industry standard for Spring Boot applications
- Built-in session management
- Easy integration with existing Spring Boot app
- Good documentation and community support
- Provides filters for protecting endpoints

**Alternatives considered**:
- *Manual filter-based authentication*: More control but more code to maintain, reinventing the wheel
- *Apache Shiro*: Less popular with Spring Boot, steeper learning curve

### 2. Authentication Storage: HTTP Session (In-Memory)
**Decision**: Use server-side HTTP Session for storing authentication state.

**Rationale**:
- Simple and straightforward for single-server deployment
- No database required (consistent with current architecture)
- Spring Security provides built-in session management
- Sufficient for internal tool with limited concurrent users

**Alternatives considered**:
- *JWT tokens*: Overkill for simple session-based auth, adds complexity
- *Database-backed sessions*: Requires adding database dependency

### 3. Credentials Management: Hardcoded Configuration
**Decision**: Store username/password in `application.yml` using Spring Security's `{noop}` password encoding.

**Rationale**:
- Simplest implementation for MVP
- Acceptable for internal tool behind firewall
- Easy to update without code changes
- Can be enhanced later with externalized config or encryption

**Alternatives considered**:
- *Database users*: Requires database layer (adds significant complexity)
- *Properties file encryption*: Possible but more complex for initial implementation

### 4. Login Page: Standalone HTML Page
**Decision**: Create dedicated `login.html` page with Bootstrap styling to match existing UI.

**Rationale**:
- Consistent with existing static HTML approach
- Reuses Bootstrap 5 framework already in project
- Simple client-side form validation
- Server-side authentication via POST to `/login`

**Alternatives considered**:
- *Modal dialog on existing page*: Less secure (auth logic mixed with app logic)
- *Spring Boot default login page*: Ugly and doesn't match existing UI design

### 5. Endpoint Protection: Permit All vs. Authenticated
**Decision**:
- Public endpoints: `/login`, `/css/**`, `/js/**`, `/images/**`
- Protected endpoints: All other paths (`/**`)

**Rationale**:
- Login page and static resources must be accessible
- Protects all application functionality (web UI + API)
- Simple security rule configuration

**Security Config Structure**:
```java
http.authorizeRequests()
    .antMatchers("/login", "/css/**", "/js/**").permitAll()
    .anyRequest().authenticated()
    .and()
    .formLogin()
    .and()
    .logout();
```

## Implementation Architecture

### Layer Structure
```
controller/LoginController          (GET /login, POST /login)
config/SecurityConfig               (Spring Security configuration)
service/AuthenticationService       (credential validation)
model/LoginRequest                  (username/password DTO)
static/login.html                   (login page UI)
static/login.css                    (login page styles)
static/login.js                     (login page logic)
```

### Flow
1. User accesses any protected page (e.g., `/index.html`)
2. Spring Security detects unauthenticated request
3. Redirects to `/login` (custom login page)
4. User submits credentials (admin/admin)
5. `AuthenticationService` validates credentials
6. On success: Create session, redirect to original page
7. On failure: Show error message, stay on login page
8. Session persists across requests until logout or timeout

## Risks / Trade-offs

### Risk 1: Hardcoded Credentials in Config File
**Risk**: Credentials visible in version control if not careful
**Mitigation**:
- Use environment variables or external config in production
- Add `application.yml` to `.gitignore` if it contains sensitive data
- Document that credentials should be changed in production

### Risk 2: Session Fixation Attacks
**Risk**: Attacker could hijack session if session ID not changed on login
**Mitigation**: Spring Security automatically changes session ID on authentication (default behavior)

### Risk 3: No HTTPS
**Risk**: Credentials transmitted in plain text if not using HTTPS
**Mitigation**:
- Document that production deployment should use HTTPS
- Acceptable risk for internal tool behind firewall
- Can add reverse proxy (nginx) with SSL termination later

### Risk 4: Session Timeout
**Risk**: Users may lose work if session expires during long file processing
**Mitigation**:
- Set reasonable session timeout (e.g., 30 minutes)
- Show clear "session expired" message
- Auto-redirect to login page on 401/403 responses

## Migration Plan

### Phase 1: Dependencies
1. Add Spring Security dependency to `pom.xml`
2. Test build to ensure compatibility

### Phase 2: Backend
1. Create `SecurityConfig` class
2. Create `AuthenticationService` with hardcoded credential check
3. Create `LoginController` (optional, Spring Security handles most)
4. Update existing controllers if needed (should auto-protect)

### Phase 3: Frontend
1. Create `login.html` with form
2. Create `login.css` with styling matching existing design
3. Create `login.js` for form submission and error handling
4. Test login flow manually

### Phase 4: Testing
1. Test valid login (admin/admin)
2. Test invalid credentials
3. Test session persistence
4. Test logout
5. Test protected endpoints redirect to login
6. Test static resources still accessible

### Rollback Plan
If issues arise:
1. Remove Spring Security dependency from `pom.xml`
2. Delete authentication-related classes
3. Revert `WebController` changes (if any)
4. Application should return to pre-authentication state

## Open Questions
- Should session timeout be configurable? (Proposed: 30 minutes default)
- Should we add "remember me" checkbox for convenience? (Proposed: No, out of scope for MVP)
- Should login page support password "show/hide" toggle? (Proposed: Yes, if time permits)
