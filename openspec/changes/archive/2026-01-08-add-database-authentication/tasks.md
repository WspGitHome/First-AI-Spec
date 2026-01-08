## 1. Backend Implementation
- [x] 1.0. Create database schema for the `users` table.
- [x] 1.1. Create `User` model in `com.example.geminispringboot.model` mapping to the `users` table.
- [x] 1.2. Create `UserDao` mapper interface in the `dao/` package.
- [x] 1.3. Create `UserDaoService` implementation in the `dao/service/` package.
- [x] 1.4. Implement `UserDetailsServiceImpl` to use the `UserDaoService` for loading user data.
- [x] 1.5. Update `SecurityConfig`:
  - [x] 1.5.1. Remove the `inMemoryUserDetailsManager`.
  - [x] 1.5.2. Add a `BCryptPasswordEncoder` bean.
  - [x] 1.5.3. Configure Spring Security to use the new `UserDetailsServiceImpl`.
- [x] 1.6. Create `UserController` with endpoints for:
  - [x] 1.6.1. `POST /register` (admin only).
  - [x] 1.6.2. `GET /user/profile`.
  - [x] 1.6.3. `POST /user/change-password`.
- [x] 1.7. Implement a database seeding mechanism (e.g., using `ApplicationRunner`) to create the initial `admin` user.

## 2. Frontend Implementation
- [x] 2.1. Create a new `register.html` page with a form for username and password.
- [x] 2.2. In the main layout/index page, add a "Register User" link/button that is only visible to users with the `ROLE_ADMIN`.
- [x_ 2.3. Create a `user-center.html` page.
- [x] 2.4. On the `user-center.html` page, display the logged-in user's information.
- [x] 2.5. On the `user-center.html` page, add a form for changing the password (fields: old password, new password, confirm new password).

## 3. Testing
- [x] 3.1. Write unit tests for the `UserDetailsServiceImpl`.
- [x] 3.2. Write integration tests for the `UserController` endpoints, ensuring correct access control is enforced.
- [ ] 3.3. Manually test the end-to-end flow: login, register (as admin), logout, login as new user, change password.
