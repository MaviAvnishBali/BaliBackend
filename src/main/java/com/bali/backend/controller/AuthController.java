package com.bali.backend.controller;

import com.bali.backend.model.AuthResponse;
import com.bali.backend.model.Role;
import com.bali.backend.model.User;
import com.bali.backend.repository.UserRepository;
import com.bali.backend.service.FirebaseAuthService;
import com.bali.backend.service.JwtService;
import com.bali.backend.service.PasswordService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * GraphQL resolver for phone-based authentication.
 *
 * Flow:
 * 1. Mobile client authenticates with Firebase Phone Auth
 * 2. Client sends the Firebase ID token to this mutation
 * 3. Backend verifies the token with Firebase Admin SDK
 * 4. Backend creates/updates user in PostgreSQL
 * 5. Backend returns its own JWT + user profile
 */
@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    /**
     * Authenticate a user via Firebase phone verification.
     *
     * @param firebaseToken the Firebase ID token from the mobile client
     * @return AuthResponse with backend JWT and user profile
     */
    @MutationMapping
    public AuthResponse loginWithPhone(@Argument String firebaseToken) {
        try {
            // 1. Verify the Firebase ID token
            FirebaseToken decodedToken = firebaseAuthService.verifyIdToken(firebaseToken);
            String firebaseUid = decodedToken.getUid();
            String phoneNumber = firebaseAuthService.getPhoneNumber(decodedToken);

            if (phoneNumber == null || phoneNumber.isEmpty()) {
                throw new RuntimeException("Phone number not found in Firebase token");
            }

            // 2. Find or create user in database
            // Prefer firebaseUid, fallback to phoneNumber (to avoid unique constraint collisions)
            User user = userRepository.findByFirebaseUid(firebaseUid)
                    .orElseGet(() -> userRepository.findByPhoneNumber(phoneNumber).orElse(null));

            if (user == null) {
                User newUser = User.builder()
                        .firebaseUid(firebaseUid)
                        .phoneNumber(phoneNumber)
                        .role(Role.USER)
                        .createdAt(LocalDateTime.now())
                        .lastLogin(LocalDateTime.now())
                        .build();
                logger.info("Creating new user for phone: {}", phoneNumber);
                user = userRepository.save(newUser);
            } else {
                // Ensure we keep the latest UID + phone in DB
                user.setFirebaseUid(firebaseUid);
                user.setPhoneNumber(phoneNumber);
                user.setLastLogin(LocalDateTime.now());
                user = userRepository.save(user);
            }

            boolean isProfileComplete = isUserComplete(user);

            // 3. Generate backend JWT
            String jwt = jwtService.generateToken(
                    user.getId(),
                    user.getPhoneNumber(),
                    user.getRole().name());

            logger.info("User {} logged in successfully via phone", user.getId());
            return AuthResponse.builder()
                    .token(jwt)
                    .user(user)
                    .isProfileComplete(isProfileComplete)
                    .build();

        } catch (FirebaseAuthException e) {
            logger.error("Firebase token verification failed: {}", e.getMessage());
            throw new RuntimeException("Invalid Firebase token: " + e.getMessage());
        }
    }

    /**
     * Unified save/update profile mutation.
     * villageId is optional; other fields are optional.
     * Requires Authorization: Bearer <jwt>
     */
    @MutationMapping
    public User saveProfile(
            @Argument String username,
            @Argument String email,
            @Argument String address,
            @Argument Long villageId,
            @Argument String profileImageUrl
    ) {
        User user = getAuthenticatedUser();
        boolean updated = false;

        // Update username if provided
        if (username != null && !username.isEmpty()) {
            // Check if username is already taken by another user
            if (user.getUsername() == null || !user.getUsername().equals(username)) {
                if (userRepository.findByUsername(username).isPresent()) {
                    throw new RuntimeException("Username already taken");
                }
                user.setUsername(username);
                updated = true;
            }
        }

        // Update email if provided
        if (email != null && !email.isEmpty()) {
            // Check if email is already taken by another user
            if (user.getEmail() == null || !email.equals(user.getEmail())) {
                if (userRepository.findByEmail(email).isPresent()) {
                    throw new RuntimeException("Email already taken");
                }
                user.setEmail(email);
                updated = true;
            }
        }

        // Update address if provided
        if (address != null && !address.isEmpty()) {
            user.setAddress(address);
            updated = true;
        }

        // Update profile image URL if provided
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            user.setProfileImageUrl(profileImageUrl);
            updated = true;
        }

        if (updated) {
            logger.info("Saving/updating profile for user: {}", user.getId());
            return userRepository.save(user);
        }

        // No updates provided
        return user;
    }

    /**
     * Backward-compatible alias for clients still calling updateProfile.
     */
    @MutationMapping(name = "updateProfile")
    public User updateProfile(
            @Argument String username,
            @Argument String email,
            @Argument String address,
            @Argument Long villageId,
            @Argument String profileImageUrl
    ) {
        return saveProfile(username, email, address, villageId, profileImageUrl);
    }

    @SchemaMapping(typeName = "User", field = "isProfileComplete")
    public boolean isProfileComplete(User user) {
        return isUserComplete(user);
    }

    private boolean isUserComplete(User user) {
        return user.getUsername() != null && !user.getUsername().isEmpty() &&
                user.getAddress() != null && !user.getAddress().isEmpty();
    }

    /**
     * Login with username and password.
     *
     * @param username the username
     * @param password the password
     * @return AuthResponse with JWT token and user profile
     */
    @MutationMapping
    public AuthResponse login(@Argument String username, @Argument String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (user.getPassword() == null || !passwordService.verifyPassword(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Update last login timestamp
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token
        String jwt = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        boolean isProfileComplete = isUserComplete(user);

        logger.info("User {} logged in successfully via username/password", user.getId());
        return AuthResponse.builder()
                .token(jwt)
                .user(user)
                .isProfileComplete(isProfileComplete)
                .build();
    }

    /**
     * Register a new user with username, email, and password.
     *
     * @param username the username
     * @param email    the email
     * @param password the password
     * @return the created User
     */
    @MutationMapping
    public User register(@Argument String username, @Argument String email, @Argument String password) {
        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (email != null && !email.isEmpty() && userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Hash password
        String hashedPassword = passwordService.hashPassword(password);

        // Create new user
        User newUser = User.builder()
                .username(username)
                .email(email)
                .password(hashedPassword)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        logger.info("Registering new user: {}", username);
        return userRepository.save(newUser);
    }

    /**
     * Get the currently authenticated user.
     *
     * @return the authenticated User or null
     */
    @QueryMapping
    public User me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        try {
            Long userId = (Long) authentication.getPrincipal();
            return userRepository.findById(userId)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting authenticated user: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get a user by ID.
     *
     * @param id the user ID
     * @return the User or null if not found
     */
    @QueryMapping
    public User user(@Argument Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Helper method to get the authenticated user from security context.
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("User not authenticated");
        }

        try {
            Long userId = (Long) authentication.getPrincipal();
            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } catch (Exception e) {
            logger.error("Error getting authenticated user: {}", e.getMessage());
            throw new RuntimeException("User not authenticated");
        }
    }
}
