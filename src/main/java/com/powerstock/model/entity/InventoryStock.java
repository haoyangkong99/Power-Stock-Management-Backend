package com.powerstock.model.entity;

import com.powerstock.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "inventory_stocks", uniqueConstraints = @UniqueConstraint(columnNames = {"item_id", "location_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryStock extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_id", nullable = false) private Item item;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "location_id", nullable = false) private Location location;
    @Column(name = "current_quantity", nullable = false) @Builder.Default private Integer currentQuantity = 0;
    @Column(name = "last_updated", nullable = false) @Builder.Default private Instant lastUpdated = Instant.now();
}
