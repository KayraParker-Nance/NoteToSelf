package kpn.projects.notetoself;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

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
        version = 3
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract TaskOccurrenceDao taskOccurrenceDao();
    public abstract NotificationConfigDao notificationConfigDao();

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "notetoself_db"
                    ).fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
