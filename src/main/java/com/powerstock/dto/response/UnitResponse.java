package com.powerstock.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UnitResponse {
    private Long id;
    private String name;
    private String symbol;
    private Long baseUnitId;
    private String baseUnitName;
    private BigDecimal conversionFactor;
    private Instant createdAt;
}
