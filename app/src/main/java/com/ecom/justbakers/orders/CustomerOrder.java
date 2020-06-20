package com.ecom.justbakers.orders;

import com.ecom.justbakers.Adapters.CustomOrderListAdapter;

import java.io.Serializable;
import java.util.Map;

public class CustomerOrder implements Serializable {
    OrderListClass orders;
    /*
    public CustomerOrder (Map<String, OrderListClass>  map) {

        orders = map.get("orders");
    }

     */
    public CustomerOrder () {

    }

    public OrderListClass getOrders() {
        return orders;
    }
}
