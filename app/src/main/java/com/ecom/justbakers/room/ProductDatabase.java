package com.ecom.justbakers.room;

import android.content.Context;
import android.content.SharedPreferences;

import com.ecom.justbakers.Classes.ProductClass;
import com.ecom.justbakers.R;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

    @Database(entities = {ProductClass.class}, version = 1, exportSchema = false)
    public abstract class ProductDatabase extends RoomDatabase {

        private static ProductDatabase prodDB;

        public abstract ProductDao daoAccess();

        public static ProductDatabase getInstance(Context context) {
            if (prodDB == null) {
                prodDB = buildDatabaseInstance ( context);

            }

            return  prodDB;
        }

        private static ProductDatabase buildDatabaseInstance (Context ctxt) {
            return Room.databaseBuilder(ctxt, ProductDatabase.class, "product_db").allowMainThreadQueries().build();
        }

        public void cleanUp(){
            prodDB = null;
        }
    }