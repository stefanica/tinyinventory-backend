package com.tinyinventory.app.controller;

import com.tinyinventory.app.dto.ProductResponseDto;
import com.tinyinventory.app.dto.StockMovementResponseDto;
import com.tinyinventory.app.exceptions.ProductNotFoundException;
import com.tinyinventory.app.exceptions.UnauthorizedException;
import com.tinyinventory.app.service.StockMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@CrossOrigin(origins = {
        "http://localhost:5173", // for development
        "https://tinyinventory.com",
        "https://www.tinyinventory.com"
})
@RestController
@RequestMapping("/api")
public class StockMovementController {

    @Autowired
    StockMovementService stockMovementService;

    @GetMapping("/get-stock-movements")
    //public ResponseEntity<List<ProductResponseDto>> getProducts(@RequestBody TokenDto tokenDto) {
    public ResponseEntity<List<StockMovementResponseDto>> getAllStockMovements(@AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        List<StockMovementResponseDto> movementList = stockMovementService.getAllStockMovements(userDetails.getUsername());
        if (movementList.isEmpty()) {
            throw new ProductNotFoundException("No checkout products yet!");
        }

        return ResponseEntity.ok(movementList);
    }


}
