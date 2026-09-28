package com.ecommerce.dto;

import java.util.List;

public class OrderRequest {

    private int userId;
    private String shippingAddress;
    private String contactPhone;
    private List<OrderItemRequest> items;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}