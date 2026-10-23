package com.example.myapplication.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Fruit {
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private int id;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private String expiryDate; // yyyy-MM-dd
    private String description;

    public Fruit(int id, String name, String category, double price, int quantity,
                 String expiryDate, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.description = description;
    }

    // A fruit is expired when its expiry date is before today.
    // yyyy-MM-dd strings sort the same way as the dates they represent.
    public boolean isExpired() {
        String today = new SimpleDateFormat(DATE_FORMAT, Locale.US).format(new Date());
        return expiryDate.compareTo(today) < 0;
    }

    // Text shown for each fruit in the list screens.
    @Override
    public String toString() {
        return name + (isExpired() ? "  [EXPIRED]" : "") +
                "\nCategory: " + category +
                "\nPrice: " + String.format(Locale.US, "%.2f", price) + "  |  Quantity: " + quantity +
                "\nExpiry date: " + expiryDate +
                (description == null || description.isEmpty() ? "" : "\n" + description);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
