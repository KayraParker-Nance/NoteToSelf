package kpn.projects.notetoself.utils;

import androidx.room.TypeConverter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import kpn.projects.notetoself.enums.TaskColour;

public class Converters {
    @TypeConverter
    public static LocalDateTime fromDateTimeString(String value){
        return value==null ? null : LocalDateTime.parse(value);
    }

    @TypeConverter
    public static String dateTimeToString(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toString();
    }

    @TypeConverter
    public static LocalDate fromDateString(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    @TypeConverter
    public static String dateToString(LocalDate date) {
        return date == null ? null : date.toString();
    }

    @TypeConverter
    public static TaskColour fromColorString(String value) {
        if (value == null) return TaskColour.NONE;
        try {
            return TaskColour.valueOf(value);
        } catch (IllegalArgumentException e) {
            return TaskColour.NONE;
        }
    }

    @TypeConverter
    public static String colorToString(TaskColour color) {
        return (color == null ? TaskColour.NONE : color).name();
    }

    @TypeConverter
    public static LocalTime fromTimeString(String value) {
        return value == null ? null : LocalTime.parse(value);
    }

    @TypeConverter
    public static String timeToString(LocalTime time) {
        return time == null ? null : time.toString();
    }

    @TypeConverter
    public static DayOfWeek fromDayOfWeekValue(Integer value) {
        return value == null ? null : DayOfWeek.of(value);
    }

    @TypeConverter
    public static Integer dayOfWeekToValue(DayOfWeek day) {
        return day == null ? null : day.getValue();
    }
}
