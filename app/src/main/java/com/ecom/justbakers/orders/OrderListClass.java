package com.ecom.justbakers.orders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

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
    ArrayList<OrderClass> pending;
    ArrayList<OrderClass> placed;
    ArrayList<OrderClass> delivered;

    public ArrayList<OrderClass> getPending() {
        return pending;
    }

    public ArrayList<OrderClass> getPlaced() {
        return placed;
    }

    public ArrayList<OrderClass> getDelivered() {
        return delivered;
    }
}
