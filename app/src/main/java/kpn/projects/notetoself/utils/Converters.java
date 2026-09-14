package kpn.projects.notetoself.utils;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
}
