package com.ecom.justbakers.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecom.justbakers.Classes.Product;
import com.ecom.justbakers.DescriptionActivity;
import com.ecom.justbakers.R;
import com.ecom.justbakers.fragments.Action;
import com.firebase.client.annotations.NotNull;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;
import info.hoang8f.widget.FButton;

import static java.lang.String.format;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {

    @NotNull
    private final Context context;
    private final Consumer<Pair<Action, Product>> cartChangedConsumer;
    @NotNull
    private ArrayList<Product> products;
    @NotNull
    private final Map<String, Product> cartProducts;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "in"));
    private final NumberFormat percentageFormat = NumberFormat.getPercentInstance();
    private int lastClickedItem = -1;

    public ProductListAdapter(@NotNull Activity cxt, @NotNull ArrayList<Product> products, @NotNull Map<String, Product> cartProducts, Consumer<Pair<Action, Product>> cartChangedConsumer) {
        context = cxt;
        this.products = products;
        this.cartProducts = cartProducts;
        this.cartChangedConsumer = cartChangedConsumer;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView productImage;
        TextView productName;
        TextView addedInfo;
        TextView productPrice;
        TextView discountedPrice;
        TextView discountTextView;
        FButton addToCart;
        FButton bargain;
        FButton addButton;
        FButton subButton;
        FButton quantityButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            discountedPrice = itemView.findViewById(R.id.discount_price);
            discountTextView = itemView.findViewById(R.id.discount_text_view);
            productImage = itemView.findViewById(R.id.product_image_view);
            addedInfo = itemView.findViewById(R.id.added_info);
            addToCart = itemView.findViewById(R.id.add_to_cart);
            bargain = itemView.findViewById(R.id.bargain_button);
            addButton = itemView.findViewById(R.id.add_button);
            subButton = itemView.findViewById(R.id.sub_button);
            quantityButton = itemView.findViewById(R.id.quantity_button);
        }
    }

    private void cartButtonClick(@Nullable Product product, FButton addToCart,
                                 FButton addButton, FButton subButton, FButton quantityButton){
        if(null == product) return;

        product.setQuantity(1);
        if(null != cartChangedConsumer){
            cartChangedConsumer.accept(new Pair<>(Action.UPDATE, product));
        }

        addButton.setVisibility(View.VISIBLE);
        subButton.setVisibility(View.VISIBLE);
        quantityButton.setVisibility(View.VISIBLE);
        addToCart.setVisibility(View.GONE);
        quantityButton.setText("1");
    }

    private void addItemButtonClick(@Nullable Product product, TextView addedInfo, FButton incrButton, FButton qtyButton){
        if(null == product) return;

        int qty = Integer.parseInt(qtyButton.getText().toString());
        ++qty;

        if((qty) <= product.getLimit()) {
            product.setQuantity(qty);
            if(null != cartChangedConsumer){
                cartChangedConsumer.accept(new Pair<>(Action.UPDATE, product));
            }
            qtyButton.setText(format(Locale.getDefault(), "%d", qty));
        }

        if(qty >= product.getLimit()){
            incrButton.setEnabled(false);
            addedInfo.setText(String.format(Locale.getDefault(), "%1$s%2$d", context.getString(R.string.max_product_quantity), product.getLimit()));
        }
    }

    private void subItemButtonClick(@Nullable Product product, TextView addedInfo, FButton addToCart, FButton addButton, FButton subButton, FButton quantityButton){
        if(null == product) return;

        int qty = Integer.parseInt(quantityButton.getText().toString());

        product.setQuantity(--qty);

        addedInfo.setText("");
        if(0 >= qty) {
            if(null != cartChangedConsumer){
                cartChangedConsumer.accept(new Pair<>(Action.DELETE, product));
            }
            addButton.setVisibility(View.GONE);
            subButton.setVisibility(View.GONE);
            quantityButton.setVisibility(View.GONE);
            addToCart.setVisibility(View.VISIBLE);
        }else{
            if(null != cartChangedConsumer){
                cartChangedConsumer.accept(new Pair<>(Action.UPDATE, product));
            }
            quantityButton.setText(format(Locale.getDefault(), "%d", product.getQuantity()));
            addButton.setEnabled(true);
        }
    }

    public Product getItem(int position) {
        return products.get(position);
    }

    public void setLastClickedItem(int position){
        lastClickedItem = position;
    }

    public int getLastClickedItem(){
        return lastClickedItem;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list_item_layout, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Product curProduct = products.get(position);
        viewHolder.addedInfo.setVisibility(View.VISIBLE);
        viewHolder.addedInfo.setText("");

        Product cartProduct = null != cartProducts ? cartProducts.get(curProduct.getId()) : null;
        if ( (cartProduct != null) && (cartProduct.getQuantity() != null) && cartProduct.getQuantity() > 0) {
            viewHolder.addToCart.setVisibility(View.GONE);
            viewHolder.bargain.setVisibility(View.GONE);
            viewHolder.quantityButton.setVisibility(View.VISIBLE);
            viewHolder.addButton.setVisibility(View.VISIBLE);
            viewHolder.subButton.setVisibility(View.VISIBLE);
            viewHolder.quantityButton.setText(String.format(Locale.getDefault(), "%1d", cartProduct.getQuantity()));
        } else {
            viewHolder.bargain.setVisibility(View.GONE);
            viewHolder.quantityButton.setVisibility(View.GONE);
            viewHolder.addButton.setVisibility(View.GONE);
            viewHolder.subButton.setVisibility(View.GONE);

            viewHolder.addToCart.setVisibility(View.VISIBLE);
        }
        viewHolder.productImage.setTag(position);
        viewHolder.addToCart.setTag(position);
        viewHolder.bargain.setTag(position);
        viewHolder.addButton.setTag(position);
        viewHolder.addButton.setTag(position);
        viewHolder.subButton.setTag(position);

        final ViewHolder finalViewHolder = viewHolder;
        viewHolder.itemView.setOnClickListener(v -> {
            lastClickedItem = position;
            Product CurrentProduct = getItem(position);
            Intent DescriptionIntent = new Intent(context, DescriptionActivity.class);
            DescriptionIntent.putExtra("ProductDetails", (Serializable)CurrentProduct);
            DescriptionIntent.putExtra("SelectedProductPosition", position);
            context.startActivity(DescriptionIntent);
        });

        viewHolder.addToCart.setOnClickListener(v -> cartButtonClick(products.get(position),
                finalViewHolder.addToCart, finalViewHolder.addButton,
                finalViewHolder.subButton, finalViewHolder.quantityButton));

        viewHolder.addButton.setOnClickListener(v -> addItemButtonClick(cartProducts.get(products.get(position).getId()),
                finalViewHolder.addedInfo, finalViewHolder.addButton, finalViewHolder.quantityButton));

        viewHolder.subButton.setOnClickListener(v -> subItemButtonClick(cartProducts.get(products.get(position).getId()),
                finalViewHolder.addedInfo, finalViewHolder.addToCart, finalViewHolder.addButton,
                finalViewHolder.subButton, finalViewHolder.quantityButton));

        viewHolder.productImage.setOnClickListener(v -> {
            lastClickedItem = position;
            Intent DescriptionIntent = new Intent(context, DescriptionActivity.class);
            DescriptionIntent.putExtra("SelectedProductPosition", position);
            Product cProduct = products.get((Integer)v.getTag());
            DescriptionIntent.putExtra("ProductDetails", (Serializable) cProduct);
            context.startActivity(DescriptionIntent);
        });

        viewHolder.productName.setText(curProduct.getName());
        viewHolder.productPrice.setText(currencyFormat.format(curProduct.getPrice()));

        double discount = 0;
        if (curProduct.getDiscount() != null)
            discount = curProduct.getPrice() * curProduct.getDiscount() * .01;

        if ( discount > 0) {
            viewHolder.productPrice.setPaintFlags(viewHolder.productPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.discountedPrice.setVisibility(View.VISIBLE);
            viewHolder.discountTextView.setVisibility(View.VISIBLE);
            viewHolder.discountedPrice.setText(currencyFormat.format(curProduct.getPrice() - discount));
            viewHolder.discountTextView.setText(String.format(Locale.getDefault(), "%1$s%2$s", percentageFormat.format(curProduct.getDiscount()/100), " OFF"));
        } else {
            viewHolder.discountedPrice.setVisibility(View.GONE);
            viewHolder.discountTextView.setVisibility(View.GONE);
        }

        String str = "justbakers" ;
        File mydir =     context.getDir(str, Context.MODE_PRIVATE);
        String firebaseStr = curProduct.getImage();
        String localStr = firebaseStr.replace("/", "_");
        File localFile = new File(mydir, localStr);

        Picasso picasso = Picasso.with(context);
        picasso.setIndicatorsEnabled(false);
        picasso.load(localFile)
                .placeholder(R.drawable.loader)
                .error(R.drawable.loader)
                .into(viewHolder.productImage);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setProductList(ArrayList<Product> productList) {
        this.products = productList;

        notifyDataSetChanged();
    }
}

