package com.ecom.justbakers.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ecom.justbakers.Adapters.ProductListAdapter;
import com.ecom.justbakers.Adapters.SellerAdapter;
import com.ecom.justbakers.Classes.Product;
import com.ecom.justbakers.Classes.Seller;
import com.ecom.justbakers.LoginActivity;
import com.ecom.justbakers.R;
import com.ecom.justbakers.databinding.FragmentProductsBinding;
import com.ecom.justbakers.room.ProductDatabase;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ProductsFragment extends Fragment {
    private static final String TAG = "ProductsFragment";

    public interface OnCartItemUpdateListener{
        void update(final int itemCount);
    }

    private OnCartItemUpdateListener onCartItemUpdateListener;
    private ProgressDialog progress;
    private ProductListAdapter productAdapter;
    private double databaseVer;

    private ValueEventListener cartValueEventListener, databaseValueEventListener;

    Firebase databaseVerRef = new Firebase("https://justbakers-285be.firebaseio.com/config/productVersion");
    private Firebase productRef = new Firebase("https://justbakers-285be.firebaseio.com/productDisplay");
    private Firebase cartRef = new Firebase("https://justbakers-285be.firebaseio.com/").child("customers");
    private FragmentProductsBinding productsBinding;

    public ProductsFragment() {}

    public static ProductsFragment newInstance() {
        ProductsFragment fragment = new ProductsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof OnCartItemUpdateListener){
            onCartItemUpdateListener = (OnCartItemUpdateListener) context;
        }else{
            throw new RuntimeException("The Activity must implement OnCartItemUpdateListener interface");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        productsBinding = FragmentProductsBinding.inflate(inflater, container, false);
        return productsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        onJustStart(view);

        progress = new ProgressDialog(getActivity());
        progress.setMessage(getResources().getString(R.string.Loading1));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        //progress.show();

        ProductDatabase pd = ProductDatabase.getInstance(getActivity());
        ArrayList<Product> products = new ArrayList<>();
        Map<String, Product> cartProducts = new LinkedHashMap<>();

        //cartProducts.stream().filter(product -> products.get(position).getId().equals(product.getId())).findAny().orElse(null)
        productAdapter = new ProductListAdapter(requireActivity(), products, cartProducts, cartChangedConsumer);

        RecyclerView productsList = productsBinding.productsList;
        productsList.setAdapter(productAdapter);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL, false);
        productsList.setLayoutManager(manager);
        productsList.setItemAnimator(new DefaultItemAnimator());


        ValueEventListener prodValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && !requireActivity().isFinishing()) {
                        List<FileDownloadTask> fileDownloadTasks = new ArrayList<>();
                        int position = -1;
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            ++position;
                            Product post = postSnapshot.getValue(Product.class);
                            Log.d(TAG, post.getName() + " - " + post.getDescription() + "-" + post.getId());
                            Product currentProduct = new Product(post.getName()
                                    , post.getImage()
                                    , post.getPrice()
                                    , post.getDiscount()
                                    , post.getDescription()
                                    , post.getSeller()
                                    , post.getId()
                                    , post.getCategory()
                                    , post.getLimit());

                            if (!pd.daoAccess().isProductExists(currentProduct.getId())) {
                                pd.daoAccess().insert(currentProduct);
                            } else {
                                Product pc = pd.daoAccess().getProductBlocking(currentProduct.getId());
                                currentProduct.setRowId(pc.getRowId());
                                pd.daoAccess().update(currentProduct);
                            }

                            String str = "justbakers";
                            File mydir = requireActivity().getDir(str, Context.MODE_PRIVATE);
                            String firebaseStr = currentProduct.getImage();
                            String localStr = firebaseStr.replace("/", "_");
                            File localFile = new File(mydir, localStr);

                            if (localFile.exists()) {
                                //noinspection ResultOfMethodCallIgnored
                                localFile.delete();
                            }
                            FileDownloadTask task = saveImgToInternalStorage(firebaseStr, localFile);
                            final int pos = position;
                            task.addOnSuccessListener(result -> {
                                if(null != getView() && getViewLifecycleOwner().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                                    productAdapter.notifyItemChanged(pos);
                                    progress.dismiss();
                                }
                            });
                            fileDownloadTasks.add(task);
                        }

                        try {
                            Tasks.whenAllComplete(fileDownloadTasks).addOnCompleteListener(task -> {
                                if(null != getView() && getViewLifecycleOwner().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                                    productAdapter.notifyDataSetChanged();
                                    updateDatabaseVersion((float) databaseVer);
                                    progress.dismiss();
                                }
                            });
                        }catch (NullPointerException ignore) {}
                    }
                }finally{
                    productRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "The read failed: " + firebaseError.getMessage());
            }
        };

        pd.daoAccess().fetchAll().observe(getViewLifecycleOwner(), products_ -> {
            products.clear();
            products.addAll(products_);
            Collections.sort(products, productClassComparator);

            productAdapter.setProductList(products);
            if(-1 != productAdapter.getLastClickedItem() && productAdapter.getLastClickedItem() < products.size()) {
                productsList.scrollToPosition(productAdapter.getLastClickedItem());
            }
            //progress.dismiss();
        });

        databaseValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double dbVer = (Double) dataSnapshot.getValue();
                if(null != dbVer) {
                    databaseVer = (double) dbVer;

                    if (isDatabaseVersionChanged((float) databaseVer)) {
                        progress.show();
                        productRef.addValueEventListener(prodValueEventListener);
                    }
                }

            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "The read failed: " + firebaseError.getMessage());
            }
        };
        databaseVerRef.addValueEventListener (databaseValueEventListener);

        cartValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && !requireActivity().isFinishing() ) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Product post = postSnapshot.getValue(Product.class);
                        Log.d(TAG, post.getName() + " - " + post.getDescription() + "-" + post.getId());
                        Product currentProduct = new Product(post.getName()
                                , post.getImage()
                                , post.getPrice()
                                , post.getDiscount()
                                , post.getDescription()
                                , post.getSeller()
                                , post.getId()
                                , post.getCategory(), post.getQuantity(), post.getLimit());
                        cartProducts.put(currentProduct.getId(), currentProduct);
                    }

                    onCartItemUpdateListener.update(cartProducts.size());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "The read failed: " + firebaseError.getMessage());
            }
        };

        cartRef.addValueEventListener(cartValueEventListener);
    }

    private void onJustStart(View view) {
        Firebase.setAndroidContext(requireActivity());
        productRef = new Firebase("https://justbakers-285be.firebaseio.com/productDisplayVer20");
        RecyclerView headerRecyclerView = view.findViewById(R.id.headerRecycleView);
        List<Seller> sellersList = new ArrayList<>();
        Seller s1 = new Seller("Harneet", "1", "2");
        Seller s2 = new Seller("Priyanka", "1", "2");
        Seller s3 = new Seller("Partywise", "1", "2");

        sellersList.add(s1);
        sellersList.add(s2);
        sellersList.add(s3);
        SellerAdapter sellerAdapter = new SellerAdapter(sellersList);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL, false);
        headerRecyclerView.setLayoutManager(manager);
        headerRecyclerView.setAdapter(sellerAdapter);
        headerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        String userId = LoginActivity.getLoggedInUser(requireActivity(), true);

        if(!LoginActivity.isUserLoggedIn(requireActivity())) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            this.startActivity(intent);
        } else {
            cartRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+ userId +"/orders/pending").child("cart");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("SelectedProductPosition", null != productAdapter ? productAdapter.getLastClickedItem() : -1 );
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(null != savedInstanceState){
            int lastCheckedItem = savedInstanceState.getInt("SelectedProductPosition");
            if(-1 != lastCheckedItem){
                RecyclerView productsList = requireView().findViewById(R.id.productsList);
                if(lastCheckedItem < productAdapter.getItemCount()) {
                    productsList.scrollToPosition(lastCheckedItem);
                }
                productAdapter.setLastClickedItem(lastCheckedItem);
            }
        }
    }

    private boolean isDatabaseVersionChanged(float databaseVer) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        return sharedPref.getFloat(getString(R.string.database_version_key), 0.0f) != databaseVer;
    }

    private void updateDatabaseVersion ( double databaseVer) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        sharedPref.edit()
                .putFloat(getString(R.string.database_version_key), (float) databaseVer)
                .apply();
    }

    @Override
    public void onResume() {
        firebaseCartCount(cartRef);
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        cartRef.removeEventListener(cartValueEventListener);
        databaseVerRef.removeEventListener(databaseValueEventListener);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void firebaseCartCount(Firebase ref){
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onCartItemUpdateListener.update((int) dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) { }
        });
    }

    Consumer<Pair<Action, Product>> cartChangedConsumer = (data)-> {
        Action action = data.first;
        Product product = data.second;

        if(Action.DELETE == action){
            cartRef.child(product.getId()).removeValue();
        }else{
            cartRef.child(product.getId()).setValue(product);
        }
    };

    private final Comparator<Product> productClassComparator = (e1, e2) -> Integer.compare(e1.getId().compareTo(e2.getId()), 0);

    private FileDownloadTask saveImgToInternalStorage(String firebasePath, File localPath){
        FirebaseStorage firebaseStorage  = FirebaseStorage.getInstance();
        StorageReference fileRef = firebaseStorage.getReference().child(firebasePath);
        return fileRef.getFile(localPath);
    }
}
