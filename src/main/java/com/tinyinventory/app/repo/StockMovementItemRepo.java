package com.tinyinventory.app.repo;

import com.tinyinventory.app.model.StockMovementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementItemRepo extends JpaRepository<StockMovementItem, Long> {

}
