package com.ecom.justbakers.room;

import com.ecom.justbakers.Classes.Product;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ProductDao {
    @Insert
    long insert(Product pc);

    @Query("SELECT * FROM products ORDER BY id desc")
    LiveData<List<Product>> fetchAll();

    @Query("SELECT * FROM products ORDER BY id desc")
    List<Product> fetchAllBlocking();

    @Query("SELECT * FROM products WHERE id =:id")
    LiveData<Product> getProduct(String id);

    @Query("SELECT * FROM products WHERE id =:id")
    Product getProductBlocking(String id);

    @Update
    void update(Product pc);

    @Query("SELECT EXISTS(SELECT * FROM products WHERE id = :taskId)")
    boolean isProductExists(String taskId);


    @Delete
    void delete(Product pc);
}
