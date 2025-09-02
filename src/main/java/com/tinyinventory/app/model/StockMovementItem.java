package com.tinyinventory.app.model;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;


//@Data //Replaced by @Getter, @Setter
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_movement_items")
public class StockMovementItem { //Represents a the Movement of a Product

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stock_movement_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_stockmovement"))
    @ToString.Exclude   // avoid recursion (StackOverflow error from Lombok @Data)
    private StockMovement stockMovement;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_product"))
    @ToString.Exclude   // avoid recursion (StackOverflow error from Lombok @Data)
    private Product product;

    //orphanRemoval = true means: “If a StockMovementBatch is removed from the batches collection, delete it from the database too.”
    @OneToMany(mappedBy = "stockMovementItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude   // avoid recursion (StackOverflow error from Lombok @Data)
    private List<StockMovementBatch> batches = new ArrayList<>();

    //Total quantity from all batches
    @Column(nullable = false)
    private int quantity;

}
