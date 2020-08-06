package com.ecom.justbakers.Adapters;

/**
 * Created by brainbreaker.
 */

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecom.justbakers.Classes.ProductClass;
import com.ecom.justbakers.DescriptionActivity;
import com.ecom.justbakers.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CustomCartListAdapter extends BaseAdapter {

    private List<ProductClass> mCartProductList;
    private CustomProductListAdapter.ButtonClickListener mdeleteClickListener = null;
    private CustomProductListAdapter.ButtonClickListener mIncrQtyClickListener = null;
    private CustomProductListAdapter.ButtonClickListener mDecrQtyClickListener = null;
    private LayoutInflater mInflater;
    private Context context;
    public CustomCartListAdapter(ArrayList<ProductClass> CartProductList, LayoutInflater inflater
            , Context context,CustomProductListAdapter.ButtonClickListener mdeleteClickListener
            , CustomProductListAdapter.ButtonClickListener mIncrQtyClickListener
            , CustomProductListAdapter.ButtonClickListener mDecrQtyClickListener ) {
        mCartProductList = CartProductList;
        mInflater = inflater;
        this.context = context;
        this.mdeleteClickListener = mdeleteClickListener;
        this.mIncrQtyClickListener = mIncrQtyClickListener;
        this.mDecrQtyClickListener = mDecrQtyClickListener;
    }

    @Override
    public int getCount() {
        return mCartProductList.size();
    }

    @Override
    public ProductClass getItem(int position) {
        return mCartProductList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewItemHolder item;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.cart_item_layout, null);
            item = new ViewItemHolder();
            item.ProductTitle = (TextView) convertView.findViewById(R.id.productname);
            item.ProductImageView = (ImageView) convertView.findViewById(R.id.pimageview);
            item.PriceTextView = (TextView) convertView.findViewById(R.id.productprice);
            item.SellerTextView = (TextView) convertView.findViewById(R.id.sellername);
            item.IncrQuantity = (TextView) convertView.findViewById(R.id.incr_quantity);
            item.ProductQuantity = (TextView) convertView.findViewById(R.id.productquantity);
            item.DecrQuantity = (TextView) convertView.findViewById(R.id.decr_quantity);
            item.DeleteButton = (ImageButton) convertView.findViewById(R.id.DeleteButton);

            convertView.setTag(item);
        } else {
            item = (ViewItemHolder) convertView.getTag();
        }

        final ProductClass curProduct = mCartProductList.get(position);

        Picasso.with(context).setIndicatorsEnabled(false);
        Picasso.with(context)
                .load(curProduct.getImage())
                .resize(200, 200)
                .placeholder(R.drawable.loader)
                .error(R.drawable.loader)
                .into(item.ProductImageView);

        /**THIS IS THE CODE FOR OPENING UP OF DESCRIPTION ACTIVITY WHEN USER CLICKS THE ITEM IN THE CART **/
        item.ProductImageView.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                       Intent DescriptionIntent = new Intent(context, DescriptionActivity.class);
                       DescriptionIntent.putExtra("ProductDetails", curProduct);
                       context.startActivity(DescriptionIntent);
                       }
        });


        item.ProductTitle.setText(curProduct.getName());
        item.PriceTextView.setText("Rs. "+curProduct.getPrice());
        item.SellerTextView.setText(curProduct.getSeller());
        item.ProductQuantity.setText(curProduct.getQuantity().toString() );
        item.IncrQuantity.setTag(position);
        item.DeleteButton.setTag(position);
        item.DecrQuantity.setTag(position);

        final View finalConvertView = convertView;

        item.IncrQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIncrQtyClickListener != null)
                    mIncrQtyClickListener.onButtonClick((Integer) v.getTag(), finalConvertView);
            }
        });


        item.DecrQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDecrQtyClickListener != null)
                    mDecrQtyClickListener.onButtonClick((Integer) v.getTag(), finalConvertView);
            }
        });

        item.DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mdeleteClickListener != null)
                    mdeleteClickListener.onButtonClick((Integer) v.getTag(), finalConvertView);
            }
        });
        return convertView;
    }

    private class ViewItemHolder {
        ImageView ProductImageView;
        TextView ProductTitle;
        TextView PriceTextView;
        TextView SellerTextView;
        TextView IncrQuantity;
        TextView ProductQuantity;
        TextView DecrQuantity;
        ImageButton DeleteButton;
    }

}

