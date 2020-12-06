package com.ecom.justbakers.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecom.justbakers.Classes.Product;
import com.ecom.justbakers.DescriptionActivity;
import com.ecom.justbakers.R;
import com.ecom.justbakers.fragments.Action;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

public class CustomCartListAdapter extends BaseAdapter {
    private final List<Product> mCartProductList;
    private final Consumer<Pair<Action, Product>> actionConsumer;
    private final LayoutInflater mInflater;
    private final Context context;
    private final int screenWidth;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "in"));
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance();
    private final NumberFormat percentageFormat = NumberFormat.getPercentInstance();

    public CustomCartListAdapter(ArrayList<Product> CartProductList, Context context
            , Consumer<Pair<Action, Product>> actionConsumer
            , int screenWidth) {
        mCartProductList = CartProductList;
        mInflater = (LayoutInflater) context.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.actionConsumer = actionConsumer;
        this.screenWidth = screenWidth;
    }

    public void removeItem(int position){
        if(mCartProductList.size() > position){
            mCartProductList.remove(position);
            notifyDataSetInvalidated();
        }
    }

    public List<Product> getCartProductList(){
        return Collections.unmodifiableList(mCartProductList);
    }

    @Override
    public int getCount() {
        return mCartProductList.size();
    }

    @Override
    public Product getItem(int position) {
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
            convertView = mInflater.inflate(R.layout.cart_item_layout, parent, false);
            item = new ViewItemHolder();
            item.productTitle = convertView.findViewById(R.id.productname);
            item.productImageView = convertView.findViewById(R.id.pimageview);
            item.priceTextView = convertView.findViewById(R.id.productprice);
            item.discountedPriceTextView = convertView.findViewById(R.id.discountPrice);
            item.discountTV = convertView.findViewById(R.id.discountTV);
            item.sellerTextView = convertView.findViewById(R.id.sellername);
            item.incrQuantity = convertView.findViewById(R.id.incr_quantity);
            item.productQuantity = convertView.findViewById(R.id.productquantity);
            item.decrQuantity = convertView.findViewById(R.id.decr_quantity);
            item.deleteButton = convertView.findViewById(R.id.deleteButton);

            convertView.setTag(item);
        } else {
            item = (ViewItemHolder) convertView.getTag();
        }

        final Product curProduct = mCartProductList.get(position);

        Picasso.with(context).setIndicatorsEnabled(false);
        String str = "justbakers" ;
        File mydir =    context.getDir(str, Context.MODE_PRIVATE);
        String firebaseStr = curProduct.getImage();
        String localStr = firebaseStr.replace("/", "_");
        File localFile = new File(mydir, localStr);
        Picasso.with(context)
                .load(localFile)
                //    .resize(200, 200)
                .placeholder(R.drawable.loader)
                .error(R.drawable.loader)
                .into(item.productImageView);

        item.productImageView.setOnClickListener(v -> {
            Intent DescriptionIntent = new Intent(context, DescriptionActivity.class);
            DescriptionIntent.putExtra("ProductDetails", (Serializable) curProduct);
            context.startActivity(DescriptionIntent);
        });

        item.productTitle.setText(curProduct.getName());
        item.priceTextView.setText(currencyFormat.format(curProduct.getPrice()));
        double discount = (curProduct.getDiscount() != null ) ? curProduct.getDiscount() : 0;
        if ( discount > 0) {
            double discountedPrice = curProduct.getPrice() -  curProduct.getPrice() * discount * .01;
            item.priceTextView.setPaintFlags( item.priceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            item.discountedPriceTextView.setText(currencyFormat.format(discountedPrice));
            item.discountTV.setText(String.format(Locale.getDefault(), "%1$s%2$s", percentageFormat.format(discount/100), " OFF"));
        } else {
            item.discountedPriceTextView.setVisibility(View.GONE);
            item.discountTV.setVisibility(View.GONE);
        }
        item.sellerTextView.setText(curProduct.getSeller());
        item.productQuantity.setText(numberFormat.format(curProduct.getQuantity()));
        item.incrQuantity.setTag(position);
        item.deleteButton.setTag(position);
        item.decrQuantity.setTag(position);

        item.productImageView.getLayoutParams().width = screenWidth/5;
        item.productImageView.getLayoutParams().height = screenWidth/5;
        item.productImageView.requestLayout();

        item.incrQuantity.setOnClickListener(v -> incrementQuantity(position, item.productQuantity, item.incrQuantity, item.decrQuantity));
        item.decrQuantity.setOnClickListener(v -> decrementQuantity(position, item.productQuantity, item.incrQuantity, item.decrQuantity));
        item.deleteButton.setOnClickListener(v -> deleteQuantity(position));

        return convertView;
    }

    void deleteQuantity(int position){
        Product product = getItem(position);
        removeItem(position);
        if(actionConsumer != null) {
            actionConsumer.accept(new Pair<>(Action.DELETE, product));
        }
    }

    void incrementQuantity ( int position, TextView productQuantity, ImageButton incrQuantity, ImageButton decrQuantity) {
        String str = productQuantity.getText().toString();
        int qty = TextUtils.isEmpty(str) ? 0 : Integer.parseInt(str);

        Product product = getItem(position);
        if((qty+1) <= product.getLimit()) {
            product.setQuantity(++qty);
            productQuantity.setText (String.valueOf(qty));
            if(actionConsumer != null) {
                actionConsumer.accept(new Pair<>(Action.UPDATE, product));
            }
            if(qty == product.getLimit()){
                incrQuantity.setEnabled(false);
            }
        } else {
            incrQuantity.setEnabled(false);
        }
        decrQuantity.setEnabled(true);
    }

    void decrementQuantity( int position, TextView productQuantity, ImageButton incrQuantity, ImageButton decrQuantity) {
        String str = productQuantity.getText().toString();
        int qty = TextUtils.isEmpty(str) ? 0 : Integer.parseInt(str);
        if ( qty > 1) {
            Product product = getItem(position);
            qty--;
            product.setQuantity(qty);
            product.setQuantity(qty);
            productQuantity.setText (String.valueOf(qty));
            if(actionConsumer != null) {
                actionConsumer.accept(new Pair<>(Action.UPDATE, product));
            }
            incrQuantity.setEnabled(true);
        } else {
            deleteQuantity(position);
            decrQuantity.setEnabled(false);
        }
    }

    private static class ViewItemHolder {
        ImageView productImageView;
        TextView productTitle;
        TextView priceTextView;
        TextView discountedPriceTextView;
        TextView discountTV;
        TextView sellerTextView;
        ImageButton incrQuantity;
        TextView productQuantity;
        ImageButton decrQuantity;
        ImageButton deleteButton;
    }

}

