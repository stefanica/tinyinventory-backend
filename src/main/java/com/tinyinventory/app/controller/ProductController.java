package com.tinyinventory.app.controller;

import com.tinyinventory.app.dto.ProductNewDto;
import com.tinyinventory.app.dto.ProductResponseDto;
import com.tinyinventory.app.dto.ProductUpdateDto;
import com.tinyinventory.app.model.Product;
import com.tinyinventory.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/get-products/{username}")
    public ResponseEntity<List<ProductResponseDto>> getProducts(@PathVariable String username) {
       try {
           List<ProductResponseDto> productList = productService.getAllUserProducts(username);

           if (productList.isEmpty()) {
               return ResponseEntity
                       .status(HttpStatus.NO_CONTENT)
                       .build();
               //could also be
               //return ResponseEntity.noContent().build();
           }
           return ResponseEntity.ok(productList);

       } catch (Exception e) {
           return ResponseEntity
                   .status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .build();
       }
    }

    @GetMapping("get-product/{username}/{productCode}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable String username, @PathVariable Long productCode) {
        try {
            ProductResponseDto productResponseDto = productService.getProduct(username, productCode);
            return ResponseEntity.status(HttpStatus.FOUND).body(productResponseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("save-product/{username}")
    public ResponseEntity<Void> saveProduct(@RequestBody ProductNewDto productNewDto, @PathVariable String username) {
        //Why use Void: In REST APIs, we typically don't return primitive values (like Boolean)
        try {
            productService.saveProduct(productNewDto, username);
            return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created on success
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build(); // Handles custom exceptions (e.g., user not found)
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build(); // Handles unexpected errors
        }
    }

    @PutMapping("update-product/{username}")
    public ResponseEntity<Void> updateProduct(@RequestBody ProductUpdateDto productUpdateDto, @PathVariable String username) {
        try {
            productService.updateProduct(productUpdateDto, username);
            return ResponseEntity.status(HttpStatus.OK).build(); // 200 Created on success
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build(); // Handles custom exceptions (e.g., user not found)
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build(); // Handles unexpected errors
        }
    }

}
