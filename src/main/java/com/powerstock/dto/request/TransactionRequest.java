package com.powerstock.dto.request;
import com.powerstock.model.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionRequest {
    @NotNull(message = "Transaction type is required") private TransactionType type;
    @NotNull(message = "Item ID is required") private Long itemId;
    @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") private Integer quantity;
    @NotNull(message = "Unit ID is required") private Long unitId;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private Long locationFromId;
    @NotNull(message = "Destination location is required") private Long locationToId;
}
