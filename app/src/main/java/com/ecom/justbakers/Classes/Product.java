package com.ecom.justbakers.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Created by justbakers.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(tableName="products", indices = {@Index(value = "id", unique = true)})
public class Product implements Serializable, Parcelable {
    private String name;
    private String image;
    private Integer price;

    @NotNull
    @ColumnInfo(name = "discount", defaultValue = "0.0")
    private Double discount = 0.0d;
    private String description;
    private String seller;

    @PrimaryKey(autoGenerate = true)
    private long rowId;

    @NonNull
    private String id;

    private String category;
    private Integer quantity;
    private Integer limit;

    @Ignore
    public Product(){}

    public Product(final long rowId){
        this.rowId = rowId;
    }

    @Ignore
    public Product(final @NonNull  String id){
        this.id = id;
    }

    @Ignore
    public Product(String name, String image, Integer price, Double discount, String description, String seller, @NonNull String id, String category, Integer limit){
        this.name = name;
        this.image = image;
        this.price = price;
        this.discount =  discount;
        this.description = description;
        this.seller = seller;
        this.id = id;
        this.category = category;
        this.limit = limit;
    }

    @Ignore
    public Product(String name, String image, Integer price, Double discount, String description, String seller, @NonNull String id, String category, Integer quantity,
                   Integer limit){
        this.name = name;
        this.image = image;
        this.price = price;
        this.discount = discount;
        this.description = description;
        this.seller = seller;
        this.id = id;
        this.category = category;
        this.quantity = quantity;
        this.limit = limit;
    }

    @Ignore
    public Product(String name, String image, Integer price, Double discount, String description, String seller, @NonNull String id, Integer quantity, Integer limit){
        this.name = name;
        this.image = image;
        this.price = price;
        this.discount = discount;
        this.description = description;
        this.seller = seller;
        this.id = id;
        this.quantity = quantity;
        this.limit = limit;
    }

    public long getRowId() {
        return rowId;
    }

    public String getName(){
        return name;
    }
    public String getImage(){
        return image;
    }
    public Integer getPrice(){
        return price;
    }
    public Double getDiscount() { return discount; }
    public Integer getQuantity(){
        return quantity;
    }
    public String getSeller(){
        return seller;
    }

    public String getDescription(){
        return description;
    }
    public @NonNull String getId(){
        return id;
    }
    public String getCategory(){
        return category;
    }
    public Integer getLimit(){
        return limit;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }
    public void setImage(String image){
        this.image = image;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public void setPrice(Integer price){
        this.price = price;
    }
    public void setDiscount(Double discount){
        this.discount = discount;
    }
    public void setSeller(String seller){
        this.seller = seller;
    }
    public void setCategory(String category){
        this.category = category;
    }
    public void setLimit(Integer limit){
        this.limit = limit;
    }
    public void setQuantity(Integer qty){
        quantity = qty;
    }


    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Ignore
    protected Product(Parcel in){
        setId((String)Objects.requireNonNull(in.readValue(String.class.getClassLoader())));
        setRowId(in.readLong());
        setName((String)in.readValue(String.class.getClassLoader()));
        setImage((String)in.readValue(String.class.getClassLoader()));
        setDescription((String)in.readValue(String.class.getClassLoader()));
        setPrice((Integer)in.readValue(Integer.class.getClassLoader()));
        setDiscount((Double)in.readValue(Double.class.getClassLoader()));
        setSeller((String)in.readValue(String.class.getClassLoader()));
        setCategory((String)in.readValue(String.class.getClassLoader()));
        setLimit((Integer)in.readValue(Integer.class.getClassLoader()));
        setQuantity((Integer) in.readValue(Integer.class.getClassLoader()));
    }

    @Override
    @Ignore
    public int describeContents() {
        return 0;
    }

    @Override
    @Ignore
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(getId());
        dest.writeLong(getRowId());
        dest.writeValue(getName());
        dest.writeValue(getImage());
        dest.writeValue(getDescription());
        dest.writeValue(getPrice());
        dest.writeValue(getDiscount());
        dest.writeValue(getSeller());
        dest.writeValue(getCategory());
        dest.writeValue(getLimit());
        dest.writeValue(getQuantity());
    }
}

