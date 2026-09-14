package kpn.projects.notetoself;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import kpn.projects.notetoself.tasks.NotificationConfig;
import kpn.projects.notetoself.tasks.NotificationConfigDao;
import kpn.projects.notetoself.tasks.Task;
import kpn.projects.notetoself.tasks.TaskDao;
import kpn.projects.notetoself.tasks.TaskOccurrence;
import kpn.projects.notetoself.tasks.TaskOccurrenceDao;
import kpn.projects.notetoself.utils.Converters;

@Database(
        entities = {
                Task.class,
                NotificationConfig.class,
                TaskOccurrence.class
        },
        version = 4
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract TaskOccurrenceDao taskOccurrenceDao();
    public abstract NotificationConfigDao notificationConfigDao();

    private static volatile AppDatabase instance;

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN color TEXT NOT NULL DEFAULT 'NONE'");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "notetoself_db"
                            ).addMigrations(MIGRATION_3_4)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
