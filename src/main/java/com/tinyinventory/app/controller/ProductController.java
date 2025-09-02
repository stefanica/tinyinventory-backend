package com.tinyinventory.app.controller;

import com.tinyinventory.app.dto.*;
import com.tinyinventory.app.exceptions.ProductNotFoundException;
import com.tinyinventory.app.exceptions.UnauthorizedException;
import com.tinyinventory.app.model.Product;
import com.tinyinventory.app.model.ProductBatch;
import com.tinyinventory.app.service.JwtService;
import com.tinyinventory.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Used to allow access from React/Vite. It may be changed or commented in production
//@CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(origins = {
        "http://localhost:5173", // for development
        "https://tinyinventory.com",
        "https://www.tinyinventory.com"
})
@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private JwtService jwtService;

    /****** Previous when the username was in the URL ********/
    /*@GetMapping("/get-products/{username}")
    public ResponseEntity<List<ProductResponseDto>> getProducts(@PathVariable String username) {*/

    /******* NEW When I extract the username from the JWT token *******/
    //Returns a list WHERE there is a NEW Product for EVERY ProductBatch
    @GetMapping("/get-products")
    //public ResponseEntity<List<ProductResponseDto>> getProducts(@RequestBody TokenDto tokenDto) {
    public ResponseEntity<List<ProductResponseDto>> getAllProducts(@AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

       List<ProductResponseDto> productList = productService.getAllUserProducts(userDetails.getUsername());
       if (productList.isEmpty()) {
           throw new ProductNotFoundException("No product found in database");
       }

       return ResponseEntity.ok(productList);
    }



    //Will return Product (by code and user) and one ProductBatch (by product and id)
    //USE: designed to be used in React to EDIT a specific product. ***This to be used only to correct mistakes
    @GetMapping("/get-product/{productCode}/{productBatchId}")
    public ResponseEntity<ProductResponseDto> getOneProductAndOneProductBatch(@AuthenticationPrincipal UserDetails userDetails,
                                                                  @PathVariable Long productCode, @PathVariable Long productBatchId) {
        if (userDetails == null) {
            throw new UnauthorizedException("User is not authenticated");
        }


        System.out.println("Get product batch: " + userDetails.getUsername() + " " + productCode + " " + productBatchId);

        ProductResponseDto productResponseDto = productService.getOneProductAndOneProductBatch(
                userDetails.getUsername(), productCode, productBatchId);
        return ResponseEntity.status(HttpStatus.OK).body(productResponseDto);
    }



    //Designed for Android Inventory Activity: Return each Product with summed and averaged price_in of all Batches
    @GetMapping("/get-products-summed")
    public ResponseEntity<List<ProductResponseDto>> getAllProductsAndBatchesSum(@AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        List<ProductResponseDto> productResponseDtoList = productService
                .getAllProductsWithSumBatches(userDetails.getUsername());

        return ResponseEntity.ok(productResponseDtoList);
    }



    //Used in Android to check/search a Product by code, when Add or Remove
    //Get Product info and the last Batch corresponding to that product
    @GetMapping("/get-product/{productCode}")
    public ResponseEntity<ProductResponseDto> getLastProductInfoBatch(@AuthenticationPrincipal UserDetails userDetails,
                                                                      @PathVariable Long productCode) {
        if (userDetails == null) {
            throw new UnauthorizedException("User is not authenticated");
        }
        ProductResponseDto productResponseDto = productService.getLastProductInfoBatch(userDetails.getUsername(), productCode);

        return ResponseEntity.ok(productResponseDto);
    }


    //For new Product creates a new product
    //For existing Product updates existing product AND checks ProductBatch to see if there is one with the same
    //price_in as the updated product, IF Yes it updates that ProductBatch, IF Not creates a new ProductBatch
    @PostMapping("/save-product")
    public ResponseEntity<Void> saveProduct(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody ProductNewDto productNewDto) {
        //Why use Void: In REST APIs, we typically don't return primitive values (like Boolean)
        if (userDetails == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        productService.saveProduct(productNewDto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created on success
    }


    //Updates a Product and one ProductBatch
    @PutMapping("/update-product")
    public ResponseEntity<Void> updateProduct(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestBody ProductUpdateDto productUpdateDto) {
        if (userDetails == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        productService.updateProduct(productUpdateDto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).build(); // 200 Created on success
    }



    //Take products from database:
    //Update ProductBatch with new remaining quantity
    //Create new StockMovement (multiple Items), StockMovementItem (multiple Batches), StockMovementBatch
    @PostMapping("/checkout-products")
    public ResponseEntity<Void> checkoutProducts(@AuthenticationPrincipal UserDetails userDetails,
                                                 @RequestBody List<ProductCheckoutDto> productCheckoutDtos) {

        if (userDetails == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        productService.checkoutProducts(userDetails.getUsername(), productCheckoutDtos);
        return ResponseEntity.status(HttpStatus.OK).build(); // 200 Created on success
    }

}
