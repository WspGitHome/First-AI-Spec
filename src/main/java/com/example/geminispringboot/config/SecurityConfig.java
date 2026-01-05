package com.example.geminispringboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Spring Security configuration for authentication.
 * Protects all endpoints except login page and static resources.
 *
 * Note: WebSecurityConfigurerAdapter is deprecated in Spring Security 5.7+
 * but still functional for Spring Boot 2.7.x compatibility.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.user.name:admin}")
    private String username;

    @Value("${spring.security.user.password:admin}")
    private String password;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                // Public endpoints: login page and static resources
                .antMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
                .and()
            .formLogin()
                // Custom login page
                .loginPage("/login")
                // Default login processing URL (Spring Security handles this)
                .loginProcessingUrl("/login")
                // Where to redirect after successful login
                .defaultSuccessUrl("/index.html", true)
                // Permit everyone to see the login page
                .permitAll()
                .and()
            .logout()
                // Logout URL
                .logoutUrl("/logout")
                // Where to redirect after logout
                .logoutSuccessUrl("/login?logout")
                // Invalidate session
                .invalidateHttpSession(true)
                .permitAll()
                .and()
            // Disable CSRF for API endpoints (needed for file upload)
            .csrf().ignoringAntMatchers("/process-roster", "/download/**");
    }

    /**
     * Configure user details service with credentials from application.yml.
     * Uses in-memory user store for simplicity (no database required).
     * Uses NoOpPasswordEncoder for plaintext password (acceptable for internal tool).
     */
    @Bean
    @Override
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
