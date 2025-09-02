package com.tinyinventory.app.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;


//@Data //Replaced by @Getter, @Setter
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_history")
public class ProductHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Optional: keep reference to current product
    @JoinColumn(name = "product_id", nullable = false)
    //@JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product")) //Maybe used as foreign key
    private Product product;

    @ManyToOne // Who owns it
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Snapshotted fields
    private String name;
    private Long code;

    @Column(name="price_in", precision = 19, scale = 4, nullable = false)
    private BigDecimal priceIn;

    @Column(name="price_out", precision = 19, scale = 4, nullable = false)
    private BigDecimal priceOut;

    private int quantity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
