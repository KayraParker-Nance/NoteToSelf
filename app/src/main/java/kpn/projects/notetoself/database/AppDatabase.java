package kpn.projects.notetoself.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {
                Task.class,
                NotificationConfig.class,
                TaskOccurrence.class,
                ScheduleItem.class
        },
        version = 7
)
@TypeConverters({MyConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract TaskDao taskDao();

    public abstract TaskOccurrenceDao taskOccurrenceDao();

    public abstract NotificationConfigDao notificationConfigDao();

    public abstract ScheduleItemDao scheduleItemDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "notetoself_db"
                            ).addMigrations(MIGRATION_6_7)
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE tasks RENAME COLUMN recurrenceInterval TO repeatInterval");
            db.execSQL("ALTER TABLE tasks RENAME COLUMN recurrenceUnit TO repeatUnit");
            db.execSQL("ALTER TABLE tasks RENAME COLUMN recurrenceStartDate TO repeatStartDate");
            db.execSQL("ALTER TABLE tasks RENAME COLUMN scheduleMode TO repeatMode");
            db.execSQL("ALTER TABLE tasks RENAME COLUMN color TO colour");
            db.execSQL("ALTER TABLE task_occurrences RENAME COLUMN status TO completed");
            db.execSQL("ALTER TABLE classes RENAME COLUMN color TO colour");
            db.execSQL("ALTER TABLE task_occurrences ADD COLUMN completed INTEGER NOT NULL DEFAULT 0");
            db.execSQL("UPDATE task_occurrences SET completed = CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END");
            db.execSQL("ALTER TABLE task_occurrences DROP COLUMN status");
        }
    };
}
