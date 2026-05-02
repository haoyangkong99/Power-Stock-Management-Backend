package com.powerstock.model.entity;

import com.powerstock.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseEntity {
    @Column(nullable = false, unique = true) private String username;
    @Column(nullable = false, unique = true) private String email;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "location_id") private Location location;
    @Column(name = "permission_mask", nullable = false) @Builder.Default private Long permissionMask = 0L;
}
