package org.ngafid;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

/**
 * Configuration that provides an in-memory {@link UserDetailsManager}.
 *
 * <p>Creates three default users with systematic names and passwords:
 * - system_admin / system_admin_pass  (ADMIN, READ, WRITE)
 * - system_reader / system_reader_pass (READ)
 * - system_writer / system_writer_pass (WRITE)
 *
 * <p>Any other beans of type {@link UserDetails} present in the context
 * will be injected by Spring into the {@code injectedUsers} parameter
 * and added to the resulting {@link InMemoryUserDetailsManager}.
 *
 * Note: this is an in-memory store intended for prototypes/tests only.
 */
@Configuration
public class UserDetailsManagerConfig {

    /**
     * Build an InMemoryUserDetailsManager containing the default users
     * plus any additional {@link UserDetails} beans that were defined elsewhere.
     *
     * @param encoder a PasswordEncoder bean (must exist in the context)
     * @param injectedUsers zero-or-more {@link UserDetails} beans supplied by Spring
     * @return a UserDetailsManager backed by in-memory users
     */
    @Bean
    public UserDetailsManager userDetailsManager(PasswordEncoder encoder, List<UserDetails> injectedUsers) {
        // Default, systematically-named users for prototype/testing
        UserDetails admin = User.withUsername("system_admin")
                .password(encoder.encode("system_admin_pass"))
                .roles("ADMIN", "READ", "WRITE") // ROLE_ prefix added automatically
                .build();

        UserDetails reader = User.withUsername("system_reader")
                .password(encoder.encode("system_reader_pass"))
                .roles("READ")
                .build();

        UserDetails writer = User.withUsername("system_writer")
                .password(encoder.encode("system_writer_pass"))
                .roles("WRITE")
                .build();

        // Combine defaults with any injected users
        List<UserDetails> users = new ArrayList<>();
        users.add(admin);
        users.add(reader);
        users.add(writer);

        if (injectedUsers != null && !injectedUsers.isEmpty()) {
            users.addAll(injectedUsers);
        }

        return new InMemoryUserDetailsManager(users);
    }
}