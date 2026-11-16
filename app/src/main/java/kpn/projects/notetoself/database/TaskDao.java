package kpn.projects.notetoself.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    public Long insertTask(Task task);
    @Insert
    public void insertTasks(List<Task> tasks);
    @Update
    public void updateTask(Task task);
    @Delete
    public void deleteTask(Task task);
    @Query("DELETE FROM tasks")
    public void deleteAllTasks();
    @Query("SELECT * FROM tasks")
    public LiveData<List<Task>> getAllTasks();
    @Query("SELECT * FROM tasks")
    public List<Task> getAllTasksSync();
    @Query("SELECT * FROM tasks WHERE id = :id")
    LiveData<Task> getById(long id);
    @Query("SELECT * FROM tasks WHERE id = :id")
    public Task getByIdSync(long id);
    @Query("SELECT * FROM tasks WHERE type = 'TODO' AND completed = 0")
    public LiveData<List<Task>> getActiveTodos();
    @Query("SELECT * FROM tasks WHERE type = 'DUE_DATE' AND date(dueDate) BETWEEN date(:start) AND date(:end) ORDER BY dueDate ASC")
    LiveData<List<Task>> getDueDateTasksInRange(LocalDate start, LocalDate end);
    @Query("SELECT * FROM tasks WHERE type = 'DUE_DATE' AND completed = 0 AND date(dueDate) BETWEEN date(:start) AND date(:end) ORDER BY dueDate ASC")
    LiveData<List<Task>> getActiveDueDateTasksInRange(LocalDate start, LocalDate end);
    @Query("SELECT * FROM tasks WHERE type = 'REGULAR'")
    public LiveData<List<Task>> getAllRegularTasks();
    @Query("SELECT * FROM tasks WHERE type = 'REGULAR'")
    public List<Task> getAllRegularTasksSync();
    @Query("SELECT COUNT(*) FROM tasks")
    int countTasksSync();
}
