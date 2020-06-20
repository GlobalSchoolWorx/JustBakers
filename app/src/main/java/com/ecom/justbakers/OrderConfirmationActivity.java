package com.ecom.justbakers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ecom.justbakers.orders.OrderClass;
import com.firebase.client.Firebase;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class OrderConfirmationActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        Button btn = findViewById(R.id.backToHomeBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);

                startActivity(intent);
            }
        });
        saveOrderInDatabase();
    }

    public void saveOrderInDatabase () {

        String userId = LoginActivity.getDefaults("UserID", getApplicationContext());
        Firebase pendingOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+userId+"/orders/pending/cart");
        Firebase placedOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+userId+"/orders/placed");
        Date date = Calendar.getInstance().getTime();
        Long code = date.getTime();
        String scode = code.toString();
        scode = "#"+scode;
        OrderClass  order = new OrderClass(scode, CartActivity.getCurrentCartList(), CartActivity.getReceipt(), date, date.getTime(), "pending");

        placedOrderRef.child(code.toString()).setValue(order);
        //pendingOrderRef.removeEventListener(UserActivity.);

        pendingOrderRef.removeValue();

        if(CartActivity.getCurrentCartList() != null)
            CartActivity.getCurrentCartList().clear();
    }
}
