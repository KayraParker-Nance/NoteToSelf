package kpn.projects.notetoself.database;

import androidx.room.TypeConverter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import kpn.projects.notetoself.enums.TaskColour;

public class MyConverters {

    //Convert from string to date time
    @TypeConverter
    public static LocalDateTime fromDateTimeString(String value) {
        return value==null ? null : LocalDateTime.parse(value);
    }

    //Convert from date time to string
    @TypeConverter
    public static String dateTimeToString(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toString();
    }

    //Convert from string to date
    @TypeConverter
    public static LocalDate fromDateString(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    //Convert from date to string
    @TypeConverter
    public static String dateToString(LocalDate date) {
        return date == null ? null : date.toString();
    }

    //Convert from string to time
    @TypeConverter
    public static LocalTime fromTimeString(String value) {
        return value == null ? null : LocalTime.parse(value);
    }

    //Convert from time to string
    @TypeConverter
    public static String timeToString(LocalTime time) {
        return time == null ? null : time.toString();
    }

    //Convert from string to task colour
    @TypeConverter
    public static TaskColour fromColourString(String value) {
        if (value == null) return TaskColour.NONE;
        try {
            return TaskColour.valueOf(value);
        } catch (IllegalArgumentException e) {
            return TaskColour.NONE;
        }
    }

    //Convert from colour to string
    @TypeConverter
    public static String colorToString(TaskColour colour) {
        return (colour == null ? TaskColour.NONE : colour).name();
    }

    //Convert from int to day
    @TypeConverter
    public static DayOfWeek fromDayOfWeekValue(Integer value) {
        return value == null ? null : DayOfWeek.of(value);
    }

    //Convert from day to int
    @TypeConverter
    public static Integer dayOfWeekToValue(DayOfWeek day) {
        return day == null ? null : day.getValue();
    }
}
