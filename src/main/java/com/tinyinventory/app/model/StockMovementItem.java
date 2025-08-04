package com.tinyinventory.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_movement_items")
public class StockMovementItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stock_movement_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_stockmovement"))
    private StockMovement stockMovement;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_product"))
    private Product product;

    @Column(nullable = false)
    private int quantity;

}
