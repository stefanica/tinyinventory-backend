package com.tinyinventory.app.controller;

import com.tinyinventory.app.model.Product;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {
        "http://localhost:5173", // for development
        "https://tinyinventory.com",
        "https://www.tinyinventory.com"
})
@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping({"/test"})
    public String home() {
        return "Hello World! TinyInventory (Barcode Scanner / Product Management App) is in development phase.";
    }

   /* @PostMapping("/addProduct")
    public String getBalance(Product product) {
        return "1500$";
    }*/



}
