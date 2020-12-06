package com.ecom.justbakers.fragments;

import java.io.Serializable;

import androidx.annotation.NonNull;

public class TotalSummary implements Serializable {
    private final static double DELIVERY_THRESHOLD_CHARGES = 300.0d;
    private final static double DELIVERY_CHARGES = 30.0d;

    private double amount;
    private double discount;

    public void reset() {
        amount = 0.0d;
        discount = 0.0d;
    }

    public double getThresholdForDeliveryCharges(){
        return DELIVERY_THRESHOLD_CHARGES;
    }

    public double getShippingCharges() {
        if (Double.compare(amount, DELIVERY_THRESHOLD_CHARGES) < 0) {
            return DELIVERY_CHARGES;
        }

        return 0.0;
    }

    public void addAmount(double amount){
        this.amount += amount;
    }

    public void addDiscount(double discount){
        this.discount = discount;
    }

    public double getAmount() {
        if (Double.compare(amount, DELIVERY_THRESHOLD_CHARGES) < 0 && Double.compare(amount, 0.0) > 0) {
            return (amount - discount + DELIVERY_CHARGES);
        }

        return (amount - discount);
    }

    public double getDiscount() {
        return discount;
    }

    @NonNull
    @Override
    public String toString() {
        return "TotalSummary{" +
                "amount=" + getAmount() +
                ", discount=" + discount +
                ", shippingCharges=" + getShippingCharges() +
                '}';
    }
}
