package kpn.projects.notetoself.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.DayOfWeek;
import java.time.LocalTime;

import kpn.projects.notetoself.enums.TaskColour;

@Entity(tableName = "classes")
public class ScheduleItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public String title;
    public String location;
    @NonNull
    public DayOfWeek dayOfWeek;
    @NonNull
    public LocalTime startTime;
    @NonNull
    public LocalTime endTime;
    @NonNull
    public TaskColour colour = TaskColour.NONE;
}
