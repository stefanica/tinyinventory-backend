package com.tinyinventory.app.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 4) // Precision = total digits, Scale = decimal digits
    private BigDecimal price;

}
