package com.example.firebaseapp;

public class Item
{
    private String name;
    private int quantity;
    private double price;
    private boolean bought;

    public Item (String name, int quantity, double price, boolean bought)
    {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.bought = bought;
    }

    public Item () {}
}
