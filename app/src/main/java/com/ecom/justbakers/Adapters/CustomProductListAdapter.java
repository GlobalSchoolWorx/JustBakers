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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by brainbreaker. ADAPTER FOR SHOWING PRODUCT LIST IN USER ACTIVITY
 */
public class CustomProductListAdapter extends BaseAdapter {

    private Context mContext;
    private ButtonClickListener mCartClickListener;
    private ButtonClickListener mAddItemClickListener;
    private ButtonClickListener mSubItemClickListener;
    private ArrayList<ProductClass> productList;
    private ArrayList<ProductClass> cartProductList;

    int screenWidth;
    public CustomProductListAdapter(Context c, ArrayList<ProductClass> productList, ArrayList<ProductClass> cartProductList,
                                    int screenWidth, ButtonClickListener cartListener,
                                    ButtonClickListener bargainListener, ButtonClickListener addItemListener, ButtonClickListener subItemListener) {
        mContext = c;
        this.productList = productList;
        this.cartProductList = cartProductList;
        this.screenWidth = screenWidth;
        mCartClickListener = cartListener;
        mAddItemClickListener = addItemListener;
        mSubItemClickListener = subItemListener;
    }

    private static class ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView addedInfo;
        TextView productPrice;
        info.hoang8f.widget.FButton addToCart;
        info.hoang8f.widget.FButton bargain;
        info.hoang8f.widget.FButton addItem;
        info.hoang8f.widget.FButton subItem;
        info.hoang8f.widget.FButton quantity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.product_list_item_layout, parent, false);

        }
        ProductClass curProduct = productList.get(position);

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        if(null == viewHolder) {
            viewHolder = new ViewHolder();
            viewHolder.productName = convertView.findViewById(R.id.productname);
            viewHolder.productPrice = convertView.findViewById(R.id.productprice);
            viewHolder.productImage = convertView.findViewById(R.id.productimageview);
            viewHolder.addedInfo = convertView.findViewById(R.id.addedInfo);
            viewHolder.addToCart = convertView.findViewById(R.id.Addtocart);
            viewHolder.bargain = convertView.findViewById(R.id.bargainbutton);
            viewHolder.addItem = convertView.findViewById(R.id.addbutton);
            viewHolder.subItem = convertView.findViewById(R.id.subbutton);
            viewHolder.quantity = convertView.findViewById(R.id.quantitybutton);
        }

        viewHolder.addedInfo.setVisibility(View.GONE);

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
        viewHolder.productImage.setTag(position);
        viewHolder.addToCart.setTag(position);
        viewHolder.bargain.setTag(position);
        viewHolder.addItem.setTag(position);
        viewHolder.addItem.setTag(position);
        viewHolder.subItem.setTag(position);

        final View finalConvertView = convertView;
        viewHolder.addToCart.setOnClickListener(v -> {
            if(mCartClickListener != null)
                mCartClickListener.onButtonClick((Integer) v.getTag(), finalConvertView);

        });

        viewHolder.addItem.setOnClickListener(v -> {
            if(mAddItemClickListener != null)
                mAddItemClickListener.onButtonClick((Integer) v.getTag(), finalConvertView);
        });

        viewHolder.subItem.setOnClickListener(v -> {
            if(mSubItemClickListener != null)
                mSubItemClickListener.onButtonClick((Integer) v.getTag(), finalConvertView);

        });

        viewHolder.productImage.setOnClickListener(v -> {
            Intent DescriptionIntent = new Intent(mContext, DescriptionActivity.class);
            DescriptionIntent.putExtra("SelectedProductPosition",position);
            ProductClass cProduct = productList.get((Integer)v.getTag());
            DescriptionIntent.putExtra("ProductDetails",cProduct);
            mContext.startActivity(DescriptionIntent);

        });

        viewHolder.productImage.getLayoutParams().width = screenWidth;
        viewHolder.productImage.getLayoutParams().height = Math.round(screenWidth / 2);
        viewHolder.productImage.requestLayout();

        viewHolder.productName.setText(curProduct.getName());
        viewHolder.productPrice.setText("Rs. "+curProduct.getPrice());


        Picasso.with(mContext).setIndicatorsEnabled(false);

        String str = "justbakers" ;
        File mydir =     mContext.getDir(str, Context.MODE_PRIVATE);
        String firebaseStr = curProduct.getImage();
        String localStr = firebaseStr.replace("/", "_");
        File localFile = new File(mydir, localStr);

        Picasso.with(mContext)
                .load(localFile)
                .placeholder(R.drawable.loader)
                .error(R.drawable.loader)
                .into(viewHolder.productImage);

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



}

