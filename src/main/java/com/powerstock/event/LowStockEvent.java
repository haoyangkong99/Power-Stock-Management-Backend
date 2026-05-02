package com.powerstock.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockEvent {
    private Long itemId;
    private String itemSku;
    private String itemName;
    private Long locationId;
    private String locationName;
    private int currentQuantity;
    private int reorderLevel;
    private Instant timestamp;
}
