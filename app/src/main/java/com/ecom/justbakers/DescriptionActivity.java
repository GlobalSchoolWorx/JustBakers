package com.ecom.justbakers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecom.justbakers.Classes.ProductClass;
import com.squareup.picasso.Picasso;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class DescriptionActivity extends AppCompatActivity {
    ProductClass ProductDetails;
    private int position;
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);
        /** ENABLING THE HOME BUTTON ON ACTION BAR **/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /**GETTING THE PRODUCT DETAILS FROM THE INTENT**/
        ProductDetails = (ProductClass) getIntent().getSerializableExtra("ProductDetails");
        position =  getIntent().getIntExtra("SelectedProductPosition", 0);
        /**FINDING THE VIEWS FROM THE LAYOUT OF THIS ACTIVITY**/
        ImageView PImage = (ImageView) findViewById(R.id.ProductImageView);
        TextView PName = (TextView) findViewById(R.id.ProductName);
        TextView PPrice = (TextView) findViewById(R.id.ProductPrice);
        TextView PDescription = (TextView) findViewById(R.id.ProductDescription);
        TextView PSeller = (TextView) findViewById(R.id.ProductSeller);
        /*

        DISABLED  CHAT BUTTON BARGAIN BUTTON ADD TO CART BUTTON --- GSW 14 MAY 2020
        info.hoang8f.widget.FButton AddTocart = (info.hoang8f.widget.FButton) findViewById(R.id.AddToCart);
        info.hoang8f.widget.FButton BargainButton = (info.hoang8f.widget.FButton) findViewById(R.id.BargainButton);
        */
        // SETTING THE DETAILS IN THE VIEWS
        Picasso.with(DescriptionActivity.this).setIndicatorsEnabled(true);  //only for debug tests

        if ( ProductDetails != null ) {

            String str = "justbakers" ;
            File mydir =     getApplicationContext().getDir(str, Context.MODE_PRIVATE);
            String firebaseStr = ProductDetails.getImage();
            String localStr = firebaseStr.replace("/", "_");
            File localFile = new File(mydir, localStr);
            Picasso.with(DescriptionActivity.this)
                    .load(localFile)
                    .placeholder(R.drawable.loader)
                    .error(R.drawable.loader)
                    .into(PImage);
            PName.setText(ProductDetails.getName());
            PPrice.setText("Rs. " + ProductDetails.getPrice());
            PDescription.setText(ProductDetails.getDescription());
            PSeller.setText("SOLD BY: " + ProductDetails.getSeller());
        }
        /*
        // Setting Click Listener on Start Chat Button
        final Button ChatButton = (Button) findViewById(R.id.StartChatButton);
        ChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ChatIntent = new Intent(DescriptionActivity.this, ChatActivity.class);
                ChatIntent.putExtra("ProductName", ProductDetails.getName());
                ChatIntent.putExtra("SellerName", ProductDetails.getSeller());
                ChatIntent.putExtra("ProductID",ProductDetails.getId());
                DescriptionActivity.this.startActivity(ChatIntent);
            }
        });

        AddTocart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ** MAKING A REFERENCE TO CART URL IN FIREBASE FOR THE VALUES TO BE PUSHED **

                final Firebase cartRef = new Firebase("https://justbakers-285be.firebaseio.com/")
                        .child("Carts");
                ** PUSHING THE PRODUCT TO THE CART DATABASE **
                cartRef.child(ProductDetails.getId()).setValue(ProductDetails);

                ** CUSTOM TOAST MESSAGE **
                Toast toast = Toast.makeText(DescriptionActivity.this,"", Toast.LENGTH_SHORT);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.rounded_square);
                TextView text = (TextView) view.findViewById(android.R.id.message);
                text.setTextColor(getResources().getColor(R.color.colorWhite));
                text.setText(getResources().getString(R.string.SuccessfulCart));
                toast.show();
                ** SNACKBAR **
                Snackbar.make(v,getResources().getString(R.string.SNACKBAR_CART),Snackbar.LENGTH_LONG).show();
            }
        });

        BargainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ** MAKING A REFERENCE TO BARGAIN REQUESTS CART **

                final Firebase bargaincartRef = new Firebase("https://justbakers-285be.firebaseio.com/")
                        .child("BargainCarts");
                AlertDialog.Builder builder = new AlertDialog.Builder(DescriptionActivity.this);
                builder.setTitle(R.string.BargainQuestion);
                // SET UP THE INPUT
                final EditText input = new EditText(DescriptionActivity.this);
                // Specifying the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setTextColor(getResources().getColor(R.color.colorPrimary));
                input.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                builder.setView(input);

                // SETUP THE BUTTONS
                builder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProductClass bargainproduct = ProductDetails;
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
                                , System.currentTimeMillis());
                        ** PUSHING THE BARGAINPRODUCT TO THE BARGAINREQUESTS DATABASE **
                        bargaincartRef.push().setValue(BargainProduct);
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
        });

        */
    }

    /** WHEN CLICKED ON BACK BUTTON **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // BACK CLICKED. GO TO HOME.
                Intent intent = new Intent(this, UserActivity.class);
                intent.putExtra("SelectedProductPosition", position);
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
    public void onBackPressed() {
        // BACK CLICKED. GO TO HOME.
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("SelectedProductPosition", position);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        //FINISH THE CURRENT ACTIVITY
        this.finish();
    }
}
