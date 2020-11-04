package com.ecom.justbakers.room;

import com.ecom.justbakers.Classes.ProductClass;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

@Dao
public interface ProductDao {
    @Insert
    long insert(ProductClass pc);

    @Query("SELECT * FROM ProductClass ORDER BY id desc")
    LiveData<List<ProductClass>> fetchAllTasks();

    @Query("SELECT * FROM ProductClass ORDER BY id desc")
    List<ProductClass> fetchAllTasksBlocking();

    @Query("SELECT * FROM ProductClass WHERE id =:id")
    LiveData<ProductClass> getProduct(String id);

    @Query("SELECT * FROM ProductClass WHERE id =:id")
    ProductClass getProductBlocking(String id);

    @Update
    void update(ProductClass pc);

    @Query("SELECT EXISTS(SELECT * FROM ProductClass WHERE id = :taskId)")
    boolean isProductExist(String taskId);


    @Delete
    void delete(ProductClass pc);
}
