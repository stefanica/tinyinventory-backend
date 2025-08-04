package com.tinyinventory.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductNewDto { //ADD a new product to database
    private String name;
    private Long code;
    private int quantity;
    private BigDecimal priceIn;
    private BigDecimal priceOut;

}
