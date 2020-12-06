package com.ecom.justbakers.seller_fragments;

/**
 * Created by brainbreaker.
 */

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.ecom.justbakers.Classes.Product;
import com.ecom.justbakers.SellerActivity;
import com.firebase.client.Firebase;

import com.ecom.justbakers.R;

import androidx.fragment.app.Fragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddProductFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    String Productname;
    String Productdescription;
    Integer Productprice;
    String ProductimageURL;
    String Productcategory;
    String Productseller;
    Integer Productlimit;
    public AddProductFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static AddProductFragment newInstance() {
        AddProductFragment fragment = new AddProductFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_product_fragment, container, false);
        ((SellerActivity) getActivity())
                .setActionBarTitle("ADD NEW PRODUCT");

        Firebase.setAndroidContext(getActivity());
        //FINDING THE VIEWS
        final AutoCompleteTextView ProductName = (AutoCompleteTextView) rootView.findViewById(R.id.AddProductName);
        final AutoCompleteTextView ProductDescription = (AutoCompleteTextView) rootView.findViewById(R.id.AddProductDescription);
        final AutoCompleteTextView ProductImageURL = (AutoCompleteTextView) rootView.findViewById(R.id.AddProductURL);
        final AutoCompleteTextView ProductPrice = (AutoCompleteTextView) rootView.findViewById(R.id.AddProductPrice);
        final AutoCompleteTextView ProductSeller = (AutoCompleteTextView) rootView.findViewById(R.id.AddProductSeller);
        final AutoCompleteTextView ProductCategory = (AutoCompleteTextView) rootView.findViewById(R.id.AddProductCategory);
        Button AddProductButton = (Button) rootView.findViewById(R.id.AddProductButton);
        Button ResetButton = (Button) rootView.findViewById(R.id.ResetButton);

        //CODE FOR ADD PRODUCT BUTTON
        Firebase ProductDisplayRef = new Firebase("https://justbakers-285be.firebaseio.com/productDisplay/");
        final Firebase ProductDisplayRefPush = ProductDisplayRef.push();
        AddProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Productname = ProductName.getText().toString();
                Productdescription = ProductDescription.getText().toString();
                Productprice =Integer.parseInt(ProductPrice.getText().toString());
                Productseller = ProductSeller.getText().toString();
                Productcategory = ProductCategory.getText().toString();
                ProductimageURL = ProductImageURL.getText().toString();
                Product Product = new Product(Productname,ProductimageURL,Productprice, 0.0, Productdescription,Productseller,ProductDisplayRefPush.getKey(),Productcategory,
                                                        Productlimit);
                ProductDisplayRefPush.setValue(Product);

                /** CUSTOM TOAST MESSAGE **/
                Toast toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.rounded_square);
            //    TextView text = (TextView) view.findViewById(android.R.id.message);
           //     text.setText(" PRODUCT ADDED SUCCESSFULLY! ");
            //    text.setTextColor(getResources().getColor(R.color.colorWhite));
            //    toast.show();
            }
        });

        //CODE FOR RESET BUTTON
        ResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProductName.setText("");
                ProductDescription.setText("");
                ProductPrice.setText("");
                ProductSeller.setText("");
                ProductCategory.setText("");
                ProductImageURL.setText("");
            }
        });

        return rootView;
    }
}