package com.tinyinventory.app.repo;

import com.tinyinventory.app.model.StockMovement;
import com.tinyinventory.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepo extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findAllByUser(User user);

    List<StockMovement> findAllByUserOrderByIdDesc(User user);

}
