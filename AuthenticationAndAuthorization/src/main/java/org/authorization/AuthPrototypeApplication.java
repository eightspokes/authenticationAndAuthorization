package org.ngafid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
public class AuthPrototypeApplication {

    @Autowired
    private UserDetailsManager userDetailsManager;

    @Autowired
    private PasswordEncoder passwordEncoder;
    public static void main(String[] args) {
        SpringApplication.run(AuthPrototypeApplication.class, args);
    }

    @Bean
    public CommandLineRunner testAuthentication(AuthenticationManager authenticationManager) {
        return args -> {
            System.out.println("\n=== Authentication and Authorization System Demo ===");
            System.out.println("Server running on: http://localhost:8081");
            System.out.println("\nPredefined users:");
            System.out.println("- system_admin / system_admin_pass (ADMIN, READ, WRITE)");
            System.out.println("- system_reader / system_reader_pass (READ)");
            System.out.println("- system_writer / system_writer_pass (WRITE)");
            
            System.out.println("\nAvailable endpoints:");
            System.out.println("- GET /service/admin/ping (requires ADMIN)");
            System.out.println("- GET /service/read/ping (requires READ)");
            System.out.println("- GET /service/write/ping (requires WRITE)");
            System.out.println("- POST /auth/users (create user, requires ADMIN)");
            System.out.println("- DELETE /auth/users/{username} (delete user, requires ADMIN)");
            
            System.out.println("\nTesting authentication...");
            
            // test the "system_admin" user
            try {
                var token = new UsernamePasswordAuthenticationToken("system_admin", "system_admin_pass");
                var auth = authenticationManager.authenticate(token);
                System.out.println("system_admin authenticated: " + auth.isAuthenticated() + " authorities=" + auth.getAuthorities());
            } catch (Exception e) {
                System.err.println("system_admin authentication failed: " + e.getMessage());
            }

            // test the "system_reader" user
            try {
                var token = new UsernamePasswordAuthenticationToken("system_reader", "system_reader_pass");
                var auth = authenticationManager.authenticate(token);
                System.out.println("system_reader authenticated: " + auth.isAuthenticated() + " authorities=" + auth.getAuthorities());
            } catch (Exception e) {
                System.err.println("system_reader authentication failed: " + e.getMessage());
            }

            // test the "system_writer" user
            try {
                var token = new UsernamePasswordAuthenticationToken("system_writer", "system_writer_pass");
                var auth = authenticationManager.authenticate(token);
                System.out.println("system_writer authenticated: " + auth.isAuthenticated() + " authorities=" + auth.getAuthorities());
            } catch (Exception e) {
                System.err.println("system_writer authentication failed: " + e.getMessage());
            }
            
            System.out.println("\n=== Demo Complete ===");
            System.out.println("Use curl or Postman to test the endpoints with different user credentials.");
            System.out.println("\nAvailable Test Script:");
            System.out.println("  ./test_auth_system.sh - Complete test suite with delays");
            System.out.println("\nThe application will show new users as they are created dynamically!");
        };
    }

    // ===== ADMIN ENDPOINTS =====
    @GetMapping("/service/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminPing() {
        System.out.println("ADMIN ACCESS: User accessed admin endpoint");
        return Map.of(
            "message", "Admin access granted",
            "timestamp", LocalDateTime.now(),
            "access_level", "ADMIN",
            "description", "This endpoint requires ADMIN role"
        );
    }

    @GetMapping("/service/admin/system-info")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> systemInfo() {
        return Map.of(
            "system_status", "operational",
            "uptime", "24h 15m 30s",
            "memory_usage", "75%",
            "cpu_usage", "45%",
            "timestamp", LocalDateTime.now()
        );
    }

    // ===== READ ENDPOINTS =====
    @GetMapping("/service/read/ping")
    @PreAuthorize("hasRole('READ')")
    public Map<String, Object> readPing() {
        System.out.println("READ ACCESS: User accessed read endpoint");
        return Map.of(
            "message", "Read access granted",
            "timestamp", LocalDateTime.now(),
            "access_level", "READ",
            "description", "This endpoint requires READ role or higher"
        );
    }

    @GetMapping("/service/read/public-data")
    @PreAuthorize("hasRole('READ')")
    public Map<String, Object> getPublicData() {
        return Map.of(
            "data", "This is public data accessible to all authenticated users",
            "sensitivity", "public",
            "timestamp", LocalDateTime.now()
        );
    }

    // ===== WRITE ENDPOINTS =====
    @GetMapping("/service/write/ping")
    @PreAuthorize("hasRole('WRITE')")
    public Map<String, Object> writePing() {
        System.out.println("WRITE ACCESS: User accessed write endpoint");
        return Map.of(
            "message", "Write access granted",
            "timestamp", LocalDateTime.now(),
            "access_level", "WRITE",
            "description", "This endpoint requires WRITE role or higher"
        );
    }

    @PostMapping("/service/write/create")
    @PreAuthorize("hasRole('WRITE')")
    public Map<String, Object> createResource(@RequestBody Map<String, Object> data) {
        return Map.of(
            "message", "Resource created successfully",
            "created_data", data,
            "resource_id", "res_" + System.currentTimeMillis(),
            "timestamp", LocalDateTime.now()
        );
    }

    // ===== USER MANAGEMENT ENDPOINTS =====
    @PostMapping("/auth/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody CreateUserRequest request) {
        try {
            // Validate request
            if (request.username == null || request.username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Username is required",
                    "timestamp", LocalDateTime.now()
                ));
            }

            if (request.password == null || request.password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Password is required",
                    "timestamp", LocalDateTime.now()
                ));
            }

            if (request.roles == null || request.roles.length == 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "At least one role is required",
                    "timestamp", LocalDateTime.now()
                ));
            }

            // Check if user already exists
            if (userDetailsManager.userExists(request.username)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "User already exists",
                    "username", request.username,
                    "timestamp", LocalDateTime.now()
                ));
            }

            // Validate roles
            Set<String> validRoles = Set.of("ADMIN", "WRITE", "READ");
            Set<String> invalidRoles = Arrays.stream(request.roles)
                .filter(role -> !validRoles.contains(role))
                .collect(Collectors.toSet());

            if (!invalidRoles.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid roles: " + invalidRoles,
                    "valid_roles", validRoles,
                    "timestamp", LocalDateTime.now()
                ));
            }

            // Create user with authorities
            Set<String> roleSet = Arrays.stream(request.roles).collect(Collectors.toSet());
            UserDetails newUser = User.builder()
                .username(request.username)
                .password(passwordEncoder.encode(request.password))
                .roles(roleSet.toArray(new String[0]))
                .build();

            userDetailsManager.createUser(newUser);

            // Log the user creation to console
            System.out.println("\nNEW USER CREATED:");
            System.out.println("   Username: " + request.username);
            System.out.println("   Roles: " + String.join(", ", request.roles));
            System.out.println("   Time: " + LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "User created successfully",
                "username", request.username,
                "roles", request.roles,
                "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to create user: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    @DeleteMapping("/auth/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String username) {
        try {
            if (!userDetailsManager.userExists(username)) {
                return ResponseEntity.notFound().build();
            }

            userDetailsManager.deleteUser(username);

            // Log the user deletion to console
            System.out.println("\nUSER DELETED:");
            System.out.println("   Username: " + username);
            System.out.println("   Time: " + LocalDateTime.now());

            return ResponseEntity.ok(Map.of(
                "message", "User deleted successfully",
                "username", username,
                "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to delete user: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    @PutMapping("/auth/users/{username}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUserRoles(
            @PathVariable String username,
            @RequestBody UpdateRolesRequest request) {
        try {
            if (!userDetailsManager.userExists(username)) {
                return ResponseEntity.notFound().build();
            }

            // Validate roles
            Set<String> validRoles = Set.of("ADMIN", "WRITE", "READ");
            Set<String> invalidRoles = Arrays.stream(request.roles)
                .filter(role -> !validRoles.contains(role))
                .collect(Collectors.toSet());

            if (!invalidRoles.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid roles: " + invalidRoles,
                    "valid_roles", validRoles,
                    "timestamp", LocalDateTime.now()
                ));
            }

            // Get current user details
            UserDetails currentUser = userDetailsManager.loadUserByUsername(username);
            
            // Create updated user with new roles
            UserDetails updatedUser = User.builder()
                .username(username)
                .password(currentUser.getPassword()) // Keep existing password
                .roles(request.roles)
                .build();

            userDetailsManager.updateUser(updatedUser);

            // Log the role update to console
            System.out.println("\nUSER ROLES UPDATED:");
            System.out.println("   Username: " + username);
            System.out.println("   New Roles: " + String.join(", ", request.roles));
            System.out.println("   Time: " + LocalDateTime.now());

            return ResponseEntity.ok(Map.of(
                "message", "User roles updated successfully",
                "username", username,
                "new_roles", request.roles,
                "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to update user roles: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    // ===== REQUEST DTOs =====
    public static class CreateUserRequest {
        public String username;
        public String password;
        public String[] roles;
    }

    public static class UpdateRolesRequest {
        public String[] roles;
    }
}