package com.tinyinventory.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//Data Entity mainly used to store product price_in and quantity
//@Data //Replaced by @Getter, @Setter
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_batches")
public class ProductBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_batch_product"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore // Prevent serialization
    private Product product;

    @Column(name="price_in", nullable = false, precision = 19, scale = 4) // Precision = total digits, Scale = decimal digits
    private BigDecimal priceIn;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
