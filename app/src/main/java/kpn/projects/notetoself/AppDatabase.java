package kpn.projects.notetoself;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import kpn.projects.notetoself.schedule.Class;
import kpn.projects.notetoself.schedule.ClassDao;
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
                TaskOccurrence.class,
                Class.class
        },
        version = 6
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract TaskOccurrenceDao taskOccurrenceDao();
    public abstract NotificationConfigDao notificationConfigDao();
    public abstract ClassDao classDao();

    private static volatile AppDatabase instance;

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN color TEXT NOT NULL DEFAULT 'NONE'");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `classes` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT NOT NULL, " +
                    "`location` TEXT, " +
                    "`dayOfWeek` INTEGER NOT NULL, " +
                    "`startTime` TEXT NOT NULL, " +
                    "`endTime` TEXT NOT NULL, " +
                    "`color` TEXT NOT NULL DEFAULT 'NONE')");
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `notification_configs_new` (" +
                    "`taskId` INTEGER NOT NULL, `enabled` INTEGER NOT NULL, `showAfterDate` TEXT, " +
                    "`repeatInterval` INTEGER NOT NULL, `repeatUnit` TEXT NOT NULL, " +
                    "`stickyEnabled` INTEGER NOT NULL, `snoozedUntil` TEXT, `lastNotifiedAt` TEXT, " +
                    "PRIMARY KEY(`taskId`), " +
                    "FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("INSERT INTO notification_configs_new " +
                    "(taskId, enabled, showAfterDate, repeatInterval, repeatUnit, stickyEnabled, snoozedUntil, lastNotifiedAt) " +
                    "SELECT taskId, enabled, showAfterDate, repeatIntervalHours, 'HOUR', stickyEnabled, snoozedUntil, lastNotifiedAt " +
                    "FROM notification_configs");
            database.execSQL("DROP TABLE notification_configs");
            database.execSQL("ALTER TABLE notification_configs_new RENAME TO notification_configs");
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
                            ).addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
