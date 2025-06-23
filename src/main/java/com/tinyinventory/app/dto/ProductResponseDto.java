package com.tinyinventory.app.dto;

import com.tinyinventory.app.model.Product;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponseDto {
    private Long id;
    //private UserResponseDto userResponseDto; //Uncomment this and below if you want the used info to be sent fot each product
    // private int userId;
    private String name;
    private Long code;
    private int quantity;
    private BigDecimal price;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        //this.userResponseDto = new UserResponseDto(product.getUser()); // Uncomment to send user data
        // this.userId = product.getUserId();
        this.name = product.getName();
        this.code = product.getCode();
        this.quantity = product.getQuantity();
        this.price = product.getPrice();
    }


}
