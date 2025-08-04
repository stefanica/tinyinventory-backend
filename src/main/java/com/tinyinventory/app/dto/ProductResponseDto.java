package com.tinyinventory.app.dto;

import com.tinyinventory.app.model.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponseDto { //GET product from database -> to web table OR android app
    private Long id;
    //private UserResponseDto userResponseDto; //Uncomment this and below if you want the used info to be sent fot each product
    // private int userId;
    private String name;
    private Long code;
    private int quantity;
    private BigDecimal priceIn;
    private BigDecimal priceOut;
    private LocalDateTime updatedAt;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        //this.userResponseDto = new UserResponseDto(product.getUser()); // Uncomment to send user data
        // this.userId = product.getUserId();
        this.name = product.getName();
        this.code = product.getCode();
        this.quantity = product.getQuantity();
        this.priceIn = product.getPriceIn();
        this.priceOut = product.getPriceOut();
        this.updatedAt = product.getUpdatedAt();
    }


}
