package com.tinyinventory.app.service;


import com.tinyinventory.app.dto.StockMovementBatchDto;
import com.tinyinventory.app.dto.StockMovementResponseDto;
import com.tinyinventory.app.model.*;
import com.tinyinventory.app.repo.StockMovementRepo;
import com.tinyinventory.app.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Service
public class StockMovementService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private StockMovementRepo stockMovementRepo;

    public List<StockMovementResponseDto> getAllStockMovements(String username) {

        //Used as response to this method. All User Stock Movements will be placed here
        List<StockMovementResponseDto> stockMovementResponseDtos = new ArrayList<>();

        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        //List<StockMovement> stockMovements = stockMovementRepo.findAllByUser(user); //Oder ASC
        List<StockMovement> stockMovements = stockMovementRepo.findAllByUserOrderByIdDesc(user); //Oder DESC

        if (!stockMovements.isEmpty()) {
            //For every Stock Movement/Checkout (All the products from one "shopping cart") of a User do:
            for (StockMovement stockMovement : stockMovements) {

                //Each StockMovementItem has one Product with one or multiple batches
                List<StockMovementItem> stockMovementItems = stockMovement.getItems();

                /// For every Item/Product moved I have to create a StockMovementBatchDto
                /// And the must be added to a List
                List<StockMovementBatchDto> stockMovementBatchDtos = new ArrayList<>();
                BigDecimal totalProfit = new BigDecimal(0);

                //For every Stock Movement Item do:
                for (StockMovementItem stockMovementItem : stockMovementItems) {
                    Product product = stockMovementItem.getProduct();
                    List<StockMovementBatch> stockMovementBatches = stockMovementItem.getBatches();


                    for (StockMovementBatch stockMovementBatch : stockMovementBatches) {
                        /// Create the StockMovementBatchDto and add: prodName, code, quantity, priceIn, priceOut, profit
                        StockMovementBatchDto stockMovementBatchDto = StockMovementBatchDto.builder()
                                .productName(product.getName())
                                .code(product.getCode())
                                .quantity(stockMovementBatch.getQuantity())
                                .priceIn(stockMovementBatch.getPriceIn())
                                .priceOut(product.getPriceOut())    //The line below calculates profit like this: priceOut.subtract(priceIn).multiply(BigDecimal.valueOf(quantity))
                                .profit(product.getPriceOut().subtract(stockMovementBatch.getPriceIn()).multiply(BigDecimal.valueOf(stockMovementBatch.getQuantity())))
                                .build();

                        //Add the bath to Batches List
                        stockMovementBatchDtos.add(stockMovementBatchDto);
                        //Add the profit to total profit
                        totalProfit = totalProfit.add(
                                product.getPriceOut()
                                        .subtract(stockMovementBatch.getPriceIn())
                                        .multiply(BigDecimal.valueOf(stockMovementBatch.getQuantity())));
                    }
                }
                //Create ONE StockMovementResponse (ONE Checkout -> Multiple Checkout Products)
                StockMovementResponseDto stockMovementResponseDto = StockMovementResponseDto.builder()
                        .movementDate(stockMovement.getMovementDate())
                        .stockMovementBatchDtoList(stockMovementBatchDtos)
                        .totalProfit(totalProfit)
                        .build();
                //Add the StockMovementResponse to the Response List created at the start
                stockMovementResponseDtos.add(stockMovementResponseDto);
            }
        }

        return stockMovementResponseDtos;
    }


}
