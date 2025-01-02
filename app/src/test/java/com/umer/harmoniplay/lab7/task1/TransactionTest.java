package com.umer.harmoniplay.lab7.task1;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TransactionTest {

    private ShoppingCart cart;
    private Product product1;
    private Product product2;

    @Before
    public void setup() {
        cart = new ShoppingCart();
        product1 = new Product("Laptop", 1000.0);
        product2 = new Product("Headphones", 100.0);
    }

    @Test
    public void testAddProductToCart() {
        cart.addProduct(product1);
        assertEquals(1, cart.getTotalItems());
        assertEquals(1000.0, cart.checkout());
    }

    @Test
    public void testRemoveProductFromCart() {
        cart.addProduct(product1);
        cart.addProduct(product2);
        cart.removeProduct(product2);
        assertEquals(1, cart.getTotalItems());
        assertEquals(1000.0, cart.checkout());
    }

    @Test
    public void testCheckout() {
        cart.addProduct(product1);
        cart.addProduct(product2);
        assertEquals(1100.0, cart.checkout());
    }

    @Test
    public void testClearCart() {
        cart.addProduct(product1);
        cart.clearCart();
        assertEquals(0, cart.getTotalItems());
        assertEquals(0.0, cart.checkout());
    }
}
