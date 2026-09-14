package kpn.projects.notetoself.tasks;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface TaskDao {
    @Insert
    public Long insertTask(Task task);

    @Update
    public void updateTask(Task task);

    @Delete
    public void deleteTask(Task task);

    @Query("SELECT * FROM tasks")
    public LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE id = :id")
    public Task getByIdSync(long id);

    @Query("SELECT * FROM tasks WHERE type = 'TODO' AND completed = 0")
    public LiveData<List<Task>> getActiveTodos();

    @Query("SELECT * FROM tasks WHERE type = 'DUE_DATE' AND date(dueDate) BETWEEN date(:start) AND date(:end) ORDER BY dueDate ASC")
    LiveData<List<Task>> getDueDateTasksInRange(LocalDate start, LocalDate end);

    @Query("SELECT * FROM tasks WHERE type = 'REGULAR'")
    public LiveData<List<Task>> getAllRegularTasks();

    @Query("SELECT * FROM tasks WHERE type = 'REGULAR'")
    public List<Task> getAllRegularTasksSync();

    @Query("SELECT COUNT(*) FROM tasks")
    int countTasksSync();

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<Task> getTaskByIdLive(long taskId);
}
