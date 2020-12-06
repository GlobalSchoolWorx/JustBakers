package com.ecom.justbakers.orders;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderListClass implements Serializable {
/*
    public OrderListClass (Map<String, ArrayList<OrderClass>> map) {
       this.pending =  map.get("pending");
       this.placed =  map.get("placed");
       this.delivered =  map.get("delivered");
    }

 */
    public OrderListClass() {

    }
    ArrayList<Order> pending;
    ArrayList<Order> placed;
    ArrayList<Order> delivered;

    public ArrayList<Order> getPending() {
        return pending;
    }

    public ArrayList<Order> getPlaced() {
        return placed;
    }

    public ArrayList<Order> getDelivered() {
        return delivered;
    }
}
