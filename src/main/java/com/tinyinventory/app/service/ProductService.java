package com.tinyinventory.app.service;


import com.tinyinventory.app.dto.*;
import com.tinyinventory.app.exceptions.CheckoutQuantityBiggerThanInventory;
import com.tinyinventory.app.exceptions.ProductNotFoundException;
import com.tinyinventory.app.model.*;
import com.tinyinventory.app.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//*** This class works with Product and ProductBatch (models, repos)
@Service
public class ProductService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductBatchRepo  productBatchRepo;
    @Autowired
    private ProductHistoryRepo productHistoryRepo;
    @Autowired
    private StockMovementRepo stockMovementRepo;
    @Autowired
    private StockMovementItemRepo stockMovementItemRepo;
    @Autowired
    private StockMovementBatchRepo stockMovementBatchRepo;


    //Returns a list WHERE there is a NEW Product for EVERY ProductBatch
    public List<ProductResponseDto> getAllUserProducts(String username) {

        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        //*** Used when the field userId was active in Product entity
        //get the list of products. This also contains User, that contains password
        //Optional<List<Product>> optionalProductList = productRepo.findAllByUserId(userId);
        //List<Product> productList = optionalProductList.orElseGet(ArrayList::new);

        List<Product> productList = productRepo.findAllByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No products found"));


        List<ProductResponseDto> productResponseDtoList = new ArrayList<>();

        //First get each product from database
        //Then for each product get each ProductBatch
        //An last, create a new ProductResponseDto using product and corresponding productBatch items
        productList.forEach(product -> {
            List<ProductBatch> productBatchList = getBatchesOrThrow(product);

            //Show only products/batches with positive quantity
            productBatchList.forEach(productBatch -> {
                if (productBatch.getQuantity() > 0) {
                    productResponseDtoList.add(new ProductResponseDto(product, productBatch));
                }
            });

        });

        //Alternative solution to the .forEach approach:
        /*
        productResponseDtoList = productList.stream()
                .flatMap(product -> productBatchRepo.findProductBatchByProduct(product)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ProductBatch not found"))
                        .stream()
                        .map(batch -> new ProductResponseDto(product, batch))
                )
                .toList();
        */

        return productResponseDtoList;
    }



    //This method will return a list of ProductResponseDto
    //For every ProductBatch associated with a Product will create a new ProductResponseDto and add it to the list
    //For Example: 1 Product and 3 ProductBatches => 3 ProductResponseDto
    public List<ProductResponseDto> getOneProductAndAllProductBatches(String username, Long code) {

        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Product product = productRepo.findByCodeAndUser(code, user)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        List<ProductBatch> productBatchList = getBatchesOrThrow(product);

        //Create a ProductResponseDto List and populate
        List<ProductResponseDto> productResponseDtoList = new ArrayList<>();
        productBatchList.forEach(item -> productResponseDtoList.add(new ProductResponseDto(product, item)));

        return productResponseDtoList;
    }


    //Will return Product (by code and user) and one ProductBatch (by product and id)
    //USE: designed to be used in React to EDIT a specific product. ***This to be used only to correct mistakes
    public ProductResponseDto getOneProductAndOneProductBatch(String username, Long productCode, Long productBatchId) {
        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Product product = productRepo.findByCodeAndUser(productCode, user)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        ProductBatch productBatch = productBatchRepo.findByIdAndProduct(productBatchId, product)
                .orElseThrow(() -> new ProductNotFoundException("ProductBatch not found"));

        return new ProductResponseDto(product, productBatch);
    }


    //For Android Inventory: Shows the Product and averages the price_in of all hit Batches
    public List<ProductResponseDto> getAllProductsWithSumBatches(String username) {
        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Product> productList = productRepo.findAllByUser(user)
                .orElseThrow(() -> new ProductNotFoundException("No products yet in the account"));

        List<ProductResponseDto> productResponseDtoList = new ArrayList<>();

        for (Product product : productList) {
            ProductResponseDto productResponseDto = getOneProductAndSumAllProductBatches(product);
            if (productResponseDto != null)
                productResponseDtoList.add(productResponseDto);
        }

        if (productResponseDtoList.isEmpty())
            throw new ProductNotFoundException("No products yet in the account");

        return productResponseDtoList;
    }


    //Used in Android to check/search a Product by code, when Add or Remove
    //Get Product info and the last Batch corresponding to that product
    public ProductResponseDto getLastProductInfoBatch(String username, Long productCode) {

        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Product product = productRepo.findByCodeAndUser(productCode, user)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        List<ProductBatch> batches = productBatchRepo.findByProductOrderByUpdatedAtDesc(product);
        int quantitySum = 0;
        if (batches.isEmpty()) {
            throw new ProductNotFoundException("ProductBatch not found");
        } else {
            for (ProductBatch batch : batches) {
                quantitySum += batch.getQuantity();
            }

            return ProductResponseDto.builder()
                    .productId(product.getId())
                    .productBatchId(batches.get(0).getId())
                    .name(product.getName())
                    .code(product.getCode())
                    .quantityThreshold(product.getQuantityThreshold())
                    .quantity(quantitySum)
                    .priceIn(batches.get(0).getPriceIn())
                    .priceOut(product.getPriceOut())
                    .updatedAt(batches.get(0).getUpdatedAt())
                    .build();
        }
    }

    //First save the new product to also get an id
    //Then retrieve the new product and use it to create a new ProductBatch
    public void saveProduct(ProductNewDto productNewDto, String username) {
        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        //user.setPassword("redacted");

        //Check is the product already exists.
        boolean containsProduct = false;
        Optional<Product> tempProduct = productRepo.findByCodeAndUser(productNewDto.getCode(), user);
        if (tempProduct.isPresent()) {
            containsProduct = true;
        }

        //IF the Products table doesn't contain a row with that code for that user, create one
        if (!containsProduct) {
            saveNewProduct(productNewDto, user);

            //ELSE save an existing or update and existing product
        } else {
            saveExistingProduct(productNewDto, user);
        }

        //After saving a new or existing Product ALSO save the details in a ProductHistory table row
        saveProductHistory(user, productNewDto);
    }



    public void saveNewProduct(ProductNewDto productNewDto, User user) {
        //Create a new product using @Builder annotation
        Product product = Product.builder()
                .user(user)
                .name(productNewDto.getName())
                .code(productNewDto.getCode())
                .priceOut(productNewDto.getPriceOut())
                .quantityThreshold(productNewDto.getQuantityThreshold())
                .build();

        try {
            //First save and then retrieve. Because by saving it, the product will also get an id
            productRepo.save(product);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save product");
        }

        //Retrieve the newly saved product and use it to create a new ProductBatch
        Product retrieveProduct = productRepo.findByCodeAndUser(productNewDto.getCode(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found! Possible wrong code."));

        //Create a new ProductBatch entity of the newly created product
        ProductBatch productBatch = ProductBatch.builder()
                .product(retrieveProduct)
                .priceIn(productNewDto.getPriceIn())
                .quantity(productNewDto.getQuantity())
                .updatedAt(LocalDateTime.now())
                .build();



        try {
            //save the new ProductBatch
            productBatchRepo.save(productBatch);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save productBatch");
        }

    }



    public void saveExistingProduct(ProductNewDto productNewDto, User user) {
        //Get the product you want to update from database. It will be used to search in ProductBatch
        Product product = productRepo.findByCodeAndUser(productNewDto.getCode(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found! Possible wrong code."));

        //Use product from above to retrieve a list of corresponding ProductBatch items
        List<ProductBatch> productBatchList = getBatchesOrThrow(product);

        boolean existingPriceIn = false;

        //Check if the updated price_in already exists
        //IF it exists, update it
        //IF NOT create a new ProductBatch item and save it
        for (int i = 0; i < productBatchList.size(); i++) {
            ProductBatch temp = productBatchList.get(i);
            if (productNewDto.getPriceIn().compareTo(temp.getPriceIn()) == 0) {
                existingPriceIn = true;
                temp.setQuantity(temp.getQuantity() + productNewDto.getQuantity());
                try {
                    temp.setUpdatedAt(LocalDateTime.now());
                    productBatchRepo.save(temp);
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update ProductBatch");
                }
                break;
            }
        }

        //IF there is no ProductBatch item with the new price_in
        //We will create a new one and save it in the ProductBatch table
        if (!existingPriceIn) {
            ProductBatch productBatch = ProductBatch.builder()
                    .product(product)
                    .priceIn(productNewDto.getPriceIn())
                    .quantity(productNewDto.getQuantity())
                    .updatedAt(LocalDateTime.now())
                    .build();
            try {
                productBatchRepo.save(productBatch);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save new ProductBatch");
            }
        }

        //User can decide to change all fields of an existing Product, so we have to
        //Update Product object and save it
        product.setName(productNewDto.getName());
        product.setCode(productNewDto.getCode());
        product.setPriceOut(productNewDto.getPriceOut());
        product.setQuantityThreshold(productNewDto.getQuantityThreshold());
        try {
            productRepo.save(product);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update Product");
        }

    }


    public void saveProductHistory(User user, ProductNewDto productNewDto) {
        Product retrieveProduct = productRepo.findByCodeAndUser(productNewDto.getCode(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found! Possible wrong code."));

        //Create new ProductHistory entity
        ProductHistory productHistory = ProductHistory.builder()
                .product(retrieveProduct)
                .user(user)
                .name(retrieveProduct.getName())
                .code(retrieveProduct.getCode())
                .priceIn(productNewDto.getPriceIn())
                .priceOut(productNewDto.getPriceOut())
                .quantity(productNewDto.getQuantity())
                .createdAt(LocalDateTime.now())
                .build();

        try {
            //save the new ProductHistory
            productHistoryRepo.save(productHistory);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save productHistory");
        }

    }


    //Update product and product batch fields
    public void updateProduct(ProductUpdateDto productUpdateDto, String username) {
        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        //Get the product you want to update from database.
        Product product = productRepo.findByCodeAndUser(productUpdateDto.getCode(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found! Possible wrong code."));

        //update product with data from productUpdateDto and save it
        product.setName(productUpdateDto.getName());
        product.setCode(productUpdateDto.getCode());
        product.setPriceOut(productUpdateDto.getPriceOut());
        product.setQuantityThreshold(productUpdateDto.getQuantityThreshold());
        try {
            productRepo.save(product);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update Product");
        }

        //Get the product batch you want to update from database.
        ProductBatch productBatch = productBatchRepo.findByIdAndProduct(productUpdateDto.getProductBatchId(), product)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not retrieve Product Batch details"));

        //update product batch with data from productUpdateDto and save it
        productBatch.setPriceIn(productUpdateDto.getPriceIn());
        productBatch.setQuantity(productUpdateDto.getQuantity());
        productBatch.setUpdatedAt(LocalDateTime.now());
        try {
            productBatchRepo.save(productBatch);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save new ProductBatch");
        }
    }


    //UPDATE ProductBatch by removing the quantity from the Batches for each Product
    ///******IMPORTANT: to add a last_checkout_date to the ProductBatch entity
    public void checkoutProducts(String username, List<ProductCheckoutDto> productCheckoutDtos) {

        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        //Create a StockMovement item to Record all Product Movements
        StockMovement stockMovement = StockMovement.builder()
                .user(user)
                .movementDate(LocalDateTime.now())
                .items(new ArrayList<StockMovementItem>())
                .build();
        stockMovement = stockMovementRepo.save(stockMovement); //save() method will returned the mapped entity with an id

        //Create a list of StockMovementItems to add all the Checkout Products
        //And at the end add the list to the stockMovement entity from above and update it
        ///List<StockMovementItem> stockMovementItems = new ArrayList<>(); //OLD Method
        List<StockMovementItem> stockMovementItems = stockMovement.getItems(); //NEW Method

        /// The FOR loops below will go through each Checkout Product and remove the quantity
        /// Remove from OLDEST Batch to NEWEST
        /// The first FOR goes through each Product
        for (ProductCheckoutDto productCheckoutDto : productCheckoutDtos) {
            Product product = productRepo.findByCodeAndUser(productCheckoutDto.getCode(), user).orElse(null);

            if (product != null) {
                int checkoutQuantity = productCheckoutDto.getQuantity();
                int totalTakenProductQuantity = 0; //To be used in the end

                List<ProductBatch> productBatches = productBatchRepo.findByProductOrderByUpdatedAtAsc(product);

                //For each Product create a StockMovementItem and add it to the list
                StockMovementItem stockMovementItem = StockMovementItem.builder()
                        .stockMovement(stockMovement)
                        .product(product)
                        .batches(new ArrayList<StockMovementBatch>())
                        .quantity(0)
                        .build();
                //Save and retrieve the stockMovementItem so it will have an id.
                //The id will be used by StockMovementBatch as a Foreign key
                stockMovementItem = stockMovementItemRepo.save(stockMovementItem);

                ///List<StockMovementBatch> stockMovementBatches = new ArrayList<>(); OLD Method (errors)
                List<StockMovementBatch> stockMovementBatches = stockMovementItem.getBatches(); //NEW Method


                /// The second FOR goes through each ProductBatch
                for (ProductBatch productBatch : productBatches) {
                    if (checkoutQuantity == 0) { break; } //Break and go to the next Product when quantity = 0

                    int batchQuantity = productBatch.getQuantity();
                    //Take products only from Batches with positive quantity
                    if (batchQuantity > 0) {
                        int takenBatchQuantity = 0;

                        //IF checkoutQuantity is equal or lower than the batch it will go to the next Product (break)
                        if (checkoutQuantity <= batchQuantity) {

                            int tempBatchQuantity = batchQuantity - checkoutQuantity; //New productBatch quantity

                            takenBatchQuantity = checkoutQuantity; //Because the Batch had enough quantity to use from one
                            //Will add to the existing takenProductQuantity. Will be just takenBatchQuantit IF all the checkoutQuantity can be supplied by one ProductBatch
                            //OR it can sum to multiple other batches (from multiple for loops)
                            totalTakenProductQuantity += takenBatchQuantity;

                            checkoutQuantity = 0; //0 because all was subtracted
                            productBatch.setQuantity(tempBatchQuantity); //set new the quantity to the productBatch

                            productBatch = productBatchRepo.save(productBatch); //Update productBatch

                            //Create a StockMovementBatch and add it to the list
                            StockMovementBatch stockMovementBatch = StockMovementBatch.builder()
                                    .stockMovementItem(stockMovementItem)
                                    .productBatch(productBatch)
                                    .priceIn(productBatch.getPriceIn())
                                    .quantity(takenBatchQuantity) //how many products we are taking out from this one Batch
                                    .build();
                            stockMovementBatch = stockMovementBatchRepo.save(stockMovementBatch); //save the MovementBatch
                            stockMovementBatches.add(stockMovementBatch); //add it to the list; to be used in @OneToMany batches of StockMovementItem

                            //ELSE subtracts the batchQuantity from checkoutQuantity and go to the next ProductBatch
                        } else {
                            checkoutQuantity = checkoutQuantity - batchQuantity;
                            //IF checkoutQuantity > batchQuantity that means we will take all the products from the batch
                            takenBatchQuantity = batchQuantity;
                            totalTakenProductQuantity += takenBatchQuantity; //Adding it to the total taken amount

                            productBatch.setQuantity(0); //Will be 0 because it was all subtracted above
                            productBatch = productBatchRepo.save(productBatch);

                            //Create a StockMovementBatch and add it to the list
                            StockMovementBatch stockMovementBatch = StockMovementBatch.builder()
                                    .stockMovementItem(stockMovementItem)
                                    .productBatch(productBatch)
                                    .priceIn(productBatch.getPriceIn())
                                    .quantity(takenBatchQuantity) //How many products we are taking out
                                    .build();
                            stockMovementBatch = stockMovementBatchRepo.save(stockMovementBatch); //save the MovementBatch
                            stockMovementBatches.add(stockMovementBatch); //add it to the list; to be used in @OneToMany batches of StockMovementItem
                        }
                    }
                }

                //Throw an error if the checkout quantity was not cleared (not enough product patches in inventory/database)
                if (checkoutQuantity != 0) {
                    throw new CheckoutQuantityBiggerThanInventory("Not enough " + product.getName() + " items in inventory.");
                }

                //stockMovementItem.setBatches(stockMovementBatches); //Not needed because a reference to the List already exists
                stockMovementItem.setQuantity(totalTakenProductQuantity); //Set the total checked out quantity for a Moved Product

                stockMovementItems.add(stockMovementItem); //add the new item to the list

                ///Theoretically this is redundant because stockMovementRepo.save(stockMovement); from below will:
                /// Saving the parent (stockMovement) will cascade and save all StockMovementItems and their StockMovementBatch children.
                stockMovementItemRepo.save(stockMovementItem);
            }

            //add the StockMovementItems list to stockMovement Object/Entity created at the start of the method,
            // and update it (save) in the repo (Database)
            //stockMovement.setItems(stockMovementItems); //NOT Needed because stockMovement already has a reference to stockMovementItems List
            stockMovementRepo.save(stockMovement);

        }


    }



    /**************** Helper Methods *******************/
    //Get a list of Product Batched for a specific Product
    private List<ProductBatch> getBatchesOrThrow(Product product) {
        List<ProductBatch> batches = productBatchRepo.findByProduct(product);
        if (batches.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ProductBatch not found");
        }
        return batches;
    }



    //Designed to be used for displaying Products in Android Inventory activity
    //Works as a helper method to: ***getAllProductsWithSumBatches
    public ProductResponseDto getOneProductAndSumAllProductBatches(Product product) {

        List<ProductBatch> batches = productBatchRepo.findByProductOrderByUpdatedAtDesc(product);
        if (batches.isEmpty()) {
            return null;
        } else {
            int quantitySum = 0;
            BigDecimal priceInSum = BigDecimal.ZERO;
            int batchesWithPositiveQuantity = 0;
            for (ProductBatch batch : batches) {
                if (batch.getQuantity() > 0) {
                    priceInSum = priceInSum.add(batch.getPriceIn());
                    quantitySum += batch.getQuantity();
                    batchesWithPositiveQuantity++;
                }
            }
            /// OR Using stream and reference method
        /*BigDecimal priceInSum = batches.stream()
                .map(ProductBatch::getPriceIn)
                .reduce(BigDecimal.ZERO, BigDecimal::add);*/


            //Get the Average price_in by dividing the summed price_in with the total batches with positive quantity
            BigDecimal averagePriceIn = priceInSum.divide(
                    BigDecimal.valueOf(batchesWithPositiveQuantity),
                    4, // scale (number of decimal places you want)
                    RoundingMode.HALF_UP // common rounding mode
            );

            return ProductResponseDto.builder()
                    .productId(product.getId()) //not really needed for Android Inventory
                    .name(product.getName())
                    .code(product.getCode())
                    .quantityThreshold(product.getQuantityThreshold())
                    .quantity(quantitySum)
                    .priceIn(averagePriceIn)
                    .priceOut(product.getPriceOut())
                    .updatedAt(batches.get(0).getUpdatedAt())
                    .build();
        }
    }


}
