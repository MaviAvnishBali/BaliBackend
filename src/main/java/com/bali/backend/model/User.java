package com.bali.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User entity supporting both traditional and Firebase phone authentication.
 * For phone-auth users, username/email/password may be null.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_firebase_uid", columnList = "firebaseUid", unique = true),
        @Index(name = "idx_phone_number", columnList = "phoneNumber")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Firebase UID from phone authentication */
    @Column(unique = true)
    private String firebaseUid;

    /** Phone number with country code, e.g. +91XXXXXXXXXX */
    @Column(unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @ManyToOne
    @JoinColumn(name = "village_group_id", nullable = true)
    private VillageGroup villageGroup;

    private String address;

    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;

    private String profileImageUrl;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
