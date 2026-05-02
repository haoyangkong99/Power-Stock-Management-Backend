package com.powerstock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryValueResponse {
    private long totalItems;
    private int totalQuantity;
    private BigDecimal totalValue;
    private List<ItemValueBreakdown> breakdowns;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemValueBreakdown {
        private Long itemId;
        private String itemSku;
        private String itemName;
        private int totalQuantity;
        private BigDecimal unitPrice;
        private BigDecimal totalItemValue;
    }
}
