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
@Table(name = "products") // Specifies the table name in PostgreSQL
public class Product {

    @Id //Primary-Key
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Unique, Auto-Increment
    @Column(nullable = false, unique = true)
    private Long id;

    /*@ManyToOne
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_user")) // Creates FK constraint
    @OnDelete(action = OnDeleteAction.CASCADE) // Enables ON DELETE CASCADE
    @JsonIgnore // Prevent serialization
    private User user; // Foreign Key to User table*/

    @ManyToOne
    @JoinColumn(name = "users", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_user", value = ConstraintMode.CONSTRAINT)) // Creates FK constraint
    @OnDelete(action = OnDeleteAction.CASCADE) // Enables ON DELETE CASCADE
    @JsonIgnore // Prevent serialization
    private User user; // Foreign Key to User table

    /*
    --> The @JoinColumn(name = "user_id") explicitly tells JPA that the foreign key column
        in the database should be named user_id.
    --> referencedColumnName = "id" → This tells JPA that user_id refers to the id column in the User table.
    --> foreignKey = @ForeignKey(name = "fk_product_user", value = ConstraintMode.CONSTRAINT) → Ensures that
        a foreign key constraint is created at the database level.
     Similar to: FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE

     */

    /*
    @Column(name = "user_id", nullable = false) // user_id maps to the database column name
    private int userId;  // Store only user_id. Used when searching for a product.
    */

    @Column(nullable = false)
    private String name;

    @Column(nullable = false) // Ensures uniqueness for code
    private Long code;

    // Precision = total digits, Scale = decimal digits
    @Column(name = "price_out", precision = 19, scale = 4) // Run first, to create field with NULL, SQL from migration package will change it
    //@Column(name = "price_out", nullable = false, precision = 19, scale = 4) //Finally comment the first one and uncomment this; all new will be NOT NULL
    private BigDecimal priceOut;

    //Used as a threshold to alert the user when the available quantity gets low
    @Column(name="quantity_threshold", nullable = false, columnDefinition = "integer default 0")
    private int quantityThreshold;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    //Idea to modify date. But works only when you create a new product
    // Optional: Automatically set createdAt on persist
    /*@PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }*/

}
