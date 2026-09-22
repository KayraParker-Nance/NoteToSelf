package kpn.projects.notetoself.tasks;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

import kpn.projects.notetoself.enums.ReminderUnit;

@Entity(
        tableName = "notification_configs",
        foreignKeys = @ForeignKey(
                entity = Task.class,
                parentColumns = "id",
                childColumns = "taskId",
                onDelete = ForeignKey.CASCADE
        )
)
public class NotificationConfig {
    @PrimaryKey
    @NonNull
    public long taskId;
    public boolean enabled;
    public LocalDateTime showAfterDate;
    public int repeatInterval;
    @NonNull
    public ReminderUnit repeatUnit = ReminderUnit.HOUR;
    public boolean stickyEnabled;
    public LocalDateTime snoozedUntil;
    public LocalDateTime lastNotifiedAt;
}
