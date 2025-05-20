package com.tinyinventory.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductNewDto {
    private String name;
    private Long code;
    private int quantity;
    private BigDecimal price;

}
