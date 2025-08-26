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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }
}