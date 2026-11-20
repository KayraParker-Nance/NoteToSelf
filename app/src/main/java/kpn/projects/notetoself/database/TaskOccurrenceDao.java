package kpn.projects.notetoself.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface TaskOccurrenceDao {
    @Insert
    public long insert(TaskOccurrence occurrence);
    @Insert
    public long insertOccurrences(List<TaskOccurrence> occurrences);
    @Update
    public void update(TaskOccurrence occurrence);
    @Query("SELECT * FROM task_occurrences WHERE id = :id")
    public TaskOccurrence getByIdSync(long id);
    @Query("SELECT * FROM task_occurrences")
    public List<TaskOccurrence> getAllSync();
    @Query("SELECT * FROM task_occurrences WHERE scheduledDate = :day AND completed = 0")
    public LiveData<List<TaskOccurrence>> getUncompletedForDay(LocalDate day);
    @Query("SELECT * FROM task_occurrences WHERE taskId = :taskId ORDER BY scheduledDate DESC")
    public LiveData<List<TaskOccurrence>> getHistoryForTask(long taskId);
    @Query("SELECT * FROM task_occurrences WHERE taskId = :taskId ORDER BY scheduledDate DESC LIMIT 1")
    public TaskOccurrence getMostRecent(long taskId);
    @Query("UPDATE task_occurrences SET completed = 1, completedAt = :completedAt WHERE id = :occurrenceId")
    public void markCompleted(long occurrenceId, java.time.LocalDateTime completedAt);

}
