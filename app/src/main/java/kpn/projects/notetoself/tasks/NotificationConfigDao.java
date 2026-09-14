package kpn.projects.notetoself.tasks;

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
    void insert(NotificationConfig config);

    @Update
    void update(NotificationConfig config);

    @Delete
    void delete(NotificationConfig config);

    @Query("SELECT * FROM notification_configs WHERE taskId = :taskId")
    LiveData<NotificationConfig> getForTask(long taskId);

    // Pulled by the sticky-notification service/worker to know what to render right now
    @Query("SELECT * FROM notification_configs WHERE enabled = 1 AND (showAfterDate IS NULL OR showAfterDate <= :now)")
    LiveData<List<NotificationConfig>> getActiveConfigs(LocalDateTime now);

    @Query("SELECT * FROM notification_configs WHERE enabled = 1 " +
            "AND (showAfterDate IS NULL OR showAfterDate <= :now) " +
            "AND (snoozedUntil IS NULL OR snoozedUntil <= :now)")
    List<NotificationConfig> getActiveConfigsSync(LocalDateTime now);

    @Query("SELECT * FROM notification_configs WHERE taskId = :taskId")
    NotificationConfig getForTaskSync(long taskId);

}
