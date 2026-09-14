package kpn.projects.notetoself.tasks;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.LocalDateTime;

import kpn.projects.notetoself.enums.RecurrenceUnit;
import kpn.projects.notetoself.enums.ScheduleMode;
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

    //DUE_DATE tasks only
    public LocalDateTime dueDate;

    //REGULAR tasks only
    public int recurrenceInterval;
    public RecurrenceUnit recurrenceUnit;
    public LocalDate recurrenceStartDate;
    public ScheduleMode scheduleMode;

    public boolean completed;
    @NonNull
    public LocalDateTime createdAt;
}
