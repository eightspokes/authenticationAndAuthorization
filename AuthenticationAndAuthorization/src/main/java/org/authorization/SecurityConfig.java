// java
package org.ngafid;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

/**
 * Spring Security configuration for the application.
 *
 * Defines beans used by Spring Security:
 * - a password encoder (BCrypt)
 * - an in-memory user store with authorities derived from AccessType
 * - the HTTP security filter chain (CSRF disabled via non-deprecated API, basic auth, request rules)
 * - an AuthenticationManager bean obtained from the configuration
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Create and provide a PasswordEncoder bean.
     * Uses BCrypt which is a secure, adaptive hashing function suitable for
     * storing password hashes. This bean is injected where password encoding
     * or matching is required (for example when creating users below).
     *
     * @return a BCryptPasswordEncoder instance used to encode and verify passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // UserDetailsService is now provided by UserDetailsManagerConfig

    /**
     * Configure the HTTP security filter chain for the application.
     *
     * Key behaviors:
     * - Disables CSRF using the non-deprecated lambda-style API.
     * - Permits unauthenticated access to requests under /auth/**.
     * - Requires authentication for all other requests.
     * - Enables HTTP Basic authentication with the non-deprecated Customizer API.
     *
     * Adjust request matchers and authentication mechanisms as required by the assignment.
     *
     * @param http the HttpSecurity to configure
     * @return the constructed SecurityFilterChain
     * @throws Exception if an error occurs while building the filter chain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /**
     * Expose the AuthenticationManager from the AuthenticationConfiguration.
     *
     * This bean is useful when components need to perform programmatic authentication
     * (for example to authenticate credentials outside of the standard filter chain).
     *
     * @param config the AuthenticationConfiguration provided by Spring
     * @return the AuthenticationManager obtained from the configuration
     * @throws Exception if the AuthenticationManager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}