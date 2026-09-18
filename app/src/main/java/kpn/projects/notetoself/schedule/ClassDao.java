package kpn.projects.notetoself.schedule;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ClassDao {
    @Insert
    long insert(Class classEntry);

    @Update
    void update(Class classEntry);

    @Delete
    void delete(Class classEntry);

    @Query("SELECT * FROM classes ORDER BY dayOfWeek ASC, startTime ASC")
    LiveData<List<Class>> getAll();

    @Query("SELECT * FROM classes WHERE id = :id")
    LiveData<Class> getByIdLive(long id);
}
