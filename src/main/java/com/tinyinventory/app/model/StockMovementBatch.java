package com.tinyinventory.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

//@Data //Replaced by @Getter, @Setter
@Getter
@Setter
@Entity
@Table(name = "stock_movement_batches")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementBatch {//Represents the movement of a ProductBatch

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stock_movement_item_id", nullable = false)
    @ToString.Exclude   // avoid recursion (StackOverflow error from Lombok @Data)
    private StockMovementItem stockMovementItem;

    @ManyToOne(optional = true) // can even be nullable if batch is later deleted
    @JoinColumn(name = "product_batch_id")
    @ToString.Exclude   // avoid recursion (StackOverflow error from Lombok @Data)
    private ProductBatch productBatch;

    @Column(name="price_in", nullable = false, precision = 19, scale = 4)
    private BigDecimal priceIn; // snapshot at checkout

    @Column(nullable = false)
    private int quantity;

}
