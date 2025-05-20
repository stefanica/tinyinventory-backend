package com.tinyinventory.app.controller;

import com.tinyinventory.app.model.Product;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping({"/","/home"})
    public String home() {
        return "Hello World! TinyInventory (Barcode Scanner / Product Management App) is in development phase.";
    }

    @PostMapping("/addProduct")
    public String getBalance(Product product) {
        return "1500$";
    }



}
