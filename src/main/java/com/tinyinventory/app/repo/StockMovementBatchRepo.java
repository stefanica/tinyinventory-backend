package com.tinyinventory.app.repo;

import com.tinyinventory.app.model.StockMovementBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementBatchRepo extends JpaRepository<StockMovementBatch, Long> {

}
