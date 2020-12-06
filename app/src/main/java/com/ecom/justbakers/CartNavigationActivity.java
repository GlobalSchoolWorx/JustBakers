package com.ecom.justbakers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.ecom.justbakers.Classes.Product;
import com.ecom.justbakers.orders.InfoClass;
import com.ecom.justbakers.orders.Order;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class CartNavigationActivity extends AppCompatActivity {
    private NavController mNavigationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_navigation_layout);
        Firebase.setAndroidContext(this);
        mNavigationController  = Navigation.findNavController(this, R.id.nav_cart_host_fragment);
        AppBarConfiguration configuration = new AppBarConfiguration.Builder(mNavigationController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, mNavigationController, configuration);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return mNavigationController.navigateUp();
    }
}
