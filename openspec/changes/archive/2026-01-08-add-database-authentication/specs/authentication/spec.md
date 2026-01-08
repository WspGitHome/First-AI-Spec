## MODIFIED Requirements

### Requirement: User Authentication
The system SHALL authenticate users against credentials stored in the database.

#### Scenario: Successful login
- **GIVEN** a user with a valid username and password stored in the database
- **WHEN** the user provides their correct credentials on the login page
- **THEN** the user SHALL be granted access and redirected to the main application page.

#### Scenario: Unsuccessful login
- **GIVEN** a user with a valid username stored in the database
- **WHEN** the user provides an incorrect password
- **THEN** the user SHALL be shown an error message and remain on the login page.

#### Scenario: Non-existent user
- **WHEN** a user attempts to log in with a username that does not exist in the database
- **THEN** the user SHALL be shown an error message and remain on the login page.
