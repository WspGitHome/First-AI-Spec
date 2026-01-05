# Change: Add Authentication Login Page

## Why
The application currently has no access control, exposing sensitive attendance and duty roster management functionality to anyone who can access the URL. Adding authentication protects the system from unauthorized access and ensures only authorized personnel can process attendance data.

## What Changes
- Add a login page with username/password authentication
- Implement session-based authentication to protect web UI and API endpoints
- Use hardcoded credentials (username: `admin`, password: `admin`) for initial implementation
- Redirect unauthenticated users to login page
- Maintain session across requests using Spring Security with HTTP Session
- Logout functionality to terminate sessions

## Impact
- Affected specs: New capability - **authentication**
- Affected code:
  - New: `LoginController`, `AuthenticationService`, `SecurityConfig`
  - New: `login.html`, `login.css`, `login.js`
  - Modified: `pom.xml` (add Spring Security dependency)
  - Modified: `WebController` (adjust routing for login page)
  - Modified: `OrchestrationController`, `ExcelController` (add security constraints)
- Breaking changes: None (adding security, not changing existing functionality)
- User impact: Users must log in before accessing any page or API endpoint
