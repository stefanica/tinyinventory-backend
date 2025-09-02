package com.tinyinventory.app.dto;

import com.tinyinventory.app.model.Product;
import com.tinyinventory.app.model.ProductBatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto { //GET product from database -> to web table OR android app
    private Long productId;
    private Long productBatchId;
    //private UserResponseDto userResponseDto; //Uncomment this and below if you want the used info to be sent fot each product
    // private int userId;
    private String name;
    private Long code;
    private int quantityThreshold;
    private int quantity;
    private BigDecimal priceIn;
    private BigDecimal priceOut;
    private LocalDateTime updatedAt;

    public ProductResponseDto(Product product, ProductBatch productBatch) {
        this.productId = product.getId();
        this.productBatchId = productBatch.getId();
        //this.userResponseDto = new UserResponseDto(product.getUser()); // Uncomment to send user data
        // this.userId = product.getUserId();
        this.name = product.getName();
        this.code = product.getCode();
        this.quantityThreshold = product.getQuantityThreshold();
        this.quantity = productBatch.getQuantity();
        this.priceIn = productBatch.getPriceIn();
        this.priceOut = product.getPriceOut();
        this.updatedAt = productBatch.getUpdatedAt();
    }


}
