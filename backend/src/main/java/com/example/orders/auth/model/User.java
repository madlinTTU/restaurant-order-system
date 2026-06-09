package com.example.orders.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "modified_at", nullable = false)
    private OffsetDateTime modifiedAt;

    @Column(name = "modified_by", nullable = false)
    private UUID modifiedBy;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (role == null) role = Role.CUSTOMER;
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (createdBy == null) createdBy = id;
        if (modifiedAt == null) modifiedAt = OffsetDateTime.now();
        if (modifiedBy == null) modifiedBy = id;
        if (version == null) version = 0L;
    }
}
