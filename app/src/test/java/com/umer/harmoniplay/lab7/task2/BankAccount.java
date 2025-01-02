package com.umer.harmoniplay.lab7.task2;

public class BankAccount {
    private double balance;
    private boolean isOpen;
    private boolean hasOverdraft;

    public BankAccount() {
        this.balance = 0.0;
        this.isOpen = false;
        this.hasOverdraft = false;
    }

    // Method to open account
    public void openAccount() {
        if (!isOpen) {
            isOpen = true;
            System.out.println("Account opened successfully.");
        } else {
            System.out.println("Account is already open.");
        }
    }

    // Method to close account
    public void closeAccount() {
        if (isOpen && balance == 0) {
            isOpen = false;
            System.out.println("Account closed successfully.");
        } else {
            System.out.println("Cannot close account. Ensure balance is zero.");
        }
    }

    // Method to deposit funds
    public void deposit(double amount) {
        if (isOpen && amount > 0) {
            balance += amount;
            System.out.println("Deposited: " + amount);
        } else {
            System.out.println("Account is closed or invalid deposit amount.");
        }
    }

    // Method to withdraw funds
    public boolean withdraw(double amount) {
        if (!isOpen) {
            System.out.println("Account is closed. Cannot withdraw funds.");
            return false;
        }

        if (amount <= 0) {
            System.out.println("Invalid withdrawal amount.");
            return false;
        }

        if (balance >= amount) {
            balance -= amount;
            System.out.println("Withdrew: " + amount);
            return true;
        }
        else if (balance > (amount / 2f) && !hasOverdraft) {
            hasOverdraft = true;
            balance -= amount;
            System.out.println("Withdrew with overdraft: " + amount);
            return true;
        }
        else {
            System.out.println("Insufficient funds and overdraft not allowed.");
            return false;
        }
    }

    // Method to check if account is open
    public boolean isOpen() {
        return isOpen;
    }

    // Method to check if balance is positive
    public boolean hasPositiveBalance() {
        return balance > 0;
    }

    // Method to get current balance
    public double getBalance() {
        return balance;
    }
}

