package kpn.projects.notetoself.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface NotificationConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(NotificationConfig config);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertConfigs(List<NotificationConfig> configs);
    @Update
    public void update(NotificationConfig config);
    @Delete
    public void delete(NotificationConfig config);
    @Query("SELECT * FROM notification_configs")
    public List<NotificationConfig> getAllConfigsSync();
    @Query("SELECT * FROM notification_configs WHERE taskId = :taskId")
    public LiveData<NotificationConfig> getForTask(long taskId);
    @Query("SELECT * FROM notification_configs WHERE taskId = :taskId")
    public NotificationConfig getForTaskSync(long taskId);
    @Query("SELECT * FROM notification_configs WHERE enabled = 1 " +
            "AND (showAfterDate IS NULL OR showAfterDate <= :now) " +
            "AND (snoozedUntil IS NULL OR snoozedUntil <= :now)")
    public LiveData<List<NotificationConfig>> getActiveConfigs(LocalDateTime now);
    @Query("SELECT * FROM notification_configs WHERE enabled = 1 " +
            "AND (showAfterDate IS NULL OR showAfterDate <= :now) " +
            "AND (snoozedUntil IS NULL OR snoozedUntil <= :now)")
    public List<NotificationConfig> getActiveConfigsSync(LocalDateTime now);
}
