package com.tinyinventory.app.controller;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {
        "http://localhost:5173", // for development
        "https://tinyinventory.com",
        "https://www.tinyinventory.com"
})
@RestController
@RequestMapping("/api")
public class ChartController { //Used for building charts on React DashBoard page




}
