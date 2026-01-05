## 1. Dependencies and Configuration
- [x] 1.1 Add Spring Security dependency to `pom.xml`
- [x] 1.2 Add credential configuration to `application.yml` (username: admin, password: admin with `{noop}` encoding)
- [x] 1.3 Configure session timeout (30 minutes) in `application.yml`

## 2. Backend Security Configuration
- [x] 2.1 Create `SecurityConfig` class extending `WebSecurityConfigurerAdapter`
- [x] 2.2 Configure security rules:
  - Permit public access to `/login`, `/css/**`, `/js/**`
  - Require authentication for all other paths (`/**`)
- [x] 2.3 Configure form login with custom login page (`/login`)
- [x] 2.4 Configure logout functionality (logout URL, logout success URL)
- [x] 2.5 Disable CSRF for API endpoints (if needed for file upload)

## 3. Authentication Service
- [x] 3.1 Create `AuthenticationService` class
- [x] 3.2 Implement credential validation logic against configured credentials
- [x] 3.3 Return authentication success/failure responses

## 4. Login Controller
- [x] 4.1 Create `LoginController` class (if needed beyond Spring Security defaults)
- [x] 4.2 Add GET `/login` endpoint to serve login page
- [x] 4.3 Add POST `/login` endpoint handler (if custom logic needed)

## 5. Frontend Login Page
- [x] 5.1 Create `login.html` with Bootstrap 5 styling
- [x] 5.2 Add login form with:
  - Username input field
  - Password input field with show/hide toggle
  - Submit button
  - Error message display area
- [x] 5.3 Match styling with existing `index.html` design (dark theme, Bootstrap)

## 6. Frontend Styling
- [x] 6.1 Create `login.css` with styles matching existing application
- [x] 6.2 Style login form to be centered and responsive
- [x] 6.3 Add error message styling (red text or alert box)
- [x] 6.4 Add loading spinner for login submission

## 7. Frontend JavaScript
- [x] 7.1 Create `login.js` for form handling
- [x] 7.2 Implement form submission via AJAX/fetch
- [x] 7.3 Handle authentication success (redirect to original page or home)
- [x] 7.4 Handle authentication failure (display error message)
- [x] 7.5 Implement password visibility toggle functionality

## 8. Update Existing UI
- [x] 8.1 Add logout button/link to `index.html` header or navigation
- [x] 8.2 Style logout button to match existing UI design
- [x] 8.3 Ensure logout functionality is accessible from all protected pages

## 9. Controller Updates
- [x] 9.1 Review `OrchestrationController` - ensure it works with Spring Security (should auto-protect)
- [x] 9.2 Review `ExcelController` - ensure it works with Spring Security (should auto-protect)
- [x] 9.3 Review `WebController` - adjust routing if needed for login page

## 10. Testing - Authentication Flow
- [x] 10.1 Start application and attempt to access `/index.html` without authentication
- [x] 10.2 Verify redirect to `/login` occurs
- [x] 10.3 Test login with valid credentials (admin/admin)
- [x] 10.4 Verify successful login and redirect to original page
- [x] 10.5 Test login with invalid credentials
- [x] 10.6 Verify error message displays correctly

## 11. Testing - Session Management
- [x] 11.1 Verify session persists across multiple page requests
- [x] 11.2 Test logout functionality
- [x] 11.3 Verify logout invalidates session and redirects to login
- [x] 11.4 Test accessing protected API endpoint after logout
- [x] 11.5 Verify session timeout behavior (wait 30 minutes or configure shorter timeout for testing)

## 12. Testing - API Endpoints
- [x] 12.1 Test accessing `/process-roster` without authentication (should fail or redirect)
- [x] 12.2 Test accessing `/process-roster` with valid authentication (should succeed)
- [x] 12.3 Test accessing `/download/{transactionId}` without authentication (should fail)
- [x] 12.4 Test file upload functionality through authenticated session

## 13. Testing - Static Resources
- [x] 13.1 Verify CSS and JS files load on login page without authentication
- [x] 13.2 Verify protected static resources require authentication

## 14. Documentation
- [x] 14.1 Update `openspec/project.md` to document authentication feature
- [x] 14.2 Document credential configuration in comments or README
- [x] 14.3 Document session timeout configuration

## 15. Validation and Cleanup
- [x] 15.1 Run `openspec validate add-authentication --strict`
- [x] 15.2 Fix any validation errors
- [x] 15.3 Verify all tasks are completed
- [x] 15.4 Update this `tasks.md` to mark all items as `- [x]`
