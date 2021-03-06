package com.ecom.justbakers.seller_fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.ecom.justbakers.Adapters.SellerProductListAdapter;
import com.ecom.justbakers.Classes.Product;
import com.ecom.justbakers.R;
import com.ecom.justbakers.SellerActivity;
import com.ecom.justbakers.SellerDetailsActivity;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;

import androidx.fragment.app.Fragment;

public class ProductFragment extends Fragment {
    private final static String TAG = "ProductFragment";
    private static ArrayList<String> ProductName;
    private static ArrayList<String> ProductURL;
    private static ArrayList<Integer> ProductPrice;
    private static ArrayList<Double> ProductDiscount;
    private static ArrayList<String> ProductDescription;
    private static ArrayList<String> ProductSeller;
    private static ArrayList<String> ProductCategory;
    private static ArrayList<String> ProductId;
    private static ArrayList<Integer> ProductLimit;
    private ProgressDialog progress;
    public ProductFragment() {
    }

    public static ProductFragment newInstance() {
        ProductFragment fragment = new ProductFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.seller_product_fragment, container, false);
        ((SellerActivity) getActivity())
                .setActionBarTitle("YOUR PRODUCTS");
        Firebase.setAndroidContext(getActivity());
        /** INITIALISATIONS **/
        ProductName = new ArrayList<>();
        ProductURL = new ArrayList<>();
        ProductPrice = new ArrayList<>();
        ProductDescription = new ArrayList<>();
        ProductCategory = new ArrayList<>();
        ProductSeller = new ArrayList<>();
        ProductId = new ArrayList<>();
        final ListView ProductListView = (ListView) rootView.findViewById(R.id.SellerProductList);
        final TextView NumberOfBargains = (TextView) rootView.findViewById(R.id.numberofbargain);

        /** CREATING AND STARTING THE PROGRESS BAR **/
        progress=new ProgressDialog(getActivity());
        progress.setMessage(getResources().getString(R.string.Loading2));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();
        /** MAKING A REF TO PRODUCT DISPLAY URL OF FIREBASE **/
        Firebase ProductDisplayRef = new Firebase("https://justbakers-285be.firebaseio.com/productDisplay");


        ProductDisplayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Product post = postSnapshot.getValue(Product.class);
                    System.out.println(post.getName() + " - " + post.getDescription()+ "-" + post.getPrice() + "-"+ post.getId());
                    ProductName.add(post.getName());
                    ProductURL.add(post.getImage());
                    ProductPrice.add(post.getPrice());
                    ProductDescription.add(post.getDescription());
                    ProductSeller.add(post.getSeller());
                    ProductCategory.add(post.getCategory());
                    ProductId.add(post.getId());
                    System.out.println("INTENsjnsjsdnjs"+ProductId);
                }
                SellerProductListAdapter Adapter = new SellerProductListAdapter(getActivity(),ProductName,ProductURL,ProductPrice,
                        getActivity().getWindowManager().getDefaultDisplay().getWidth(),
                        new SellerProductListAdapter.ButtonClickListener() {
                            @Override
                            public void onButtonClick(int position) {
                                System.out.println("INTENT PROddsfd"+position +" "+ProductId.get(position));
                                Product IntentProduct = new Product(ProductName.get(position)
                                        ,ProductURL.get(position)
                                        ,ProductPrice.get(position)
                                        ,ProductDiscount.get(position)
                                        ,ProductDescription.get(position)
                                        ,ProductSeller.get(position)
                                        ,ProductId.get(position)
                                        ,ProductCategory.get(position)
                                        ,ProductLimit.get(position));

                                Log.i(TAG, "INTENT PRODUCT PRODUCT ID"+ IntentProduct.getId());
                                Intent sellerDetailIntent = new Intent(getActivity(), SellerDetailsActivity.class);
                                sellerDetailIntent.putExtra("IntentProduct", (Serializable) IntentProduct);
                                getActivity().startActivity(sellerDetailIntent);
                            }
                        });
                Adapter.notifyDataSetChanged();
                ProductListView.setAdapter(Adapter);
                progress.setProgress(100);
                progress.dismiss();
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        Firebase bargaincounterref = new Firebase("https://justbakers-285be.firebaseio.com/userData/counts/boxCount");
        bargaincounterref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( dataSnapshot.getValue() != null)
                  NumberOfBargains.setText("You have "+ dataSnapshot.getValue().toString()+ " pending requests on your products");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println(firebaseError);
            }
        });
        return rootView;
    }
}