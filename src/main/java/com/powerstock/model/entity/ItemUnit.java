package com.powerstock.model.entity;

import com.powerstock.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_units", uniqueConstraints = @UniqueConstraint(columnNames = {"item_id", "unit_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemUnit extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_id", nullable = false) private Item item;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "unit_id", nullable = false) private Unit unit;
    @Column(name = "is_primary", nullable = false) @Builder.Default private Boolean primary = false;
    @Column(name = "is_active", nullable = false) @Builder.Default private Boolean active = true;
}
