package com.tinyinventory.app.exceptions;

public class CheckoutQuantityBiggerThanInventory extends RuntimeException {
    public CheckoutQuantityBiggerThanInventory(String message) {
        super(message);
    }
}
