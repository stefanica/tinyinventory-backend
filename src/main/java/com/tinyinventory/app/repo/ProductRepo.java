package com.tinyinventory.app.repo;

import com.tinyinventory.app.model.Product;
import com.tinyinventory.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    //***NO longer used. Was using an additional userId field from Product(s), but I removed it
    //SQL: SELECT * FROM products WHERE user_id = ?1
    //JPA SQL: @Query("SELECT p FROM products p WHERE p.user_id = :userId")
    //Optional<List<Product>> findAllByUserId(int userId);

    //SELECT * FROM products WHERE users = ?1
    //@Query("SELECT p FROM Product p WHERE p.user = :user")
    //List<Product> findAllByUser(@Param("user") User user);
    Optional<List<Product>> findAllByUser(User user);

    //SELECT * FROM products WHERE code = ?1
    //@Query("SELECT p FROM Product p WHERE p.code = :code");
    //Optional<Product> findProductByCode(@Param("code") Long code);
    Optional<Product> findProductByCode(Long code);

    //find product in Products table using Code and User
    Optional<Product> findByCodeAndUser(Long code, User user);
    //Alternative method using @Query annotation
    //@Query("SELECT p FROM Product p WHERE p.code = :code AND p.user = :user")
    //Optional<Product> findProductByCodeAndUser(@Param("code") Long code, @Param("user") User user);



}
