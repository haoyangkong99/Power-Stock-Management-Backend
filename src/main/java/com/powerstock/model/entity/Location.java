package com.powerstock.model.entity;

import com.powerstock.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Location extends BaseEntity {
    @Column(nullable = false) private String name;
    @Column(columnDefinition = "TEXT") private String address;
    @Column(name = "is_active", nullable = false) @Builder.Default private Boolean active = true;
}
