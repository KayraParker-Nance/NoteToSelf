package kpn.projects.notetoself.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.work.impl.utils.ForceStopRunnable;

import kpn.projects.notetoself.AppDatabase;
import kpn.projects.notetoself.tasks.NotificationConfig;
import kpn.projects.notetoself.tasks.Task;
import android.content.BroadcastReceiver;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class NotificationActionReceiver extends BroadcastReceiver {
    public static final String ACTION_COMPLETE = "kpn.projects.notetoself.ACTION_COMPLETE";
    public static final String ACTION_SNOOZE = "kpn.projects.notetoself.ACTION_SNOOZE";

    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_OCCURRENCE_ID = "extra_occurrence_id";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    private static final int DEFAULT_SNOOZE_HOURS = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L);
        long occurrenceId = intent.getLongExtra(EXTRA_OCCURRENCE_ID, -1L);
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        Context appContext = context.getApplicationContext();

        PendingResult pendingResult = goAsync(); // keeps the receiver alive past onReceive for the background write

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);

                if (ACTION_COMPLETE.equals(action)) {
                    if (occurrenceId != -1L) {
                        db.taskOccurrenceDao().markCompleted(occurrenceId, LocalDateTime.now());
                    } else if (taskId != -1L) {
                        Task task = db.taskDao().getByIdSync(taskId);
                        if (task != null) {
                            task.completed = true;
                            db.taskDao().updateTask(task);
                        }
                    }
                    NotificationRefreshReceiver.triggerImmediateRefresh(appContext);


                } else if (ACTION_SNOOZE.equals(action)) {
                    NotificationConfig config = db.notificationConfigDao().getForTaskSync(taskId);
                    if (config != null) {
                        int hours = config.repeatIntervalHours > 0 ? config.repeatIntervalHours : DEFAULT_SNOOZE_HOURS;
                        LocalDateTime snoozedUntil = LocalDateTime.now().plusHours(hours);
                        config.snoozedUntil = snoozedUntil;
                        db.notificationConfigDao().insert(config); // REPLACE strategy handles the update

                        long triggerAt = snoozedUntil.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        NotificationAlarmScheduler.scheduleSnoozeCheck(appContext, triggerAt);
                    }
                }

                if (notificationId != -1) {
                    NotificationManager manager = appContext.getSystemService(NotificationManager.class);
                    manager.cancel(notificationId);
                }
            } finally {
                pendingResult.finish();
            }
        }).start();
    }
}
