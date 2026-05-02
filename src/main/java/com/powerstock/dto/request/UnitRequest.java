package com.powerstock.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UnitRequest {
    @NotBlank(message = "Unit name is required") private String name;
    @NotBlank(message = "Unit symbol is required") private String symbol;
    private Long baseUnitId;
    private BigDecimal conversionFactor = BigDecimal.ONE;
}
