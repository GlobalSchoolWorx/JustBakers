package com.ecom.justbakers.room;

import android.content.Context;

import com.ecom.justbakers.Classes.Product;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Product.class}, version = 2, exportSchema = true)
    public abstract class ProductDatabase extends RoomDatabase {

        private static ProductDatabase prodDB;

        public abstract ProductDao daoAccess();

        private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `products` (`name` TEXT, `image` TEXT, `price` INTEGER, `discount` REAL DEFAULT 0.0, `description` TEXT, `seller` TEXT, `rowId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `id` TEXT NOT NULL, `category` TEXT, `quantity` INTEGER, `limit` INTEGER)");
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_products_id` ON `products` (`id`)");
                database.execSQL("INSERT INTO `products` (`name`, `image`, `price`, `description`, `seller`, `rowId`, `id`, `category`, `quantity`, `limit`) SELECT * FROM `ProductClass`");
                database.execSQL("DROP TABLE IF EXISTS `ProductClass`");
            }
        };

        public static ProductDatabase getInstance(Context context) {
            if (prodDB == null) {
                prodDB = buildDatabaseInstance ( context);
            }

            return  prodDB;
        }

        private static ProductDatabase buildDatabaseInstance (Context ctxt) {
            return Room.databaseBuilder(ctxt, ProductDatabase.class, "product_db").allowMainThreadQueries().addMigrations(MIGRATION_1_2).build();
        }

        public void cleanUp(){
            prodDB = null;
        }
    }