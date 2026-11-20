package kpn.projects.notetoself.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(
        tableName = "task_occurrences",
        foreignKeys = @ForeignKey(
                entity =  Task.class,
                parentColumns = "id",
                childColumns = "taskId",
                onDelete = ForeignKey.CASCADE
        )
)
public class TaskOccurrence {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long taskId;
    @NonNull
    public LocalDate scheduledDate;
    public LocalDateTime completedAt;
    @NonNull
    public boolean completed;
}
