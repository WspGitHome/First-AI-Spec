# Design: Database Authentication and User Management

## Context
This document outlines the technical design for migrating from static, in-memory authentication to a database-backed system. The goal is to create a secure and extensible foundation for user management.

## Decisions

### 1. Database Schema
A new `users` table will be created to store user information.

**`users` table schema:**
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | Primary Key, Auto-increment | Unique identifier for the user. |
| `username` | `VARCHAR(255)` | Not Null, Unique | The user's login name. |
| `password` | `VARCHAR(255)` | Not Null | The user's encrypted password. |
| `roles` | `VARCHAR(255)` | Not Null | Comma-separated list of user roles (e.g., "ROLE_ADMIN,ROLE_USER"). |

An initial `admin` user will be seeded into this table on application startup if one does not already exist.
when need databaseconnection info  need read `/Users/wushuping/Documents/project/google_gemini_base/gemini-springboot/openspec/project.md`  Important Constraints:Database Connection

### 2. Authentication Flow
- **`User` Model:** A model class will be created in `com.example.geminispringboot.model` to map to the `users` table, using MyBatis Plus annotations and useing lombok annotations
- **`UserDao` and `UserDaoService`:**
    - A `UserDao` mapper interface will be created in the `dao/` package (`public interface UserDao extends BaseMapper<User> {}`).
    - A `UserDaoService` implementation will be created in the `dao/service/` package (`@Service public class UserServiceDao extends ServiceImpl<UserDao, User> {}`). This service will be used for all database operations, primarily using MyBatis Plus `QueryWrapper` objects as per project constraints.
- **`UserDetailsServiceImpl`:** This class will implement Spring Security's `UserDetailsService`. It will use the `UserDaoService` to fetch a user from the database by username.
- **`SecurityConfig`:**
    - The existing `inMemoryUserDetailsManager` bean will be removed.
    - A `BCryptPasswordEncoder` bean will be created for password hashing.
    - The `HttpSecurity` configuration will be updated to use the new `UserDetailsServiceImpl` and `BCryptPasswordEncoder`.
  

### 3. User Management Endpoints
A new `UserController` will be created to handle user-related actions.

- **Admin-only Registration:**
  - `POST /register`: Accepts a new username and password. This endpoint will be secured to only allow access from users with the `ROLE_ADMIN`.
- **User Center:**
  - `GET /user/profile`: Returns the profile information for the currently logged-in user.
  - `POST /user/change-password`: Accepts the old password and a new password to allow users to update their own password.

### 4. Frontend
- **Registration:** A registration link/button will be added to `index.html` (or a shared layout) and will only be visible if the logged-in user has the `ROLE_ADMIN` authority. This will lead to a new `register.html` page.
- **User Center:** A "User Center" link will be added to the main navigation. This will lead to a `user-center.html` page that displays user info and contains the change password form.

## Risks / Trade-offs
- **Database Seeding:** A reliable mechanism must be implemented to create the initial `admin` user without causing errors on subsequent startups. A `CommandLineRunner` or `ApplicationRunner` bean is a suitable approach.
- **Security:** Endpoints must be correctly secured. The registration endpoint is critical and must be protected against access by non-admin users. Input validation must be performed for all new endpoints.
