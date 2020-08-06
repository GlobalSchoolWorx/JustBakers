package com.ecom.justbakers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecom.justbakers.Adapters.CustomCartListAdapter;
import com.ecom.justbakers.Adapters.CustomProductListAdapter;
import com.ecom.justbakers.Classes.ProductClass;
import com.ecom.justbakers.gpay.TempCheckoutActivity;
import com.ecom.justbakers.orders.InfoClass;
import com.ecom.justbakers.orders.OrderClass;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class CartActivity extends AppCompatActivity {
    private static ArrayList<ProductClass> CartProductList;
    private ArrayList<String> ProductID;
    CustomCartListAdapter adapter;
    static OrderClass.OrderReceiptClass receipt;
    private Integer TotalPrice = 0;
    ValueEventListener cart_vel;
    Firebase cartRef;
    Firebase addressRef;
    private ListView cartlistView;
    private TextView addressView;
    private TextView contactView;
    private ProgressDialog progress;
    private info.hoang8f.widget.FButton CheckoutButton;
    private info.hoang8f.widget.FButton addrChgButton;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Firebase.setAndroidContext(this);
        /** ENABLING THE HOME BUTTON ON ACTION BAR **/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** FINDING THE RELEVANT ELEMENTS **/
        cartlistView = (ListView) findViewById(R.id.cartlistView);
        addressView = (TextView) findViewById(R.id.tvAddressView);
        contactView = (TextView) findViewById(R.id.tvPhoneView);
        CheckoutButton = (info.hoang8f.widget.FButton) findViewById(R.id.Checkoutbutton);
        addrChgButton = (info.hoang8f.widget.FButton) findViewById(R.id.chgAddrButton);
        /** INITIALISATION **/
        CartProductList = new ArrayList<>();
        ProductID = new ArrayList<>();

        /** PROGRESS BAR CODE **/
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage("LOADING CART...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();


    }

    @Override
    public void onStart() {
        /** MAKING A REFERENCE TO THE CART URL IN FIREBASE **/
        super.onStart();
        String userId = LoginActivity.getDefaults("UserID", this);
        cartRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userId + "/orders/pending/cart");
        addressRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userId + "/info");

        ValueEventListener addr_vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                InfoClass ic = (InfoClass) dataSnapshot.getValue(InfoClass.class);

                addressView.setText("\bDeliver To\b " + "\n\n"+ ic.getName() + "\n" + ic.getFlatNumber() + "\n" + ic.getSociety() + "\n" + ic.getArea());
                contactView.setText("Contact Number\n\n" + ic.getPhoneNumber());
                ///Set Address Details
            }


            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };

        addressRef.addValueEventListener(addr_vel);

        cart_vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView tv_amount = (TextView) findViewById(R.id.tv_amount);
                CartProductList.clear();
                TotalPrice = 0;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    System.out.println("POST SNAPSHOT IS " + postSnapshot);

                    /** RETRIEVING DETAILS OF EACH PRODUCT IN THE FOR LOOP, ADDING THEM TO LIST AND UPDATING THE TOTAL PRICE **/
                    ProductClass CartProduct = postSnapshot.getValue(ProductClass.class);
                    ProductClass product = new ProductClass(CartProduct.getName()
                            , CartProduct.getImage()
                            , CartProduct.getPrice()
                            , CartProduct.getDescription()
                            , CartProduct.getSeller()
                            , CartProduct.getId()
                            , CartProduct.getQuantity()
                            , CartProduct.getLimit());
                    TotalPrice = TotalPrice + (CartProduct.getPrice() * CartProduct.getQuantity());
                    CartProductList.add(product);
                    ProductID.add(product.getId());
                }
                /** SETTING THE ADAPTER IF CARTPRODUCTLIST IS NOT NULL **/
                if (CartProductList.isEmpty()) {
                    progress.dismiss();
                    /** CUSTOM TOAST MESSAGE **/
                    Toast toast = Toast.makeText(CartActivity.this, "", Toast.LENGTH_SHORT);
                    View view = toast.getView();
                    view.setBackgroundResource(R.drawable.rounded_square);
                    TextView text = (TextView) view.findViewById(android.R.id.message);

                    text.setText(" YOU GOT TO PUT SOMETHING IN THE CART! ");
                    text.setTextColor(getResources().getColor(R.color.colorWhite));
                    toast.show();
                    Intent intent = new Intent(CartActivity.this, UserActivity.class);
                    CartActivity.this.startActivity(intent);
                } else {
                    adapter = new CustomCartListAdapter(CartProductList, getLayoutInflater(), CartActivity.this, new CustomProductListAdapter.ButtonClickListener() {

                        /**
                         * CODE FOR DELETE BUTTON ON EACH ITEM- DELETING THE PRODUCT FROM FIREBASE AND RELOADING THE ACTIVITY
                         **/
                        @Override
                        public void onButtonClick(int position, View v) {
                            deleteQuantity(position, v);
                        }
                    }
                            , new CustomProductListAdapter.ButtonClickListener() {
                        /*** For Increment Quantity ***/
                        @Override
                        public void onButtonClick(int position, View v) {
                            incrementQuantity(position, v);
                        }
                    }
                            , new CustomProductListAdapter.ButtonClickListener() {
                        /*** For Decrement Quantity ***/
                        @Override
                        public void onButtonClick(int position, View v) {
                            decrementQuantity(position, v);
                        }
                    });
                    /** SETTING THE ADAPTER IN THE LIST VIEW **/
                    cartlistView.setAdapter(adapter);
                    String str;
                    if(TotalPrice < 300 ) {
                        str = "\nDelivery Charges : 30/-   " + "(For Orders less than 300/-)";
                        TotalPrice = TotalPrice + 30;
                    } else {
                        str = "\nDelivery Charges : 0/-   " + "\n(Free delivery for Orders above 300/-)";
                    }
                    tv_amount.setText(str + "\nTotal Amount is : " + TotalPrice.toString()+ "/-\n");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        };
        cartRef.addValueEventListener(cart_vel);

        progress.dismiss();


        /**THIS IS THE CODE FOR OPENING UP OF DESCRIPTION ACTIVITY WHEN USER CLICKS THE ITEM IN THE CART
         cartlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent DescriptionIntent = new Intent(CartActivity.this, DescriptionActivity.class);
        DescriptionIntent.putExtra("ProductDetails", CartProductList.get(position));
        CartActivity.this.startActivity(DescriptionIntent);
        }
        });
         *           **/


        /** SETTING ONCLICKLISTENER ON THE CHECKOUT BUTTON **/
        CheckoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*** CUSTOM TOAST MESSAGE***/
                receipt = new OrderClass.OrderReceiptClass(TotalPrice);
                /*
                Toast toast = Toast.makeText(CartActivity.this, "", Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.rounded_square);
                TextView text = (TextView) view.findViewById(android.R.id.message);
                text.setTextColor(getResources().getColor(R.color.colorWhite));
                text.setText(" TOTAL AMOUNT PAYABLE IS Rs " + TotalPrice + " ");
                toast.show();
                */
                cartRef.removeEventListener(cart_vel);
                startActivity(new Intent(getApplicationContext(), OrderConfirmationActivity.class));
            }
        });

        addrChgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TempCheckoutActivity.class);
                intent.putExtra("CONTEXT", "UPDATE");
                startActivity(intent);
            }
        });

    }

    void deleteQuantity (int position, View v) {
    String userId = LoginActivity.getDefaults("UserID", getApplicationContext());
    /**
     * MAKING A REFERENCE TO DELETE THE PARTICULAR PRODUCT-ID
     **/
    Firebase deleteref = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userId + "/orders/pending/cart/"
            + ProductID.get(position));
                                deleteref.removeValue();
                                Snackbar.make(cartlistView,

    getResources().

    getString(R.string.Deleted_Cart),Snackbar.LENGTH_LONG)
            .

    setAction("Action",null).

    show();
    adapter.notifyDataSetChanged();
    //RELOAD THE ACTIVITY FOR ANY FUTURE CHANGES
    Intent intent = getIntent();

    finish();

    startActivity(intent);

    }

    void incrementQuantity ( int position, View v) {
        TextView productQuantity = (TextView) v.findViewById(R.id.productquantity);
        TextView incrQuantity = (TextView) v.findViewById(R.id.incr_quantity);

        String str = productQuantity.getText().toString();
        int qty = Integer.parseInt(str);

        productQuantity.setText (String.valueOf(qty));

        ProductClass Product = CartProductList.get(position);
        // WE HAVE DEFINED NEW VARIABLE FOR CART PRODUCT AS WE ALSO HAVE TO ADD QUANTITY TO CART
        ProductClass CartProduct = new ProductClass(Product.getName()
                ,Product.getImage()
                ,Product.getPrice()
                ,Product.getDescription()
                ,Product.getSeller()
                ,Product.getId()
                ,Product.getQuantity(), Product.getLimit());

        if((qty+1) <= Product.getLimit()) {
            CartProduct.setQuantity(++qty);
            Product.setQuantity(qty);
            /** PUSHING THE PRODUCT TO THE CART DATABASE **/
            cartRef.child(CartProduct.getId()).setValue(CartProduct);
            //THIS FUNCTION WILL UPDATE THE CART COUNTER NODE IN OUR DATABASE
       //     UserActivity.UpdateCartCounterInFirebase();
        } else {
        //Custom Toast Display Function defined below - Displays custom toast message
        // customToastDisplay(CartActivity.this,getResources().getString(R.string.QuantityLimitExceeded));
            incrQuantity.setEnabled(false);
    }

    }

    void decrementQuantity ( int position, View v) {

        TextView productQuantity = (TextView) v.findViewById(R.id.productquantity);
        TextView incrQuantity = (TextView) v.findViewById(R.id.incr_quantity);

        String str = productQuantity.getText().toString();
        int qty = Integer.parseInt(str);
        if ( qty > 1) {
            ProductClass Product = CartProductList.get(position);
            // WE HAVE DEFINED NEW VARIABLE FOR CART PRODUCT AS WE ALSO HAVE TO ADD QUANTITY TO CART
            ProductClass CartProduct = new ProductClass(Product.getName()
                    ,Product.getImage()
                    ,Product.getPrice()
                    ,Product.getDescription()
                    ,Product.getSeller()
                    ,Product.getId()
                    ,Product.getQuantity(), Product.getLimit());
            qty--;
            CartProduct.setQuantity(qty);
            Product.setQuantity(qty);
            /** PUSHING THE PRODUCT TO THE CART DATABASE **/
            cartRef.child(CartProduct.getId()).setValue(CartProduct);
            incrQuantity.setEnabled(true);
        }
        else
            deleteQuantity(position, v);

        productQuantity.setText (String.valueOf(qty));
    }

    /** FUNCTION TO GET REALTIME CART SIZE IN OFFLINE MODE **/
    public static int getCartListSize(){
        try {
            if (CartProductList.size() != 0) {
                return CartProductList.size();
            }
            else {
                return 0;
            }
        } catch (NullPointerException e) {
            return 0;
        }
    }

    /** FUNCTION TO GET REALTIME CART  **/
    public static ArrayList<ProductClass> getCurrentCartList(){
        try {
            if (CartProductList.size() != 0) {
                return CartProductList;
            }
            else {
                return null;
            }
        } catch (NullPointerException e) {
            return null;
        }
    }

    /** WHEN CLICKED ON BACK BUTTON **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // BACK CLICKED. GO TO HOME.
                Intent intent = new Intent(this, UserActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                //FINISH THE CURRENT ACTIVITY
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop () {

        super.onStop();
        cartRef.removeEventListener(cart_vel);
    }

    public static OrderClass.OrderReceiptClass getReceipt() {
        return receipt;
    }
}
