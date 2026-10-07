package com.splitbill.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_users", indexes = {@Index(name = "idx_auth_users_user_id", columnList = "user_id"), @Index(name = "idx_auth_users_role", columnList = "role"), @Index(name = "idx_auth_users_provider", columnList = "provider"), @Index(name = "idx_auth_users_provider_subject", columnList = "provider_subject")}, uniqueConstraints = {@UniqueConstraint(name = "uk_auth_users_user_id", columnNames = "user_id"), @UniqueConstraint(name = "uk_auth_users_provider_subject", columnNames = {"provider", "provider_subject"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Provider provider = Provider.LOCAL;

    @Column(name = "provider_subject", length = 255)
    private String providerSubject;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Role {
        USER, ADMIN
    }

    public enum Provider {
        LOCAL, GOOGLE
    }
}