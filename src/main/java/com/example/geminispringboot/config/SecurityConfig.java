package com.example.geminispringboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for authentication.
 * Protects all endpoints except login page and static resources.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.user.name:admin}")
    private String username;

    @Value("${spring.security.user.password:admin}")
    private String password;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(authorizeRequests ->
                authorizeRequests
                    // Public endpoints: login page and static resources
                    .antMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                    // All other endpoints require authentication
                    .anyRequest().authenticated()
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login")
                    .failureUrl("/login?error=true")
                    // Redirect to the root URL, which is mapped to the index page
                    .defaultSuccessUrl("/", true)
                    .permitAll()
            )
            .logout(logout ->
                logout
                    // Require POST for logout to ensure CSRF protection
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .permitAll()
            )
            // Disable CSRF for API endpoints (needed for file upload)
            .csrf(csrf -> csrf.ignoringAntMatchers("/process-roster", "/download/**"));
        return http.build();
    }

    /**
     * Configure user details service with credentials from application.yml.
     * Uses in-memory user store for simplicity (no database required).
     * Uses NoOpPasswordEncoder for plaintext password (acceptable for internal tool).
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username(username)
                .password(password)
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Password encoder that doesn't encrypt passwords (plaintext).
     * Suitable for internal tools behind firewall.
     * For production, consider using BCrypt or similar.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
