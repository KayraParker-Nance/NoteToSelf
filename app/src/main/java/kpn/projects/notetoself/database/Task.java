package kpn.projects.notetoself.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.LocalDateTime;

import kpn.projects.notetoself.enums.RepeatMode;
import kpn.projects.notetoself.enums.RepeatUnit;
import kpn.projects.notetoself.enums.TaskColour;
import kpn.projects.notetoself.enums.TaskType;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public String title;
    public String description;
    @NonNull
    public TaskType type;
    public LocalDateTime dueDate;
    public int repeatInterval;
    public RepeatUnit repeatUnit;
    public LocalDate repeatStartDate;
    public RepeatMode repeatMode;
    public boolean completed;
    @NonNull
    public LocalDateTime createdAt;
    @NonNull
    TaskColour colour = TaskColour.NONE;

}
