package com.ecom.justbakers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ecom.justbakers.orders.InfoClass;
import com.ecom.justbakers.orders.OrderClass;
import com.ecom.justbakers.sms_verify.PhoneAuthActivity;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class OrderConfirmationActivity extends AppCompatActivity {
    String confirmationMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Firebase msgRef = new Firebase("https://justbakers-285be.firebaseio.com/config/confirmationmsg");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        msgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                com.romainpiel.shimmer.ShimmerTextView sTV = findViewById(R.id.Tagline);
                confirmationMsg = (String) dataSnapshot.getValue();
                sTV.setText(confirmationMsg);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        Button btn = findViewById(R.id.backToHomeBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);

                startActivity(intent);
            }
        });



        saveOrderInDatabase();
        sendMessageToAdmin();
    }

    void sendMessageToAdmin () {



    }

    protected void sendEmail() {
        Log.i("Send email", "");
        String[] TO = {"ikbirkaur13@gmail.com"};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Order For Just Bakers");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(OrderConfirmationActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
    public void saveOrderInDatabase () {

        String userId = LoginActivity.getDefaults("UserID", getApplicationContext());
        Firebase pendingOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+userId+"/orders/pending/cart");
        Firebase placedOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+userId+"/orders/placed");
        Firebase counterRef = new Firebase("https://justbakers-285be.firebaseio.com/slotbooking");
        Firebase totalPendingOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/pendingOrders");
        Date date = Calendar.getInstance().getTime();
        String curDate = String.valueOf(date.hashCode());
        Long code = date.getTime();
        String oscode = code.toString();
        String scode = "#"+oscode;

        if ( CartActivity.getReceipt() != null && CartActivity.getCurrentCartList() != null ) {

            InfoClass info = new InfoClass(LoginActivity.getDefaults("Gmail", this),  LoginActivity.getDefaults("Name", this),
                    LoginActivity.getDefaults("Phone", this), LoginActivity.getDefaults("Area", this),
                    LoginActivity.getDefaults("Society", this), LoginActivity.getDefaults("Flat", this));

            OrderClass order = new OrderClass(scode, CartActivity.getCurrentCartList(), CartActivity.getReceipt(), date, date.getTime(), "pending");

            placedOrderRef.child(code.toString()).setValue(order);

            totalPendingOrderRef.child(oscode).child("info").setValue(info);

            totalPendingOrderRef.child(oscode).child("order").setValue(order);
            pendingOrderRef.removeValue();
         //   counterRef.child(code.toString());
       //     (counterRef.child(curDate.toString()).child(scode)).push();
         //   (counterRef.child(curDate.toString()).child(scode)).setValue(order);
            if (CartActivity.getCurrentCartList() != null)
                CartActivity.getCurrentCartList().clear();
        }
        else {
            Log.i("saveOrderInDatabase", "ERROR : Receipt is NULL");
        }
    }
}
