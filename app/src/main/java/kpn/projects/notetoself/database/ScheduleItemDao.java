package kpn.projects.notetoself.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScheduleItemDao {
    @Insert
    public long insert(ScheduleItem scheduleItem);
    @Update
    public void update(ScheduleItem scheduleItem);
    @Delete
    public void delete(ScheduleItem scheduleItem);
    @Query("SELECT * FROM classes ORDER BY dayOfWeek ASC, startTime ASC")
    public LiveData<List<ScheduleItem>> getAll();
    @Query("SELECT * FROM classes WHERE id = :id")
    public LiveData<ScheduleItem> getById(long id);
}
