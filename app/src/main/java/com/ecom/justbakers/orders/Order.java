package com.ecom.justbakers.orders;

import com.ecom.justbakers.Classes.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order implements Serializable {

    private  String ordernumber;
    private ArrayList<Product> cart;
    private OrderReceiptClass receipt;
    private Date date;
    private long time;
    private String status;

    public Order() {}
    public Order(String ordernumber, ArrayList<Product> cart, OrderReceiptClass receipt, Date date, long time, String status) {
        this.ordernumber = ordernumber;
        this.cart = cart;
        this.date = date;
        this.receipt = receipt;
        this.date = date;
        this.time = time;   // Time is in milliseconds
        this.status = status;
    }

    public Order(Order oc, String status) {

        this.ordernumber = oc.ordernumber;
        this.cart = oc.cart;
        this.date = oc.date;
        this.receipt = oc.receipt;
        this.date = oc.date;
        this.time = oc.time;   // Time is in milliseconds
        this.status = status;
    }

    public String   getOrdernumber() { return ordernumber;}
    public ArrayList<Product> getCart() { return cart;}
    public OrderReceiptClass getReceipt() {return receipt;}
    public Date getDate() { return  date;}

    public long getTime() { return  time;}
    public String getStatus() { return status;}

    public OrderStatus getOrderStatus(){
        return OrderStatus.from(status);
    }
    public void setOrderStatus(OrderStatus orderStatus){
        status = orderStatus.getValue();
    }

    public double getTimeElapsed() {
       Date curDate = Calendar.getInstance().getTime();
       long curTime = curDate.getTime();
       double elapsedTime = curTime - getTime();
       elapsedTime = elapsedTime / (1000 * 60 * 60);
       return elapsedTime;
    }

    public enum OrderStatus{
        //Please note these strings are in use in Firebase database. Please check firebase before changing these string values.
        PENDING("pending"),
        CONFIRMED("confirmed"),
        PROCESSING("processing"),
        DELIVERED("delivered"),
        CANCELLED("cancelled");

        private final String value;
        OrderStatus(String value){
            this.value = value;
        }

        public static OrderStatus from(String value){
            for(OrderStatus orderStatus : OrderStatus.values()){
                if(orderStatus.value.equals(value)){
                    return orderStatus;
                }
            }

            throw new RuntimeException("No Order Status Defined for : "+value);
        }

        @NonNull
        public String getValue() {
            return value;
        }

        @NonNull
        @Override
        public String toString() {
            return getValue();
        }
    }

    public static class OrderReceiptClass {

        private double  totalamount;
        private double  discount;
        private double  tax;
        private double  finalamount;
        private double  ordernumber;

        public OrderReceiptClass () { }
        public OrderReceiptClass(double  totalamount) {
            this.totalamount = totalamount;
            this.discount = 0.0;
            this.tax = 12.5;
            this.finalamount = totalamount;
        }

        public OrderReceiptClass(double  totalamount, double discount, double  tax, double  finalamount, double ordernumber) {
        this.totalamount = totalamount;
        this.discount = discount;
        this.tax = tax;
        this.finalamount = finalamount;
        this.ordernumber = ordernumber;
        }
        public double getTotalamount() {
            return totalamount;
        }

        public double getDiscount() {
            return discount;
        }

        public double getTax() {
            return tax;
        }
        public double getFinalamount() {
            return finalamount;
        }

        public double getOrdernumber() {
            return ordernumber;
        }
    }

}
