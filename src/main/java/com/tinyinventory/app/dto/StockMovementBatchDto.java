package com.tinyinventory.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockMovementBatchDto {
    private String productName;
    private Long code;
    private int quantity;
    private BigDecimal priceIn;
    private BigDecimal priceOut;
    private BigDecimal profit;
}
