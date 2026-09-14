package kpn.projects.notetoself;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import kpn.projects.notetoself.debug.DebugSeeder;
import kpn.projects.notetoself.notifications.NotificationAlarmScheduler;

public class NoteToSelfApp extends Application {
    public static final String STICKY_CHANNEL_ID = "sticky_tasks";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        NotificationAlarmScheduler.ensureScheduled(this);

        if (BuildConfig.DEBUG) {
            DebugSeeder.seedIfEmpty(this);
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                STICKY_CHANNEL_ID,
                getString(R.string.channel_sticky_name),
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription(getString(R.string.channel_sticky_description));

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }
}
