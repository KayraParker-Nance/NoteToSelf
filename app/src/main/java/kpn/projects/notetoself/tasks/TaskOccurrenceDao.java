package kpn.projects.notetoself.tasks;

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
    long insert(TaskOccurrence occurrence);

    @Update
    void update(TaskOccurrence occurrence);

    @Query("SELECT * FROM task_occurrences WHERE scheduledDate = :day AND status = 'PENDING'")
    LiveData<List<TaskOccurrence>> getPendingForDay(LocalDate day);

    @Query("SELECT COUNT(*) FROM task_occurrences WHERE taskId = :taskId AND status = 'PENDING'")
    int countPendingForTask(long taskId);

    // Full history for a task for the "how early/late was I" view
    @Query("SELECT * FROM task_occurrences WHERE taskId = :taskId ORDER BY scheduledDate DESC")
    LiveData<List<TaskOccurrence>> getHistoryForTask(long taskId);

    @Query("SELECT * FROM task_occurrences WHERE taskId = :taskId ORDER BY scheduledDate DESC LIMIT 1")
    TaskOccurrence getMostRecent(long taskId);

    @Query("UPDATE task_occurrences SET status = 'MISSED' WHERE id = :occurrenceId")
    void markMissed(long occurrenceId);

    @Query("UPDATE task_occurrences SET status = 'COMPLETED', completedAt = :completedAt WHERE id = :occurrenceId")
    void markCompleted(long occurrenceId, java.time.LocalDateTime completedAt);

    public class TaskOccurrenceWithTitle {
        public long occurrenceId;
        public long taskId;
        public String title;
        public LocalDate scheduledDate;
    }

    @Query("SELECT o.id AS occurrenceId, o.taskId AS taskId, t.title AS title, o.scheduledDate AS scheduledDate " +
            "FROM task_occurrences o " +
            "JOIN tasks t ON t.id = o.taskId " +
            "WHERE o.status = 'PENDING' AND o.scheduledDate <= :end " +
            "ORDER BY o.scheduledDate ASC")
    LiveData<List<TaskOccurrenceWithTitle>> getPendingUpTo(LocalDate end);

    @Query("SELECT * FROM task_occurrences WHERE taskId = :taskId AND status = 'PENDING' " +
            "AND scheduledDate <= :today ORDER BY scheduledDate ASC LIMIT 1")
    TaskOccurrence getDueOrOverduePendingForTaskSync(long taskId, LocalDate today);
}
