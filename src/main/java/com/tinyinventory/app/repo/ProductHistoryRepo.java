package com.tinyinventory.app.repo;

import com.tinyinventory.app.model.ProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductHistoryRepo extends JpaRepository<ProductHistory, Long> {


}
