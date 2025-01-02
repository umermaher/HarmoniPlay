package com.umer.harmoniplay.lab7.task2;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class BankAccountTest {

    private BankAccount account;

    @Before
    public void setup() {
        account = new BankAccount();
    }

    @Test
    public void testOpenAccount() {
        assertFalse(account.isOpen());
        account.openAccount();
        assertTrue(account.isOpen());
    }

    @Test
    public void testDepositFunds() {
        account.openAccount();
        account.deposit(100);
        assertEquals(100, account.getBalance());
    }

    @Test
    public void testWithdrawFunds_PositiveBalance() {
        account.openAccount();
        account.deposit(200);
        assertTrue(account.withdraw(100));
        assertEquals(100, account.getBalance());
    }

    @Test
    public void testWithdrawFunds_InsufficientBalance() {
        account.openAccount();
        account.deposit(50);
        // current balance should be more than the withdrawal amount so the overdraft can only be allowed
        assertFalse(account.withdraw(100)); // Withdrawal denied due to insufficient balance
        assertEquals(50, account.getBalance());
    }

    @Test
    public void testWithdrawWithOverdraft() {
        account.openAccount();
        account.deposit(100);
        assertTrue(account.withdraw(150)); // Allows overdraft
        assertEquals(-50, account.getBalance());
        assertFalse(account.withdraw(100)); // No more overdrafts allowed
    }

    @Test
    public void testCloseAccount_PositiveBalance() {
        account.openAccount();
        account.deposit(100);
        account.withdraw(100); // Balance back to zero
        account.closeAccount();
        assertFalse(account.isOpen()); // Should close successfully
    }

    @Test
    public void testCannotCloseAccountWithBalance() {
        account.openAccount();
        account.deposit(100);
        account.closeAccount();
        assertTrue(account.isOpen()); // Can't close account since balance isn't zero
    }

    @Test
    public void testHasPositiveBalance() {
        account.openAccount();
        account.deposit(200);
        assertTrue(account.hasPositiveBalance());
        account.withdraw(200);
        assertFalse(account.hasPositiveBalance());
    }
}

