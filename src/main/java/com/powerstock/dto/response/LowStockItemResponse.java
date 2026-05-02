package com.powerstock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockItemResponse {
    private Long itemId;
    private String itemSku;
    private String itemName;
    private Long locationId;
    private String locationName;
    private int currentQuantity;
    private int reorderLevel;
    private BigDecimal reorderQuantity;
}
