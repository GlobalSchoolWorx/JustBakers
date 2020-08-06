import android.provider.ContactsContract;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

public class ProductDao {

    @Dao
    public interface DaoAccess {

        @Insert
        Long insertTask(ContactsContract.CommonDataKinds.Note note);


        @Query("SELECT * FROM ProductClass ORDER BY id desc")
        LiveData<List<ContactsContract.CommonDataKinds.Note>> fetchAllTasks();


        @Query("SELECT * FROM ProductClass WHERE id =:taskId")
        LiveData<ContactsContract.CommonDataKinds.Note> getTask(int taskId);


        @Update
        void updateTask(ContactsContract.CommonDataKinds.Note note);


        @Delete
        void deleteTask(ContactsContract.CommonDataKinds.Note note);
    }
}
