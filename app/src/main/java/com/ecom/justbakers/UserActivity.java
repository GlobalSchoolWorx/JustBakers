package com.ecom.justbakers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ecom.justbakers.Adapters.CustomOrderListAdapter;
import com.ecom.justbakers.Adapters.CustomProductListAdapter;
import com.ecom.justbakers.Adapters.OrderDetailsListAdapter;
import com.ecom.justbakers.Classes.BargainProductClass;
import com.ecom.justbakers.Classes.ProductClass;
import com.ecom.justbakers.MenuItemListeners.MenuBargainItemListener;
import com.ecom.justbakers.MenuItemListeners.MenuCartItemListener;
import com.ecom.justbakers.orders.OrderClass;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class UserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ProgressDialog progress;
    //private static int cartIndex = 0;
    private int bargainCartIndex = 0;
    ListView productList;
    ProgressBar progressBar;
    private static OrderClass curOrder;
    private String userId = null;
    CustomProductListAdapter productAdapter;
    CustomOrderListAdapter orderAdapter;
    static  TextView cartcounterTV;
    TextView bargaincounterTV;
    private boolean pendingOrderFlag = true;
    private  boolean placedOrderFlag = false;
    private  boolean deliveredOrderFlag = false;

    ValueEventListener cartValueEventListener;
    ValueEventListener prodValueEventListener;
    /** MAKING A REFERENCE TO PRODUCT DISPLAY FIREBASE **/
    Firebase productRef = new Firebase("https://justbakers-285be.firebaseio.com/productDisplay");
    /** MAKING A REFERENCE TO CART URL IN FIREBASE FOR THE VALUES TO BE PUSHED **/
    Firebase cartRef = new Firebase("https://justbakers-285be.firebaseio.com/")  // https://justbakers-285be.firebaseio.com/
            .child("customers");
    /** MAKING A REFERENCE TO BARGAIN REQUESTS CART **/
    //  Firebase bargaincartRef = new Firebase("https://justbakers-285be.firebaseio.com/")
    //         .child("BargainCarts");
    /** MAKING A REFERENCE TO ORDERS LIST FIREBASE **/
    Firebase placedOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers");
    Firebase deliveredOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Firebase.setAndroidContext(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        productList =(ListView)findViewById(R.id.ProductList);


        /**CHECK IF IN ANY CASE USERNAME STRING IS NULL OR EMPTY GO BACK TO LOGIN ACTIVITY**/
        if(LoginActivity.getDefaults("UserID",this)==null || LoginActivity.getDefaults("UserID",this).equals(""))
        {
            Intent intent = new Intent(this,LoginActivity.class);
            this.startActivity(intent);
        }
        else {
            userId = LoginActivity.getDefaults("UserID",this);

            cartRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+userId+"/orders/pending")  // https://justbakers-285be.firebaseio.com/
                    .child("cart");
            placedOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+userId+"/orders/placed");

            deliveredOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+userId+"/orders/").child("delivered");


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        /**GETTING THE NAVIGATION BAR'S HEADER VIEW**/
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);

        /** SETTING THE TEXTS IN NAVIGATION HEADER **/
        TextView Email = (TextView) header.findViewById(R.id.Email);
        Email.setText(LoginActivity.getDefaults("UserID",this));
        TextView Name = (TextView) header.findViewById(R.id.Name);
        Name.setText(LoginActivity.getDefaults("UserID",this));

        /**RANDOM TIPS GENERATION**/
//        TextView UserTips = (TextView) findViewById(R.id.UserTips);
        Resources res = getResources();

        //       UserTips.setVisibility(View.GONE);
        String[] userTips = res.getStringArray(R.array.UserTips);
        Random random = new Random();
        String tip = userTips[random.nextInt(userTips.length)];
        //UserTips.setText(tip);

        /** CODE FOR SIGN OUT IN NAVIGATION HEADER **/
        Button SignOut = (Button) header.findViewById(R.id.signout);
        SignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signoutintent = new Intent(UserActivity.this, LoginActivity.class);
                UserActivity.this.startActivity(signoutintent);
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        /** INITIALISATION **/
        ArrayList<ProductClass> productClassList = new ArrayList<>();
        ArrayList<ProductClass> cartProductClassList = new ArrayList<>();
        ArrayList<OrderClass> placedOrdersClassList = new ArrayList<>();

        //      FirebaseBargainCartCount(bargaincartRef);

        /** CREATING AND STARTING THE PROGRESS BAR **/
        progress=new ProgressDialog(this);
        progress.setMessage(getResources().getString(R.string.Loading1));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();

        cartValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( !UserActivity.this.isFinishing() && UserActivity.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) ) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        ProductClass post = postSnapshot.getValue(ProductClass.class);
                        System.out.println(post.getName() + " - " + post.getDescription() + "-" + post.getId());
                        ProductClass currentProduct = new ProductClass(post.getName()
                                , post.getImage()
                                , post.getprice()
                                , post.getDescription()
                                , post.getSeller()
                                , post.getId()
                                , post.getCategory(), post.getQuantity(), post.getLimit());
                        cartProductClassList.add(currentProduct);
                    }

                    updateCartCount(cartProductClassList.size());
                    //productAdapter.setCartProductList(cartProductClassList);
                    //productList.invalidateViews();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        };

        cartRef.addValueEventListener(cartValueEventListener);

        /** ADDING THE VALUE EVENT LISTENER TO PRODUCTREF FOR DISPLAYING THE LIST OF PRODUCTS **/

        productAdapter = new CustomProductListAdapter(UserActivity.this
                , productClassList, cartProductClassList
                , getWindowManager().getDefaultDisplay().getWidth()
                , //* ADD TO CART BUTTON ON CLICK LISTENER *
                (position, v) -> {
                    //Custom Toast Display Function defined below - Displays custom toast message
                    //     CustomToastDisplay(UserActivity.this,getResources().getString(R.string.SuccessfulCart));
                    /**FUNCTION DEFINED IN THE LAST- PUSHING THE PRODUCT TO CART DATABASE**/
                    cartButtonClick(position, v, productClassList, cartRef);
                } // BARGAIN BUTTON ON CLICK LISTENER
                , (position, v) -> {
            /**FUNCTION DEFINED AT THE END OF THE CLASS, SHOW AN DIALOG ASKING FOR BARGAIN PRICE AND PUSH IT TO FIRBASE*/
            //  BargainButtonAlertDialog(UserActivity.this,position,v,ProductClassList,bargaincartRef);
        }, //ADD ITEM BUTTON ON CLICK LISTENER
                (position, v) -> {
                    //Custom Toast Display Function defined below - Displays custom toast message
                    //    CustomToastDisplay(UserActivity.this,getResources().getString(R.string.SuccessfulCart));
                    /**FUNCTION DEFINED IN THE LAST- PUSHING THE PRODUCT TO CART DATABASE**/
                    addItemButtonClick(position, v, productClassList, cartRef);
                }
                , //* SUB ITEM BUTTON ON CLICK LISTENER
                (position, v) -> {
                    // Custom Toast Display Function defined below - Displays custom toast message
                    // CustomToastDisplay(UserActivity.this,getResources().getString(R.string.SuccessfulCart));
                    // FUNCTION DEFINED IN THE LAST- PUSHING THE PRODUCT TO CART DATABASE
                    subItemButtonClick(position, v, productClassList, cartRef);
                });

        productList.setAdapter(productAdapter);

        prodValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( !UserActivity.this.isFinishing() && UserActivity.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) ) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        ProductClass post = postSnapshot.getValue(ProductClass.class);
                        System.out.println(post.getName() + " - " + post.getDescription() + "-" + post.getId());
                        ProductClass currentProduct = new ProductClass(post.getName()
                                , post.getImage()
                                , post.getprice()
                                , post.getDescription()
                                , post.getSeller()
                                , post.getId()
                                , post.getCategory()
                                , post.getLimit());
                        productClassList.add(currentProduct);
                    }

                    productAdapter.setProductList(productClassList);
                    productList.invalidateViews();
                    //productList.setAdapter(productAdapter);
                }
                progress.dismiss();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        };

        productRef.addValueEventListener(prodValueEventListener);

        /** ADDING THE VALUE EVENT LISTENER TO PRODUCTREF FOR DISPLAYING THE LIST OF PRODUCTS **/
        orderAdapter = new CustomOrderListAdapter(UserActivity.this
                , placedOrdersClassList
                ,/** ORDER DETAILS BUTTON ON CLICK LISTENER **/
                new CustomOrderListAdapter.OrderButtonClickListener(){
                    @Override
                    public void onButtonClick(int position,View v) {
                        /**FUNCTION DEFINED IN THE LAST- PUSHING THE PRODUCT TO CART DATABASE**/
                        orderTrackButtonClick(position,v, placedOrdersClassList, placedOrderRef);
                    }
                }
                ,/** ORDER DETAILS BUTTON ON CLICK LISTENER **/
                new CustomOrderListAdapter.OrderButtonClickListener(){
                    @Override
                    public void onButtonClick(int position,View v) {
                        /**FUNCTION DEFINED IN THE LAST- PUSHING THE PRODUCT TO CART DATABASE**/
                        orderDetailsButtonClick(position,v, placedOrdersClassList, placedOrderRef);
                    }
                }
                ,/** CANCEL ORDER BUTTON ON CLICK LISTENER **/
                new CustomOrderListAdapter.OrderButtonClickListener(){
                    @Override
                    public void onButtonClick(int position,View v) {
                        /**FUNCTION DEFINED IN THE LAST- PUSHING THE PRODUCT TO CART DATABASE**/
                        cancelOrderButtonClick(position,v, placedOrdersClassList);
                    }
                });

        placedOrderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( !UserActivity.this.isFinishing() && UserActivity.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) ) {
                    placedOrdersClassList.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        OrderClass post = postSnapshot.getValue(OrderClass.class);
                        OrderClass currentOrder = new OrderClass(post.getOrdernumber()
                                , post.getCart()
                                , post.getReceipt()
                                , post.getDate()
                                , post.getTime()
                                , post.getStatus());
                        placedOrdersClassList.add(currentOrder);
                    }

                    Collections.sort(placedOrdersClassList, orderClassComparator);
                    orderAdapter.setOrderList(placedOrdersClassList);
                    productList.invalidateViews();
                }
                progress.dismiss();

            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
/**
 DeliveredOrderRef.addValueEventListener(new ValueEventListener() {
@Override
public void onDataChange(DataSnapshot dataSnapshot) {
for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
OrderClass post = postSnapshot.getValue(OrderClass.class);
OrderClass currentOrder = new OrderClass( post.getOrdernumber()
,post.getCart()
,post.getReceipt()
,post.getDate()
,post.getTime()
,post.getStatus());
DeliveredOrdersClassList.add(currentOrder);
}

if(DeliveredOrdersClassList!=null && deliveredOrderFlag){
SETTING UP THE ADAPTER
orderAdapter = new CustomOrderListAdapter(UserActivity.this
,DeliveredOrdersClassList
, null
ORDER DETAILS BUTTON ON CLICK LISTENER
new CustomOrderListAdapter.OrderButtonClickListener(){
@Override
public void onButtonClick(int position,View v) {
FUNCTION DEFINED IN THE LAST- PUSHING THE PRODUCT TO CART DATABASE
OrderDetailsButtonClick(position,v,PlacedOrdersClassList,PlacedOrderRef);
}
});
DescriptionSET THE ADAPTER IN LISTVIEW AND DISMISS THE PROGRESS BAR
ProductList.setAdapter(adapter);
progress.dismiss();
}
}
@Override
public void onCancelled(FirebaseError firebaseError) {
System.out.println("The read failed: " + firebaseError.getMessage());
}
});

 **/
    }

    @Override
    protected void onResume() {
        // SETTING THE CART COUNTS IF THERE ARE ITEMS IN THE CART AND BARGAIN REQUESTS REFERENCES ALREADY
        firebaseCartCount(cartRef);
        super.onResume();
    }

    /** THIS FUNCTION WILL UPDATE THE TOTAL CART COUNT IN FIREBASE **/
    private void updateCartCounterInFirebase() {
        Firebase counterRef = new Firebase("https://justbakers-285be.firebaseio.com/userData/counts/cartCount");
        counterRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if(currentData.getValue() == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue((Long) currentData.getValue() + 1);
                }
                return Transaction.success(currentData); //we can also abort by calling Transaction.abort()
            }
            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                //This method will be called once with the results of the transaction.
            }
        });
    }

    /** THIS FUNCTION WILL UPDATE THE TOTAL CART COUNT IN FIREBASE **/
    public void resetCartCounterInFirebase() {

        updateCartCount(0);
        Firebase counterRef = new Firebase("https://justbakers-285be.firebaseio.com/userData/counts/cartCount");
        counterRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                currentData.setValue(0);

                return Transaction.success(currentData); //we can also abort by calling Transaction.abort()
            }
            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                //This method will be called once with the results of the transaction.
            }
        });
    }

    /**THIS FUNCTION WILL UPDATE THE BARGAIN REQUESTS COUNTER IN FIREBASE**/
    private void updateBargainCartCounterInFirebase() {
        Firebase counterRef = new Firebase("https://justbakers-285be.firebaseio.com/userData/counts/boxCount");
        counterRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if(currentData.getValue() == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue((Long) currentData.getValue() + 1);
                }
                return Transaction.success(currentData); //we can also abort by calling Transaction.abort()
            }
            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                //This method will be called once with the results of the transaction.
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onRestart() {
        // CONFIRMING THAT CORRECT NUMBER OF PRODUCTS ARE DISPLAYED EVEN ON RESTART
        //bargaincartindex = BargainCartActivity.getBargainCartListSize();
        //cartIndex = CartActivity.getCartListSize();
        invalidateOptionsMenu();
        super.onRestart();
    }

    @Override
    public void onStop() {

        super.onStop();
        cartRef.removeEventListener(cartValueEventListener);
        productRef.removeEventListener(prodValueEventListener);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    /**HANDLING THE ACTION BAR ITEM CLICKS HERE**/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // INFLATE THE MENU; THIS WILL ADD ITEMS TO ACTION BAR IF THEY ARE PRESENT.
        getMenuInflater().inflate(R.menu.user, menu);

        View menu_item_cart = menu.findItem(R.id.action_cart).getActionView();
        cartcounterTV = (TextView) menu_item_cart.findViewById(R.id.cartcounter);
       // bargaincounterTV = (TextView) menu_item_jb.findViewById(R.id.jbcounter);

        /**UPDATING INITIAL THE CART COUNTS AND ONMENUITEMLISTENER FOR MENU_WITTY ITEM**/

        updateCartCount(productAdapter.getCartItemsCount());

        /*new MenuBargainItemListener(menu_item_jb, "Show message") {
            @Override
            public void onClick(View v) {
                Intent Witty_Intent = new Intent(UserActivity.this,BargainCartActivity.class);
                UserActivity.this.startActivity(Witty_Intent);
            }
        };*/

        /**UPDATING INITIAL THE BARGAIN REQUEST COUNTS AND ONMENUITEMLISTENER FOR MENU_CART ITEM**/
        updateBargainCartCount(bargainCartIndex);
        MenuCartItemListener cartItemListener = new MenuCartItemListener(menu_item_cart, "Show message") {
            @Override
            public void onClick(View v) {

                Intent Cart_Intent = new Intent(UserActivity.this, CartActivity.class);
                UserActivity.this.startActivity(Cart_Intent);
            }
        };

        menu_item_cart.setOnClickListener(cartItemListener);
        menu_item_cart.setOnLongClickListener(cartItemListener);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // NAVIGATION BAR ITEM CLICKS ARE HANDLED HERE
        int id = item.getItemId();
        final ArrayList<ProductClass> RefinedProductList = new ArrayList<>();
        LinearLayout llContact = findViewById(R.id.SHIMMERTEXTVIEWS_CONTACT);
        TableRow orderTableHeader = findViewById(R.id.orderTableHeader);

        if (id == R.id.nav_placedorders) {
            llContact.setVisibility(View.GONE);
            productList.setVisibility(View.VISIBLE);


            orderTableHeader.setVisibility(View.VISIBLE);
            setActionBarTitle("Your Orders");

            productList.setAdapter(orderAdapter);

            setFlags(false, true, false);
        } else if (id == R.id.nav_yourcart) {
            llContact.setVisibility(View.GONE);
            productList.setVisibility(View.VISIBLE);


            orderTableHeader.setVisibility(View.GONE);
            setActionBarTitle("Your Cart");


            /**ONCLICKLISTENER ON THE CARDS-PASSING THE DETAILS VIA INTENT TO DESCRIPTION ACTIVITY**/

            productList.setOnItemClickListener((parent, view, position, id1) -> {
                ProductClass CurrentProduct = (ProductClass) productAdapter.getItem(position);
                Intent DescriptionIntent = new Intent(UserActivity.this, DescriptionActivity.class);
                DescriptionIntent.putExtra("ProductDetails",CurrentProduct);
                UserActivity.this.startActivity(DescriptionIntent);
            });

            productList.setAdapter(productAdapter);
            setFlags(true, false, false);
        }  else if (id == R.id.nav_contact) {

            llContact.setVisibility(View.VISIBLE);
            productList.setVisibility(View.GONE);
            orderTableHeader.setVisibility(View.GONE);

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**FUNCTION TO UPDATE CART COUNTS IN ACTION BAR USING FIREBASE**/

    public void firebaseCartCount(Firebase ref){
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateCartCount((int) dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    /**FUNCTION TO UPDATE BARGAIN REQUESTS COUNTS IN ACTION BAR USING FIREBASE**/

    public void firebaseBargainCartCount(Firebase ref){
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateBargainCartCount((int) dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    /**FUNCTION TO UPDATE CART COUNTS IN ACTION BAR IN OFFLINE MODE**/
    public void updateCartCount(final int new_number) {
        //cartIndex = new_number;
        if (cartcounterTV == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (new_number == 0)
                    cartcounterTV.setVisibility(View.INVISIBLE);
                else {
                    cartcounterTV.setVisibility(View.VISIBLE);
                    cartcounterTV.setText(Integer.toString(new_number));
                }
            }
        });
    }

    /**FUNCTION TO UPDATE BARGAIN REQUESTS COUNTS IN ACTION BAR IN OFFLINE MODE**/
    public void updateBargainCartCount(final int new_number) {
        bargainCartIndex = new_number;
        if (bargaincounterTV == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (new_number == 0)
                    bargaincounterTV.setVisibility(View.INVISIBLE);
                else {
                    bargaincounterTV.setVisibility(View.VISIBLE);
                    bargaincounterTV.setText(Integer.toString(new_number));
                }
            }
        });
    }

    /**FUNCTION TO SHOW CUSTOM TOAST MESSAGE**/
    public static void customToastDisplay(Context context, String Message){
        /** CUSTOM TOAST MESSAGE **/
        Toast toast = Toast.makeText(context,"", Toast.LENGTH_SHORT);
        View view = toast.getView();
        view.setBackgroundResource(R.drawable.rounded_square);
        TextView text = (TextView) view.findViewById(android.R.id.message);
        text.setTextColor(context.getResources().getColor(R.color.colorWhite));
        text.setText(Message);
        toast.show();
    }

    public void cartButtonClick(int position, View v, ArrayList<ProductClass> productList , Firebase ref){
        TextView addedInfo = (TextView) v.findViewById(R.id.addedInfo);
        addedInfo.setVisibility(View.VISIBLE);

        //updateCartCount(++cartIndex);
        ProductClass product = productList.get(position);
        // WE HAVE DEFINED NEW VARIABLE FOR CART PRODUCT AS WE ALSO HAVE TO ADD QUANTITY TO CART
        product.setQuantity(1);
        ProductClass CartProduct = new ProductClass(product.getName()
                ,product.getImage()
                ,product.getprice()
                ,product.getDescription()
                ,product.getSeller()
                ,product.getId()
                ,product.getQuantity()
                ,product.getLimit());
        /** PUSHING THE PRODUCT TO THE CART DATABASE **/
        ref.child(CartProduct.getId()).setValue(CartProduct);
        addedInfo.setText("This product is in your cart. Max quantity : " + CartProduct.getLimit());
        //THIS FUNCTION WILL UPDATE THE CART COUNTER NODE IN OUR DATABASE
        updateCartCounterInFirebase();
        info.hoang8f.widget.FButton addCart = (info.hoang8f.widget.FButton) v.findViewById(R.id.Addtocart);
        info.hoang8f.widget.FButton addItem = (info.hoang8f.widget.FButton) v.findViewById(R.id.addbutton);
        info.hoang8f.widget.FButton subItem = (info.hoang8f.widget.FButton) v.findViewById(R.id.subbutton);
        info.hoang8f.widget.FButton quantity = (info.hoang8f.widget.FButton) v.findViewById(R.id.quantitybutton);
        addItem.setVisibility(View.VISIBLE);
        subItem.setVisibility(View.VISIBLE);
        quantity.setVisibility(View.VISIBLE);
        addCart.setVisibility(View.GONE);
        quantity.setText("1");


    }

    public void orderTrackButtonClick(int position, View v, ArrayList<OrderClass> OrderList , Firebase ref){

        RecyclerView order_track_tv = v.findViewById(R.id.recycleView);
        TextView order_details_tv = v.findViewById(R.id.orderDetailTextView);
        int vis = order_track_tv.getVisibility();
        Button cancel_order_button = v.findViewById(R.id.cancel_order_btn);

        if ( vis == View.GONE) {
            vis = View.VISIBLE;

        }
        else {
            vis = View.GONE;


        }
        cancel_order_button.setVisibility(View.GONE);
        order_details_tv.setVisibility(View.GONE);
        order_track_tv.setVisibility(vis);


        String[] trackArr = {"Order\nPlaced", "Order\nConfirmed", "Order\nProcessing", "Order\nDelivered"};

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        order_track_tv.setLayoutManager(layoutManager);


        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int devicewidth = displaymetrics.widthPixels;


        int deviceheight = displaymetrics.heightPixels / 8;

        curOrder = OrderList.get(position);

        RecyclerView.Adapter mAdapter = new OrderDetailsListAdapter(this, curOrder, trackArr);
        order_track_tv.setAdapter(mAdapter);
        order_track_tv.getLayoutParams().width = devicewidth;
        order_track_tv.getLayoutParams().height = deviceheight;
    }

    public void orderDetailsButtonClick(int position, View v, ArrayList<OrderClass> OrderList , Firebase ref){


        TextView order_details_tv = v.findViewById(R.id.orderDetailTextView);
        RecyclerView order_track_tv = v.findViewById(R.id.recycleView);
        TextView order_details_button = v.findViewById(R.id.tv_orderdetails);

        Button cancel_order_button = v.findViewById(R.id.cancel_order_btn);
        int vis = order_details_tv.getVisibility();
        String cart_items = "";

        order_track_tv.setVisibility(View.GONE);
        if ( vis == View.GONE) {
            vis = View.VISIBLE;
            // order_details_button.setCompoundDrawables(null, null, dr,null);
          //  order_details_button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.tripledot_horizontal, 0);

        }
        else {
            vis = View.GONE;
            // Drawable dr = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_expand_more_black_24dp);
          //  order_details_button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.tripledot_horizontal, 0);


        }
        curOrder = OrderList.get(position);
        order_details_tv.setVisibility(vis);

        cancel_order_button.setVisibility(vis);

        for ( ProductClass pc : curOrder.getCart()) {
            cart_items += "Name : " + pc.getName()  + " Quantity : " + pc.getQuantity() + "  Price : " + pc.getprice() + "\n";
        }

        String fulldate =  curOrder.getDate().toString();
        String date = (String) curOrder.getDate().toString().subSequence(0,10);
        String year =  (String) curOrder.getDate().toString().subSequence(fulldate.length()-5,fulldate.length());
        order_details_tv.setText ("Status : " + curOrder.getStatus().toUpperCase() + "\n"
                +    "Order Placed on : " + date + year + "\n"
                +    "Amount : " + curOrder.getReceipt().getTotalamount() + "\n"
                +    "Including GST : 12%" + "\n"
                +     cart_items);


        if(curOrder.getStatus().toUpperCase().equals("CANCELLED"))
            cancel_order_button.setVisibility(View.GONE);
    }

    public void cancelOrderButtonClick(int position, View v, ArrayList<OrderClass> OrderList ){

        curOrder = OrderList.get(position);
        String str = curOrder.getOrdernumber();
        String str1[] = str.split("#");

       android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to Cancel this order.").setPositiveButton (android.R.string.yes, (dialog, whichButton) -> {
            Firebase CustomerChangeOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+userId+"/orders/placed/"+str1[1]);

            CustomerChangeOrderRef.child("status").setValue("cancelled");
            orderAdapter.notifyDataSetChanged();
            TextView order_details_tv = v.findViewById(R.id.orderDetailTextView);
            Button cancel_order_button = v.findViewById(R.id.cancel_order_btn);
            TextView button_order_details = v.findViewById(R.id.tv_orderdetails);
            TextView track_order_button = v.findViewById(R.id.track_order_btn);

            order_details_tv.setVisibility(View.GONE);
            cancel_order_button.setVisibility(View.GONE);
            button_order_details.setText("CANCELLED");
            track_order_button.setEnabled(false);
            track_order_button.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);

        });

        builder.setNegativeButton(android.R.string.no, (dialog, whichButton) -> {});

        builder.show();

    }

    public static  OrderClass getCurOrder () {
        return curOrder;
    }
    public void bargainButtonAlertDialog(Context context, final int position, View v, final ArrayList<ProductClass> ProductList , final Firebase ref){
        final TextView addedInfo = (TextView) v.findViewById(R.id.addedInfo);
        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        builder.setTitle(R.string.BargainQuestion);
        // SET UP THE INPUT
        final EditText input = new EditText(context);
        // Specifying the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextColor(getResources().getColor(R.color.colorPrimary));
        input.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        builder.setView(input);

        // SETUP THE BUTTONS
        builder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProductClass bargainproduct = ProductList.get(position);
                // A CHECK IF THE BARGAIN PRICE REQUESTED IS NOT LESS THAN 40% OR TWICE OF PRODUCT PRICE
                if((Integer.parseInt(input.getText().toString()))< (0.4*bargainproduct.getprice()) || (Integer.parseInt(input.getText().toString()))> (2*bargainproduct.getprice())){
                    customToastDisplay(UserActivity.this,getResources().getString(R.string.bargainError));
                }
                else{
                    BargainProductClass BargainProduct = new BargainProductClass(bargainproduct.getName()
                            ,bargainproduct.getImage()
                            ,bargainproduct.getprice()
                            ,Integer.parseInt(input.getText().toString())
                            ,bargainproduct.getDescription()
                            ,bargainproduct.getSeller()
                            ,0
                            ,bargainproduct.getId()
                            ,""
                            ,0
                            ,1
                            ,System.currentTimeMillis());
                    /** PUSHING THE BARGAINPRODUCT TO THE BARGAINREQUESTS DATABASE **/
                    ref.push().setValue(BargainProduct);
                    //THIS FUNCTION WILL UPDATE THE BARGAIN REQUEST COUNTER NODE IN OUR DATABASE
                    updateBargainCartCounterInFirebase();
                    bargainCartIndex = bargainCartIndex + 1;
                    updateBargainCartCount(bargainCartIndex);
                    //MAKING ADDED INFO TEXT VIEW VISIBLE
                    addedInfo.setVisibility(View.VISIBLE);
                    addedInfo.setText("Bargain Requested. Click 'W' icon on the top for more details");
                }
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void addItemButtonClick(int position, View v, ArrayList<ProductClass> ProductList , Firebase ref){
        info.hoang8f.widget.FButton incrButton = (info.hoang8f.widget.FButton) v.findViewById(R.id.addbutton);
        info.hoang8f.widget.FButton qtyButton = (info.hoang8f.widget.FButton) v.findViewById(R.id.quantitybutton);

        ProductClass Product = ProductList.get(position);
        // WE HAVE DEFINED NEW VARIABLE FOR CART PRODUCT AS WE ALSO HAVE TO ADD QUANTITY TO CART

        ProductClass CartProduct = new ProductClass(Product.getName()
                ,Product.getImage()
                ,Product.getprice()
                ,Product.getDescription()
                ,Product.getSeller()
                ,Product.getId()
                ,Product.getQuantity(), Product.getLimit());


       int qty = Integer.parseInt(qtyButton.getText().toString());

       if((qty+1) <= Product.getLimit()) {
            CartProduct.setQuantity(++qty);
            Product.setQuantity(qty);
            /** PUSHING THE PRODUCT TO THE CART DATABASE **/
            ref.child(CartProduct.getId()).setValue(CartProduct);
            //THIS FUNCTION WILL UPDATE THE CART COUNTER NODE IN OUR DATABASE
            updateCartCounterInFirebase();
            qtyButton.setText(CartProduct.getQuantity().toString());
        }
        else {
            incrButton.setEnabled(false);
            //Custom Toast Display Function defined below - Displays custom toast message
         //   customToastDisplay(UserActivity.this,getResources().getString(R.string.QuantityLimitExceeded));
        }

    }

    public void subItemButtonClick(int position, View v, ArrayList<ProductClass> ProductList , Firebase ref){
        TextView addedInfo = (TextView) v.findViewById(R.id.addedInfo);
        info.hoang8f.widget.FButton qtyButton = (info.hoang8f.widget.FButton) v.findViewById(R.id.quantitybutton);
        //  addedInfo.setVisibility(View.VISIBLE);
        //  addedInfo.setText("Reduced One Item");

        ProductClass Product = ProductList.get(position);
        // WE HAVE DEFINED NEW VARIABLE FOR CART PRODUCT AS WE ALSO HAVE TO ADD QUANTITY TO CART
        ProductClass CartProduct = new ProductClass(Product.getName()
                ,Product.getImage()
                ,Product.getprice()
                ,Product.getDescription()
                ,Product.getSeller()
                ,Product.getId()
                ,Product.getQuantity()
                ,Product.getLimit());

        int qty = Integer.parseInt(qtyButton.getText().toString());

        CartProduct.setQuantity(--qty);
        Product.setQuantity(qty);
        /** PUSHING THE PRODUCT TO THE CART DATABASE **/
        ref.child(CartProduct.getId()).setValue(CartProduct);
        //THIS FUNCTION WILL UPDATE THE CART COUNTER NODE IN OUR DATABASE
        updateCartCounterInFirebase();
        info.hoang8f.widget.FButton addCart = (info.hoang8f.widget.FButton) v.findViewById(R.id.Addtocart);
        info.hoang8f.widget.FButton addItem = (info.hoang8f.widget.FButton) v.findViewById(R.id.addbutton);
        info.hoang8f.widget.FButton subItem = (info.hoang8f.widget.FButton) v.findViewById(R.id.subbutton);
        info.hoang8f.widget.FButton quantity = (info.hoang8f.widget.FButton) v.findViewById(R.id.quantitybutton);

        if( qty == 0) {
            addItem.setVisibility(View.GONE);
            subItem.setVisibility(View.GONE);
            quantity.setVisibility(View.GONE);
            addCart.setVisibility(View.VISIBLE);
        }
        else {
            quantity.setText(CartProduct.getQuantity().toString());
            info.hoang8f.widget.FButton incrButton = (info.hoang8f.widget.FButton) v.findViewById(R.id.addbutton);
            incrButton.setEnabled(true);

        }


    }
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public void setFlags ( boolean pendingOrderFlag, boolean placedOrderFlag, boolean deliveredOrderFlag) {
        this.pendingOrderFlag = pendingOrderFlag;
        this.placedOrderFlag = placedOrderFlag;
        this.deliveredOrderFlag = deliveredOrderFlag;
    }
    // Sort Orderslary by Date
    Comparator<OrderClass> orderClassComparator = new Comparator<OrderClass>() {
        @Override
        public int compare(OrderClass e1, OrderClass e2) {
            if(e1.getDate().getTime() > e2.getDate().getTime()) {
                return -1;
            } else if (e1.getDate().getTime() < e2.getDate().getTime()) {
                return 1;
            } else {
                return 0;
            }
        }
    };
}


