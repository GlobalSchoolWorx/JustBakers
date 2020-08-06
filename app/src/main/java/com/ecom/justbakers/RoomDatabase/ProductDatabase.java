import com.ecom.justbakers.Classes.ProductClass;

import androidx.room.Database;
import androidx.room.RoomDatabase;

public class ProductDatabase {

    //@Database(entities = {}, version = 1, exportSchema = false)
    public abstract class NoteDatabase {

        public abstract ProductDao.DaoAccess daoAccess();
    }
}
