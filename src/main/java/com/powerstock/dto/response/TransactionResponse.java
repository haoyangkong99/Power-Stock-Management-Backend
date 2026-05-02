package com.powerstock.dto.response;
import com.powerstock.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private Long itemId;
    private String itemSku;
    private String itemName;
    private Integer quantity;
    private String unitSymbol;
    private BigDecimal unitPrice;
    private BigDecimal totalValue;
    private Long userId;
    private String username;
    private Long locationFromId;
    private String locationFromName;
    private Long locationToId;
    private String locationToName;
    private Boolean synced;
    private Instant createdAt;
}
