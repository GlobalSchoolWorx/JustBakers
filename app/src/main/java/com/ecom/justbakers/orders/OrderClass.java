package com.ecom.justbakers.orders;

import com.ecom.justbakers.Classes.ProductClass;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderClass implements Serializable {

    private  String ordernumber;
    private ArrayList<ProductClass> cart;
    private OrderReceiptClass receipt;
    private Date date;
    private long time;
    private String status;

    /*
   public OrderClass (Map <String, String> map) {
       Set<String> ks = map.keySet();

       ArrayList<String> listOfKeys = new ArrayList<String>(ks);

       for (String key : listOfKeys)
       {
           System.out.println(key);

           this.status = map.get("status");
       }
   }

     */
    public OrderClass() {
        /*** THIS IS IMPORT FOR CREATING JSON OBJEECT FROM FIREBASE ***/
    }
    public OrderClass (String ordernumber, ArrayList<ProductClass> cart, OrderReceiptClass receipt, Date date, long time, String status) {

        this.ordernumber = ordernumber;
        this.cart = cart;
        this.date = date;
        this.receipt = receipt;
        this.date = date;
        this.time = time;   // Time is in milliseconds
        this.status = status;
    }

    public OrderClass(OrderClass oc, String status) {

        this.ordernumber = oc.ordernumber;
        this.cart = oc.cart;
        this.date = oc.date;
        this.receipt = oc.receipt;
        this.date = oc.date;
        this.time = oc.time;   // Time is in milliseconds
        this.status = status;
    }

    public String   getOrdernumber() { return ordernumber;}
    public ArrayList<ProductClass> getCart() { return cart;}
    public OrderReceiptClass getReceipt() {return receipt;}
    public Date getDate() { return  date;}

    public long getTime() { return  time;}
    public String getStatus() { return status;}

    public double getTimeElapsed() {
       Date curDate = Calendar.getInstance().getTime();
       long curTime = curDate.getTime();
       double elapsedTime = curTime - getTime();
       elapsedTime = elapsedTime / (1000 * 60 * 60);
       return elapsedTime;
    }


    public static class OrderReceiptClass {

        private double  totalamount;
        private double  discount;
        private double  tax;
        private double  finalamount;
        private double  ordernumber;

        OrderReceiptClass () {
            /*** THIS IS IMPORT FOR CREATING JSON OBJEECT FROM FIREBASE ***/
        }
        public OrderReceiptClass(double  totalamount) {
            this.totalamount = totalamount;
            this.discount = 0.0;
            this.tax = 12.5;
            this.finalamount = totalamount;
        }

        OrderReceiptClass(double  totalamount, double discount, double  tax, double  finalamount, double ordernumber) {
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
