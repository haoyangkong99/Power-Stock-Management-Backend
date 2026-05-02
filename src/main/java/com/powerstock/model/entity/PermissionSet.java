package com.powerstock.model.entity;

import com.powerstock.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permission_sets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PermissionSet extends BaseEntity {
    @Column(nullable = false, unique = true) private String name;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "permission_mask", nullable = false) @Builder.Default private Long permissionMask = 0L;
}
