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
import kpn.projects.notetoself.enums.ReminderUnit;
import kpn.projects.notetoself.enums.TaskColour;
import kpn.projects.notetoself.enums.TaskType;

@Dao
public interface TaskDao {
    @Insert
    public Long insertTask(Task task);

    @Insert
    void insertTasks(List<Task> tasks);

    @Update
    public void updateTask(Task task);

    @Delete
    public void deleteTask(Task task);

    @Query("DELETE FROM tasks")
    void deleteAllTasks();

    @Query("SELECT * FROM tasks")
    public LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks")
    List<Task> getAllTasksSync();

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

    @Query("SELECT * FROM tasks WHERE type = 'DUE_DATE' AND completed = 0 " +
            "AND date(dueDate) BETWEEN date(:start) AND date(:end) ORDER BY dueDate ASC")
    LiveData<List<Task>> getActiveDueDateTasksInRange(LocalDate start, LocalDate end);

    class TaskTemplate {
        public String title;
        public TaskType type;
        public TaskColour color;
        public boolean hasConfig;
        public boolean stickyEnabled;
        public int repeatInterval;
        public ReminderUnit repeatUnit;
    }
    @Query("SELECT t.title AS title, t.type AS type, t.color AS color, " +
            "c.taskId IS NOT NULL AS hasConfig, c.stickyEnabled AS stickyEnabled, " +
            "c.repeatInterval AS repeatInterval, c.repeatUnit AS repeatUnit " +
            "FROM tasks t LEFT JOIN notification_configs c ON c.taskId = t.id " +
            "WHERE t.id IN (SELECT MAX(id) FROM tasks GROUP BY LOWER(title)) " +
            "AND t.title LIKE :prefix || '%' ESCAPE '\\' " +
            "ORDER BY t.title COLLATE NOCASE LIMIT 5")
    List<TaskTemplate> findTemplates(String prefix);
}
