package com.example.geminispringboot.config;

import com.example.geminispringboot.config.CustomAuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for authentication.
 * Protects all endpoints except login page and static resources.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationFailureHandler authenticationFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(authorizeRequests ->
                authorizeRequests
                    // Public endpoints: login page and static resources
                    .antMatchers("/login", "/css/**", "/js/**", "/images/**", "/api/user/register").permitAll()
                    // User management endpoints
                    // .antMatchers("/api/user/register").hasRole("ADMIN")
                    .antMatchers("/api/user/**").authenticated()
                    // All other endpoints require authentication
                    .anyRequest().authenticated()
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login")
                    .failureHandler(authenticationFailureHandler)
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
            // Disable CSRF for API endpoints
            .csrf(csrf -> csrf.ignoringAntMatchers("/process-roster", "/download/**", "/api/**"));
        return http.build();
    }

    /**
     * Use BCrypt for password encoding.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
