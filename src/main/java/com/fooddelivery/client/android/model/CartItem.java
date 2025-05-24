package com.fooddelivery.client.android.model;

import java.io.Serializable;
import java.util.Objects;

// Represents an item in the user's shopping cart on the client side.
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productName; // Assuming productName is unique identifier within a store context for cart
    private String storeName;   // To which store this cart item belongs
    private double pricePerItem;
    private int quantitySelected;
    // Could add productType or other display-relevant info if needed

    public CartItem(String productName, String storeName, double pricePerItem, int quantitySelected) {
        this.productName = productName;
        this.storeName = storeName;
        this.pricePerItem = pricePerItem;
        this.quantitySelected = quantitySelected;
    }

    // Getters
    public String getProductName() { return productName; }
    public String getStoreName() { return storeName; }
    public double getPricePerItem() { return pricePerItem; }
    public int getQuantitySelected() { return quantitySelected; }

    // Setters
    public void setProductName(String productName) { this.productName = productName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public void setPricePerItem(double pricePerItem) { this.pricePerItem = pricePerItem; }
    public void setQuantitySelected(int quantitySelected) { this.quantitySelected = quantitySelected; }

    public double getTotalPrice() {
        return pricePerItem * quantitySelected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(productName, cartItem.productName) &&
               Objects.equals(storeName, cartItem.storeName); // A cart item is unique by product and store
    }

    @Override
    public int hashCode() {
        return Objects.hash(productName, storeName);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "productName='" + productName + '\'' +
                ", storeName='" + storeName + '\'' +
                ", pricePerItem=" + pricePerItem +
                ", quantitySelected=" + quantitySelected +
                ", totalPrice=" + getTotalPrice() +
                '}';
    }
}
