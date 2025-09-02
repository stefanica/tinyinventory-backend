package com.tinyinventory.app.dto;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCheckoutDto {
    private Long code;
    private int quantity;
}
