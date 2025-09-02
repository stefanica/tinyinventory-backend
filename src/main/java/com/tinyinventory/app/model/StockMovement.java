package com.tinyinventory.app.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.*;

//@Data //Replaced by @Getter, @Setter
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_stockmovement_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore // Prevent serialization
    private User user;

    //orphanRemoval = true means: “If a StockMovementItem is removed from the items collection, delete it from the database too.”
    @OneToMany(mappedBy = "stockMovement", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude   // avoid recursion (StackOverflow error from Lombok @Data)
    private List<StockMovementItem> items = new ArrayList<>(); //This represents a product (item) with multiple batches

    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate;


}
