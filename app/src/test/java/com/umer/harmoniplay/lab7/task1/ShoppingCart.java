package com.umer.harmoniplay.lab7.task1;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private List<Product> cart;
    private double totalAmount;

    public ShoppingCart() {
        this.cart = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    public void addProduct(Product product) {
        cart.add(product);
        totalAmount += product.getPrice();
    }

    public void removeProduct(Product product) {
        cart.remove(product);
        totalAmount -= product.getPrice();
    }

    public double checkout() {
        return totalAmount;
    }

    public int getTotalItems() {
        return cart.size();
    }

    public void clearCart() {
        cart.clear();
        totalAmount = 0.0;
    }
}

