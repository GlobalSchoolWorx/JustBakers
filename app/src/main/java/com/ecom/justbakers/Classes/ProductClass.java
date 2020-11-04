package com.ecom.justbakers.Classes;

import com.google.auto.value.AutoValue;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Created by justbakers.
 */
//@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(indices = {@Index(value = "id", unique = true)})
public class ProductClass implements Serializable {

    private String name;
    private String image;
    private Integer price;
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
    public ProductClass(){}

    public ProductClass(final long rowId){
        this.rowId = rowId;
    }

    @Ignore
    public ProductClass(final @NonNull  String id){
        this.id = id;
    }

    @Ignore
    public ProductClass(String name, String image, Integer price, String description, String seller, @NonNull String id, String category, Integer limit){
        this.name = name;
        this.image = image;
        this.price = price;
        this.description = description;
        this.seller = seller;
        this.id = id;
        this.category = category;
        this.limit = limit;
    }

    @Ignore
    public ProductClass(String name, String image, Integer price, String description, String seller, @NonNull String id, String category, Integer quantity,
                        Integer limit){
        this.name = name;
        this.image = image;
        this.price = price;
        this.description = description;
        this.seller = seller;
        this.id = id;
        this.category = category;
        this.quantity = quantity;
        this.limit = limit;
    }

    @Ignore
    public ProductClass(String name, String image, Integer price, String description, String seller, @NonNull String id, Integer quantity, Integer limit){
        this.name = name;
        this.image = image;
        this.price = price;
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
}

