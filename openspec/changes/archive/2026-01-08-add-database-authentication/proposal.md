# Change: Database-Backed Authentication and User Management

## Why
The current authentication system uses static, hardcoded credentials from a configuration file. This is insecure, inflexible, and prevents new users from being added without manual configuration and a server restart. This change moves authentication to a database-backed system to provide secure, dynamic user management.

## What Changes
- **Database Authentication:** Replace the in-memory user details service with a database-backed implementation.
- **Password Encryption:** Introduce `BCryptPasswordEncoder` to securely store user passwords.
- **User Registration:** Add a feature allowing the `admin` user to register new users through the web interface. This functionality will be hidden from non-admin users.
- **User Center:** Create a "User Center" page accessible after login.
- **User Profile:** The User Center will display the current user's username and account information.
- **Change Password:** The User Center will provide a form for users to change their own password.

## Impact
- **Affected Specs:**
  - `authentication`: Will be modified to reflect database-backed validation.
  - `user-management`: A new spec will be created to define registration, profile viewing, and password management.
- **Affected Code:**
  - `SecurityConfig`: Will be updated to use a new `UserDetailsService` and `PasswordEncoder`.
  - New `User` entity, repository, and service layers will be created.
  - New `UserController` (or similar) will be created to handle registration and user profile actions.
  - New HTML templates and static resources will be created for the registration and user center pages.
- **Breaking Changes:**
  - The static user credentials in `application.yml` will no longer be used for authentication. An initial `admin` user will need to be seeded in the database.
