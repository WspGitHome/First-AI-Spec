## ADDED Requirements

### Requirement: User Authentication
The system SHALL require users to authenticate with a username and password before accessing any protected pages or API endpoints.

#### Scenario: Successful login
- **WHEN** a user navigates to any protected page while not authenticated
- **THEN** the system SHALL redirect the user to the login page
- **WHEN** the user submits valid credentials (username: "admin", password: "admin")
- **THEN** the system SHALL create an authenticated session
- **AND** redirect the user to the originally requested page
- **AND** the user SHALL have access to all protected functionality

#### Scenario: Invalid credentials
- **WHEN** a user submits invalid credentials (incorrect username or password)
- **THEN** the system SHALL display an error message "用户名或密码错误"
- **AND** the user SHALL remain on the login page
- **AND** the user SHALL NOT be granted access to protected resources

#### Scenario: Session persistence
- **WHEN** a user has successfully authenticated
- **THEN** the system SHALL maintain the authentication session across subsequent requests
- **AND** the session SHALL remain valid for the configured timeout period (default: 30 minutes)
- **AND** the user SHALL NOT be required to re-authenticate for each protected resource

#### Scenario: Accessing protected API without authentication
- **WHEN** an unauthenticated user attempts to access a protected API endpoint (e.g., `/process-roster`)
- **THEN** the system SHALL return HTTP 401 Unauthorized or redirect to login
- **AND** the API endpoint SHALL NOT execute

### Requirement: Login Page
The system SHALL provide a login page with a user interface for username and password input.

#### Scenario: Login page accessibility
- **WHEN** a user is redirected to the login page
- **THEN** the system SHALL display a responsive HTML login form
- **AND** the form SHALL contain username and password input fields
- **AND** the form SHALL use Bootstrap 5 styling consistent with the existing application UI
- **AND** static resources (CSS, JS) SHALL be accessible without authentication

#### Scenario: Login form submission
- **WHEN** the user completes the login form and submits
- **THEN** the system SHALL send a POST request to `/login` with credentials
- **AND** the system SHALL process the authentication synchronously
- **AND** upon success, redirect to the protected page
- **AND** upon failure, display error message on the login page

#### Scenario: Password visibility toggle
- **WHEN** the user clicks the password field visibility toggle icon
- **THEN** the system SHALL toggle the password input between "password" and "text" type
- **AND** allow the user to view or hide their password input

### Requirement: Logout Functionality
The system SHALL provide a mechanism for users to explicitly terminate their authentication session.

#### Scenario: User logout
- **WHEN** an authenticated user clicks the logout button or link
- **THEN** the system SHALL invalidate the HTTP session
- **AND** clear all authentication data
- **AND** redirect the user to the login page
- **AND** subsequent requests SHALL require re-authentication

#### Scenario: Session expiration
- **WHEN** a user's authentication session expires due to timeout
- **THEN** the system SHALL invalidate the session
- **AND** redirect the user to the login page on the next request
- **AND** display a session expiration message

### Requirement: Credential Configuration
The system SHALL use configurable username and password credentials stored in application configuration.

#### Scenario: Default credentials
- **WHEN** the application starts with default configuration
- **THEN** the system SHALL accept username "admin" and password "admin"
- **AND** these credentials SHALL be defined in `application.yml`

#### Scenario: Custom credentials configuration
- **WHEN** an administrator modifies the `application.yml` configuration
- **THEN** the system SHALL use the configured credentials for authentication
- **AND** the credentials SHALL be specified using Spring Security's `{noop}` password encoding format

### Requirement: Static Resource Access
The system SHALL allow unauthenticated access to certain static resources required for the login page.

#### Scenario: CSS and JavaScript access
- **WHEN** an unauthenticated user loads the login page
- **THEN** the system SHALL serve CSS files from `/css/**` without authentication
- **AND** serve JavaScript files from `/js/**` without authentication
- **AND** serve the login page HTML without authentication

#### Scenario: Protected static resources
- **WHEN** an unauthenticated user attempts to access other static resources
- **THEN** the system SHALL require authentication for those resources
- **AND** redirect to the login page if not authenticated
