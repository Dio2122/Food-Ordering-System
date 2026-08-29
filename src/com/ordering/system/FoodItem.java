package com.ordering.system;

import java.math.BigDecimal;


public class FoodItem {

    private int itemId;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean isAvailable;
    private String imagePath; 

    
    public FoodItem()
    {}

    public FoodItem(int itemId, String name, String description, BigDecimal price, boolean isAvailable, String imagePath) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.isAvailable = isAvailable;
        this.imagePath = imagePath;
    }
    
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
    
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Override toString() to display nicely in UI components like JList or JComboBox.
     */
    @Override
    public String toString() {
        return String.format("%s - RM%.2f", name, price);
    }
}
