package com.powerstock.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemRequest {
    @NotBlank(message = "SKU is required")
    @Size(max = 255)
    private String sku;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    private String description;

    private String category;

    @NotNull(message = "Base price is required")
    private BigDecimal basePrice;

    private Integer reorderLevel = 0;

    private Integer reorderQuantity = 0;
}
