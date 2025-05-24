package com.fooddelivery.client.android.viewmodels;

import com.fooddelivery.client.android.model.CartItem;
import com.fooddelivery.client.android.repository.StoreRepository;
import com.fooddelivery.client.android.repository.ResponseCallback;
import com.fooddelivery.communication.payloads.PurchaseRequestPayload;
import com.fooddelivery.communication.payloads.OrderItemPayload;
import com.fooddelivery.communication.payloads.StatusResponsePayload;
import com.fooddelivery.client.android.network.MasterServerConnector; // For main method simulation (if used standalone)


import java.util.ArrayList;
import java.util.List;
import java.util.HashMap; // For managing cart items by product name
import java.util.Map;

public class CartViewModel {

    private StoreRepository storeRepository;
    private Map<String, CartItem> cartItems; // Key: productName for simplicity
    private String currentStoreName; // Cart is typically for one store at a time

    // Conceptual state
    private boolean isLoading;
    private String statusMessage; // For purchase success/failure

    public CartViewModel() {
        this.storeRepository = StoreRepository.getInstance();
        this.cartItems = new HashMap<>();
        this.isLoading = false;
    }

    public void setCurrentStore(String storeName) {
        // If changing store, clear the cart
        if (this.currentStoreName != null && !this.currentStoreName.equals(storeName)) {
            cartItems.clear();
            System.out.println("CartViewModel: Store changed to " + storeName + ", cart cleared.");
        } else if (this.currentStoreName == null) {
             System.out.println("CartViewModel: Cart store set to " + storeName);
        }
        this.currentStoreName = storeName;
    }
    
    public String getCurrentStoreName(){
        return this.currentStoreName;
    }

    public void addOrUpdateItem(String productName, double pricePerItem, int quantity) {
        if (currentStoreName == null || currentStoreName.isEmpty()) {
            statusMessage = "Error: Store not set for cart. Cannot add/update item.";
            System.err.println(statusMessage);
            // In a real app, might throw exception or use a specific error callback/state
            return;
        }
        if (quantity <= 0) {
            removeItem(productName);
            return;
        }
        CartItem item = cartItems.get(productName);
        if (item != null) {
            item.setQuantitySelected(quantity);
            System.out.println("CartViewModel: Updated quantity for " + productName + " to " + quantity + " in store " + currentStoreName);
        } else {
            item = new CartItem(productName, currentStoreName, pricePerItem, quantity);
            cartItems.put(productName, item);
            System.out.println("CartViewModel: Added " + productName + " (Qty: " + quantity + ", Price: " + pricePerItem + ") to cart for store " + currentStoreName);
        }
        // Notify observers of cart change
    }

    public void removeItem(String productName) {
        if (cartItems.remove(productName) != null) {
            System.out.println("CartViewModel: Removed " + productName + " from cart for store " + currentStoreName);
            // Notify observers of cart change
        }
    }

    public List<CartItem> getCartItemsList() {
        return new ArrayList<>(cartItems.values());
    }

    public double getTotalCartPrice() {
        double total = 0;
        for (CartItem item : cartItems.values()) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public void checkout() {
        if (currentStoreName == null || currentStoreName.isEmpty()) {
            statusMessage = "Error: Cannot checkout, no store selected for the cart.";
            System.err.println(statusMessage);
            // Notify observers of error
            return;
        }
        if (cartItems.isEmpty()) {
            statusMessage = "Cart is empty. Nothing to purchase.";
            System.out.println(statusMessage);
            // Notify observers (e.g., show a toast)
            return;
        }

        System.out.println("CartViewModel: Initiating checkout for store: " + currentStoreName + " with " + cartItems.size() + " item types.");
        isLoading = true;
        statusMessage = null;
        // Notify observers loading state true

        List<OrderItemPayload> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems.values()) {
            orderItems.add(new OrderItemPayload(cartItem.getProductName(), cartItem.getQuantitySelected()));
        }
        PurchaseRequestPayload requestPayload = new PurchaseRequestPayload(currentStoreName, orderItems);

        storeRepository.purchaseItems(requestPayload, new ResponseCallback<StatusResponsePayload>() {
            @Override
            public void onSuccess(StatusResponsePayload result) {
                isLoading = false;
                if (result != null && "SUCCESS".equalsIgnoreCase(result.getStatus())) {
                    statusMessage = "Purchase successful! " + (result.getMessage() != null ? result.getMessage() : "");
                    System.out.println("CartViewModel: " + statusMessage);
                    cartItems.clear(); 
                    // Notify observers success & cart change & loading state false
                } else {
                    statusMessage = "Purchase failed: " + (result != null && result.getMessage() != null ? result.getMessage() : "Unknown error from server.");
                    System.err.println("CartViewModel: " + statusMessage);
                    // Notify observers failure & loading state false
                }
            }

            @Override
            public void onError(String errorMsg) {
                isLoading = false;
                statusMessage = "Purchase error: " + errorMsg;
                System.err.println("CartViewModel: " + statusMessage);
                // Notify observers error & loading state false
            }
        });
    }

    public boolean isLoading() { return isLoading; }
    public String getStatusMessage() { return statusMessage; }
    
    // Simple main for standalone testing/simulation if desired
    public static void main(String[] args) {
        MasterServerConnector.getInstance().connect("localhost", 6000, new MasterServerConnector.ResponseCallback() {
            @Override
            public void onSuccess(String response) {
                System.out.println("CART_VM_SIM: Connected to Master: " + response);
                CartViewModel cartVM = new CartViewModel();
                cartVM.setCurrentStore("Pizzeria Uno"); // Simulate store selection
                
                cartVM.addOrUpdateItem("Pepperoni Pizza", 12.99, 1);
                cartVM.addOrUpdateItem("Coke", 1.50, 4);
                System.out.println("CART_VM_SIM: Cart items: " + cartVM.getCartItemsList().size());
                System.out.println("CART_VM_SIM: Cart total: $" + String.format("%.2f", cartVM.getTotalCartPrice()));
                
                cartVM.checkout();
                
                try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
                System.out.println("CART_VM_SIM: Checkout status: " + cartVM.getStatusMessage());
                MasterServerConnector.getInstance().disconnect();
            }
            @Override
            public void onError(Exception e) {
                System.err.println("CART_VM_SIM: Failed to connect: " + e.getMessage());
            }
        });
        try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
    }
}
