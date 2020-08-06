package com.ecom.justbakers.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecom.justbakers.Classes.ProductClass;
import com.ecom.justbakers.DescriptionActivity;
import com.ecom.justbakers.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;

import static com.ecom.justbakers.sms_verify.PhoneAuthActivity.md5;

/**
 * Created by brainbreaker. ADAPTER FOR SHOWING PRODUCT LIST IN USER ACTIVITY
 */
public class CustomProductListAdapter extends BaseAdapter {

    private Context mContext;
    private ButtonClickListener mCartClickListener = null;
    private ButtonClickListener mBargainClickListener = null;
    private ButtonClickListener mAddItemClickListener = null;
    private ButtonClickListener mSubItemClickListener  = null;
    private ArrayList<ProductClass> productList;
    private ArrayList<ProductClass> cartProductList;
    private ProductClass curProduct;

    int screenWidth;
    ViewHolder viewHolder;
    public CustomProductListAdapter(Context c, ArrayList<ProductClass> productList, ArrayList<ProductClass> cartProductList,
                                    int screenWidth, ButtonClickListener cartListener,
                                    ButtonClickListener bargainListener, ButtonClickListener addItemListener, ButtonClickListener subItemListener) {
        mContext = c;
        this.productList = productList;
        this.cartProductList = cartProductList;
        this.screenWidth = screenWidth;
        mCartClickListener = cartListener;
        mBargainClickListener = bargainListener;
        mAddItemClickListener = addItemListener;
        mSubItemClickListener = subItemListener;


    }

    static class ViewHolder {
        ImageView ProductImage;
        TextView ProductName;
        TextView AddedInfo;
        TextView ProductPrice;
        info.hoang8f.widget.FButton addToCart;
        info.hoang8f.widget.FButton bargain;
        info.hoang8f.widget.FButton addItem;
        info.hoang8f.widget.FButton subItem;
        info.hoang8f.widget.FButton quantity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.product_list_item_layout, parent, false);

        }
        curProduct = productList.get(position);

        viewHolder = new ViewHolder();
        viewHolder.ProductName = (TextView) convertView.findViewById(R.id.productname);
        viewHolder.ProductPrice = (TextView) convertView.findViewById(R.id.productprice);
        viewHolder.ProductImage = (ImageView) convertView.findViewById(R.id.productimageview);
        viewHolder.AddedInfo = (TextView)convertView.findViewById(R.id.addedInfo);
        viewHolder.AddedInfo.setVisibility(View.GONE);
        viewHolder.addToCart = (info.hoang8f.widget.FButton) convertView.findViewById(R.id.Addtocart);
        viewHolder.bargain = (info.hoang8f.widget.FButton) convertView.findViewById(R.id.bargainbutton);
        viewHolder.addItem = (info.hoang8f.widget.FButton) convertView.findViewById(R.id.addbutton);
        viewHolder.subItem = (info.hoang8f.widget.FButton) convertView.findViewById(R.id.subbutton);
        viewHolder.quantity = (info.hoang8f.widget.FButton) convertView.findViewById(R.id.quantitybutton);

        ProductClass cartProduct = null;

        if ( cartProductList != null) {
        for (ProductClass cprd : cartProductList) {
            if (cprd.getId().equals(curProduct.getId())) {
                cartProduct = cprd;
                break;
            }
        }
        }

        if ( (cartProduct != null) && (cartProduct.getQuantity() != null) && cartProduct.getQuantity() > 0) {
            viewHolder.addToCart.setVisibility(View.GONE);
            viewHolder.bargain.setVisibility(View.GONE);
            viewHolder.quantity.setVisibility(View.VISIBLE);
            viewHolder.addItem.setVisibility(View.VISIBLE);
            viewHolder.subItem.setVisibility(View.VISIBLE);
            viewHolder.quantity.setText(cartProduct.getQuantity().toString());
        } else {
            viewHolder.bargain.setVisibility(View.GONE);
            viewHolder.quantity.setVisibility(View.GONE);
            viewHolder.addItem.setVisibility(View.GONE);
            viewHolder.subItem.setVisibility(View.GONE);

            viewHolder.addToCart.setVisibility(View.VISIBLE);
        }
        viewHolder.ProductImage.setTag(position);
        viewHolder.addToCart.setTag(position);
        viewHolder.bargain.setTag(position);
        viewHolder.addItem.setTag(position);
        viewHolder.subItem.setTag(position);

        final View finalConvertView = convertView;
        viewHolder.addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCartClickListener != null)
                    mCartClickListener.onButtonClick((Integer) v.getTag(), finalConvertView);

            }
        });

        viewHolder.addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAddItemClickListener != null)
                    mAddItemClickListener.onButtonClick((Integer) v.getTag(), finalConvertView);
            }
        });

        viewHolder.subItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSubItemClickListener != null)
                    mSubItemClickListener.onButtonClick((Integer) v.getTag(), finalConvertView);

            }
        });

        viewHolder.ProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent DescriptionIntent = new Intent(mContext, DescriptionActivity.class);
                ProductClass cProduct = productList.get((Integer)v.getTag());
                DescriptionIntent.putExtra("ProductDetails",cProduct);
                mContext.startActivity(DescriptionIntent);

            }
        });
/*
                viewHolder.bargain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mBargainClickListener != null)
                            mBargainClickListener.onButtonClick((Integer) v.getTag(), finalConvertView);
                    }
                });
*/
        viewHolder.ProductImage.getLayoutParams().width = screenWidth;
        viewHolder.ProductImage.getLayoutParams().height = Math.round(screenWidth / 2);
        viewHolder.ProductImage.requestLayout();

        viewHolder.ProductName.setText(curProduct.getName());
        viewHolder.ProductPrice.setText("Rs. "+curProduct.getPrice());


        Picasso.with(mContext)
                .setIndicatorsEnabled(false);
/*
// Code for storing image files locally.
        String str = "justbakers" ;
        File mydir =     mContext.getDir(str, Context.MODE_PRIVATE);
        String str2 = md5(curProduct.getImage());    // getImage() should return String like  "Bakery/seller1/brownbread.png"
        File localFile = new File(mydir, str2 + ".png");


        if (localFile.exists()) {

                Picasso.with(mContext)
                        .load(localFile).memoryPolicy(MemoryPolicy.NO_CACHE)
                        .placeholder(R.drawable.loader)
                        .error(R.drawable.loader)
                        .into(viewHolder.ProductImage);


            } else {
                String imgfile = curProduct.getImage();
                saveImgToInternalStorage(imgfile, localFile);
            }

 */
                  Picasso.with(mContext)
                    .load(curProduct.getImage())
                    .placeholder(R.drawable.loader)
                    .error(R.drawable.loader)
                    .into(viewHolder.ProductImage);


        return convertView;
    }

    public interface ButtonClickListener {
        void onButtonClick(int position, View view);
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public ProductClass getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setProductList(ArrayList<ProductClass> productList) {
        this.productList = productList;

        notifyDataSetChanged();
    }

    public void setCartProductList(ArrayList<ProductClass> cartProductList) {
        this.cartProductList = cartProductList;

        notifyDataSetChanged();
    }

    public int getCartItemsCount() {
        return cartProductList.size();
    }

    private void saveImgToInternalStorage(String firebasePath, File localPath){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            signInAnonymously();
        }

        FirebaseStorage firebaseStorage  = FirebaseStorage.getInstance();
        StorageReference fileRef = firebaseStorage.getReference().child(firebasePath);

        FileDownloadTask fileDownloadTask = fileRef.getFile(localPath);

        try {
        fileDownloadTask.addOnSuccessListener(taskSnapshot -> {
                    Picasso.with(mContext)
                  //          .load(curProduct.getImage())
                            .load(localPath).memoryPolicy(MemoryPolicy.NO_CACHE)
                            .placeholder(R.drawable.loader)
                            .error(R.drawable.loader)
                            .into(viewHolder.ProductImage);

                }
            // Local temp file has been created
            );

        fileDownloadTask.addOnFailureListener(e -> {

        });
      }catch (NullPointerException ignore) {

      }


    }

    private void signInAnonymously() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously().addOnSuccessListener ( new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // do your stuff
            }
        })
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Log.e(TAG, "signInAnonymously:FAILURE", exception);
                    }
                });
    }
}

