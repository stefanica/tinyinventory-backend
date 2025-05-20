package com.tinyinventory.app.dto;

import com.tinyinventory.app.model.Product;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponseDto {
    private Long id;
    private UserResponseDto userResponseDto;
   // private int userId;
    private String name;
    private Long code;
    private int quantity;
    private BigDecimal price;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.userResponseDto = new UserResponseDto(product.getUser());
       // this.userId = product.getUserId();
        this.name = product.getName();
        this.code = product.getCode();
        this.quantity = product.getQuantity();
        this.price = product.getPrice();
    }


}
