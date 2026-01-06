## ADDED Requirements

### Requirement: Admin-Only User Registration
The system SHALL allow an admin user to register new users.

#### Scenario: Admin registers a new user successfully
- **GIVEN** a user is logged in with the `ROLE_ADMIN`
- **WHEN** the admin user navigates to the registration page and submits a valid new username and password
- **THEN** a new user account SHALL be created in the database with the `ROLE_USER`.

#### Scenario: Non-admin user attempts to access registration
- **GIVEN** a user is logged in without the `ROLE_ADMIN`
- **WHEN** the user attempts to access the registration page or its corresponding API endpoint
- **THEN** the system SHALL deny access.

### Requirement: User Profile Display
The system SHALL display the current user's information in a "User Center".

#### Scenario: User views their profile
- **GIVEN** any user is logged in
- **WHEN** the user navigates to the "User Center"
- **THEN** their username and account details SHALL be displayed.

### Requirement: Change Password
The system SHALL allow a user to change their own password.

#### Scenario: User successfully changes their password
- **GIVEN** a user is logged in
- **WHEN** the user provides their correct old password and a valid new password in the "User Center"
- **THEN** their password SHALL be updated in the database.

#### Scenario: User provides incorrect old password
- **GIVEN** a user is logged in
- **WHEN** the user provides an incorrect old password when trying to change their password
- **THEN** the system SHALL display an error message and the password SHALL NOT be changed.
