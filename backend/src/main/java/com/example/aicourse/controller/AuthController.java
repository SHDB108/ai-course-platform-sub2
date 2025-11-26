package com.example.aicourse.controller;

import com.example.aicourse.entity.User;
import com.example.aicourse.utils.JwtUtil;
import com.example.aicourse.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Get the authenticated user (our User entity implements UserDetails)
            Object principal = authentication.getPrincipal();

            Long userId;
            String username;
            String role;
            String token;

            if (principal instanceof User) {
                // Real user from database
                User user = (User) principal;
                userId = user.getId();
                username = user.getUsername();
                role = user.getRole();
                token = jwtUtil.generateToken(user);
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                // Mock user from InMemoryUserDetailsManager (for mock profile)
                org.springframework.security.core.userdetails.User springUser =
                        (org.springframework.security.core.userdetails.User) principal;
                userId = 1L; // Default mock user ID
                username = springUser.getUsername();
                // Extract role from authorities (e.g., "ROLE_STUDENT" -> "STUDENT")
                role = springUser.getAuthorities().stream()
                        .findFirst()
                        .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                        .orElse("STUDENT");
                token = jwtUtil.generateToken(springUser);
            } else {
                return Result.error("Unsupported user type");
            }

            // Build response data
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", username);
            data.put("userId", userId);
            data.put("role", role);

            return Result.ok(data);

        } catch (DisabledException e) {
            return Result.error("Account is disabled");
        } catch (BadCredentialsException e) {
            return Result.error("Invalid username or password");
        } catch (Exception e) {
            return Result.error("Login failed: " + e.getMessage());
        }
    }

    /**
     * Login request DTO
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
