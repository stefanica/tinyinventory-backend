package com.tinyinventory.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockMovementResponseDto {
    private LocalDateTime movementDate;
    private List<StockMovementBatchDto> stockMovementBatchDtoList;
    private BigDecimal totalProfit;

}
