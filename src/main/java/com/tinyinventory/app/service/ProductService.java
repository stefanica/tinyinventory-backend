package com.tinyinventory.app.service;


import com.tinyinventory.app.dto.ProductNewDto;
import com.tinyinventory.app.dto.ProductResponseDto;
import com.tinyinventory.app.dto.ProductUpdateDto;
import com.tinyinventory.app.dto.UserResponseDto;
import com.tinyinventory.app.model.Product;
import com.tinyinventory.app.model.User;
import com.tinyinventory.app.repo.ProductRepo;
import com.tinyinventory.app.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ProductRepo productRepo;


    public List<ProductResponseDto> getAllUserProducts(String username) {
        int userId = 0;

        //Get useId from database if we know the username
        Optional<User> userOptional = userRepo.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return new ArrayList<>();
        } else {
            userId = userOptional.get().getId();
        }

        //*** Used when the field userId was active in Product entity
        //get the list of products. This also contains User, that contains password
        //Optional<List<Product>> optionalProductList = productRepo.findAllByUserId(userId);
        //List<Product> productList = optionalProductList.orElseGet(ArrayList::new);

        List<Product> productList = productRepo.findAllByUser(userOptional.get());

        //create a new list where the Users are stripped of passwords
        List<ProductResponseDto> response = new ArrayList<>();
        for (Product product : productList) {
            response.add(new ProductResponseDto(product));
        }

        return response;
    }

    public ProductResponseDto getProduct(String username, Long code) {
        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        try {
            //********IMPORTANT: modify so that the product is retrieved by code and username/user
            Product product = productRepo.findProductByCode(code)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
            return new ProductResponseDto(product);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to search for product");
        }

    }

    public void saveProduct(ProductNewDto productNewDto, String username) {
        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        //user.setPassword("redacted");

        Product product = new Product();
        product.setCode(productNewDto.getCode());
        product.setName(productNewDto.getName());
        product.setPriceIn(productNewDto.getPriceOut());
        product.setPriceOut(productNewDto.getPriceIn());
        product.setQuantity(productNewDto.getQuantity());
        product.setUpdatedAt(LocalDateTime.now());
        //product.setUserId(user.getId());
        product.setUser(user);

        try {
            productRepo.save(product);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save product");
        }
    }

    public void updateProduct(ProductUpdateDto productUpdateDto, String username) {
        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        //First check to see if the priceIn of the product is the same as priceIn of updated product
        //IF NOT then create a new product, almost identically with the old one, but wih NEW priceIn4
        //***********TO IMPLEMENT *********


        Product product = new Product();
        product.setId(productUpdateDto.getId());
        product.setCode(productUpdateDto.getCode());
        product.setName(productUpdateDto.getName());
        product.setPriceIn(productUpdateDto.getPriceOut());
        product.setQuantity(productUpdateDto.getQuantity());
        product.setUpdatedAt(LocalDateTime.now());
        product.setUser(user);

        try {
            productRepo.save(product);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update product");
        }
    }


}
