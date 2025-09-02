package com.tinyinventory.app.repo;

import com.tinyinventory.app.model.Product;
import com.tinyinventory.app.model.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductBatchRepo extends JpaRepository<ProductBatch, Long> {

    //The methods from below do the same thing (ProductBatch is optional)
    List<ProductBatch> findProductBatchByProduct(Product product); //remove if below works
    List<ProductBatch> findByProduct(Product product);


    Optional<ProductBatch> findByIdAndProduct(Long id, Product product);


    ///METHOD 1: Find all product batches of a product, ordered by date
    //You will have to take the first one from the list; can use Streams: .stream().findFirst().orElse(null);
    @Query("""
    SELECT pb
    FROM ProductBatch pb
    WHERE pb.product = :product
    ORDER BY pb.updatedAt DESC
    """)
    List<ProductBatch> findLatestBatchByProduct(@Param("product") Product product);


    ///METHOD 2: Find all product batches of a product, ordered by date
    Optional<ProductBatch> findFirstByProductOrderByUpdatedAtDesc(Product product);


    //Find all ProductBatch items of a Product, ordered by Update time. Mainly used for Android Inventory display.
    List<ProductBatch> findByProductOrderByUpdatedAtDesc(Product product);


    //Find all ProductBatch items of a Product, ordered by Update time form Oldest to Newest
    //Used to remove a quantity/batch
    List<ProductBatch> findByProductOrderByUpdatedAtAsc(Product product);


    /// Not Used Yet!!! Designed to be used for checkout to compare checkout quantity to inventory quantity
    //SUM(pb.quantity) → sums up all the quantity values.
    //COALESCE(..., 0) → makes sure that if there are no batches, it returns 0 instead of null.
    //@Param("product") → binds the method parameter to the JPQL query.
    @Query("SELECT COALESCE(SUM(pb.quantity), 0) FROM ProductBatch pb WHERE pb.product = :product")
    Long sumQuantityByProduct(@Param("product") Product product);



}
