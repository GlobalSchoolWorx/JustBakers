package com.ecom.justbakers;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ecom.justbakers.menu_item_listeners.MenuCartItemListener;
import com.ecom.justbakers.fragments.ProductsFragment;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class LaunchActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ProductsFragment.OnCartItemUpdateListener {
    public static final String INTENT_EXTRA_DATA_USER_DISPLAY_NAME = "userDisplayName";
    public static final String INTENT_EXTRA_DATA_GMAIL = "gmail";

    private TextView cartCounterTextView;
    private long customerCount;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        NavigationUI.setupActionBarWithNavController(this, navController, drawer);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);

        if(!LoginActivity.isUserLoggedIn(this)) {
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
        } else {
            MenuItem customerMenuItem = navigationView.getMenu().findItem(R.id.nav_menu_admin);
            customerMenuItem.setVisible(LoginActivity.isLoggedInUserAdmin(this));

            Firebase custCountRef = new Firebase("https://justbakers-285be.firebaseio.com/customers");
            custCountRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    customerCount = dataSnapshot.getChildrenCount();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        View header = navigationView.getHeaderView(0);
        TextView email = header.findViewById(R.id.email);
        email.setText(String.format(getString(R.string.user_email), LoginActivity.getLoggedInUser(this, false)));
        ((TextView)header.findViewById(R.id.Name)).setText(LoginActivity.getDefaults(this, LoginActivity.SESSION_KEY_DISPLAY_NAME));

        Button SignOut = header.findViewById(R.id.signout);
        SignOut.setOnClickListener(v -> {
            LoginActivity.signOut(LaunchActivity.this, () -> {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signOut();
                Intent signInIntent = new Intent(LaunchActivity.this, LoginActivity.class);
                LaunchActivity.this.startActivity(signInIntent);
                LaunchActivity.this.finish();
            });
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_menu_products) {
            navController.navigate(R.id.action_to_products_fragment);
        } else if (id == R.id.nav_menu_orders) {
            navController.navigate(R.id.action_to_orders_fragment);
        }else if (id == R.id.nav_menu_admin) {
            navController.navigate(R.id.action_to_admin_fragment);
            String str = "Customer Orders : " + customerCount;
            setActionBarTitle(str);
        }else if (id == R.id.nav_menu_contact) {
            navController.navigate(R.id.action_to_contacts_fragment);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.user, menu);

        View menuItemCart = menu.findItem(R.id.action_cart).getActionView();
        cartCounterTextView = menuItemCart.findViewById(R.id.cartcounter);
        MenuCartItemListener cartItemListener = new MenuCartItemListener(menuItemCart, "Checkout Cart") {
            @Override
            public void onClick(View v) {
                TextView countView = v.findViewById(R.id.cartcounter);

                if(getCartCount(countView) > 0 ) {
                    Intent Cart_Intent = new Intent(LaunchActivity.this, CartNavigationActivity.class);
                    LaunchActivity.this.startActivity(Cart_Intent);
                } else {
                    Toast toast = Toast.makeText(LaunchActivity.this, "", Toast.LENGTH_SHORT);
                    View view = toast.getView();
                    view.setBackgroundResource(R.drawable.rounded_square);
                    TextView text = view.findViewById(android.R.id.message);

                    text.setText(R.string.empty_cart);
                    text.setTextColor(getResources().getColor(R.color.colorWhite));
                    toast.show();
                }
            }
        };

        menuItemCart.setOnClickListener(cartItemListener);
        menuItemCart.setOnLongClickListener(cartItemListener);
        return true;
    }

    @Override
    public void update(int itemCount) {
        updateCartCount(itemCount);
    }

    private int getCartCount(TextView cartCounterTextView){
        try {
            if(!TextUtils.isEmpty(cartCounterTextView.getText().toString())) {
                return Integer.parseInt(cartCounterTextView.getText().toString());
            }
        }catch(NumberFormatException e){
            return 0;
        }
        return 0;
    }

    private void updateCartCount(final int itemCount) {
        if(!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED)) return;

        runOnUiThread(() -> {
            if (cartCounterTextView == null) {
                //if cartCounterTextView is yet null, the options menu has not yet been created. Reschedule after toolbar animation is finished.
                Toolbar toolbar = findViewById(R.id.toolbar);
                ViewCompat.postOnAnimationDelayed(toolbar, () -> {
                    cartCounterTextView.setText(String.format(Locale.getDefault(), "%d", itemCount));
                    if (itemCount == 0) {
                        cartCounterTextView.setVisibility(View.INVISIBLE);
                    }else{
                        cartCounterTextView.setVisibility(View.VISIBLE);
                    }
                }, 50);
                return;
            }

            cartCounterTextView.setText(String.format(Locale.getDefault(), "%d", itemCount));
            if (itemCount == 0) {
                cartCounterTextView.setVisibility(View.INVISIBLE);
            }else{
                cartCounterTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}
