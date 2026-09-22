package kpn.projects.notetoself.notifications;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import androidx.annotation.ColorRes;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import kpn.projects.notetoself.AppDatabase;
import kpn.projects.notetoself.NoteToSelfApp;
import kpn.projects.notetoself.R;
import kpn.projects.notetoself.enums.TaskColour;
import kpn.projects.notetoself.tasks.NotificationConfig;
import kpn.projects.notetoself.tasks.Task;
import kpn.projects.notetoself.tasks.TaskOccurrence;
import kpn.projects.notetoself.ui.MainActivity;

public class NotificationRefreshReceiver extends BroadcastReceiver {
    private static final int PINK_ACCENT = 0xFFFCB5CD;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, d MMM");

    public static final String ACTION_REFRESH = "kpn.projects.notetoself.ACTION_REFRESH_NOTIFICATIONS";
    public static final String ACTION_HOURLY_CHECK = "kpn.projects.notetoself.ACTION_HOURLY_CHECK";
    public static final String ACTION_MIDNIGHT = "kpn.projects.notetoself.ACTION_MIDNIGHT";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Context appContext = context.getApplicationContext();
        PendingResult pendingResult = goAsync(); // survives past onReceive for the background DB work

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (ACTION_MIDNIGHT.equals(action)) {
                    NotificationAlarmScheduler.scheduleMidnightCheck(appContext); // reschedule for tomorrow first
                } else if (ACTION_HOURLY_CHECK.equals(action)) {
                    NotificationAlarmScheduler.scheduleHourlyCheck(appContext); // reschedule next tick first
                }
                refresh(appContext);
            } finally {
                pendingResult.finish();
            }
        });
    }

    public static void triggerImmediateRefresh(Context context) {
        Context appContext = context.getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> refresh(appContext));
    }

    private static void refresh(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        LocalDateTime now = LocalDateTime.now();

        Set<Integer> shownIds = new HashSet<>();
        for (StatusBarNotification sbn : manager.getActiveNotifications()) {
            shownIds.add(sbn.getId());
        }

        List<NotificationConfig> configs = db.notificationConfigDao().getActiveConfigsSync(now);

        for (NotificationConfig config : configs) {
            Task task = db.taskDao().getByIdSync(config.taskId);
            if (task == null) continue;

            NotificationItem item = buildItem(db, task);
            int id = (int) (2000 + task.id);

            if (item == null) {
                manager.cancel(id); // nothing to show anymore
                if (config.lastNotifiedAt != null) {
                    config.lastNotifiedAt = null; // so the next occurrence isn't held back by the old interval
                    db.notificationConfigDao().insert(config);
                }
                continue;
            }

            boolean isShowing = shownIds.contains(id);
            boolean shouldPost;
            if (config.repeatInterval > 0) {
                shouldPost = config.lastNotifiedAt == null
                        || !now.isBefore(config.repeatUnit.addTo(config.lastNotifiedAt, config.repeatInterval));
            } else {
                shouldPost = !isShowing;
            }

            if (shouldPost) {
                post(context, item);
                config.lastNotifiedAt = now;
                db.notificationConfigDao().insert(config);
            }
        }
    }

    private static NotificationItem buildItem(AppDatabase db, Task task) {
        switch (task.type) {
            case DUE_DATE:
                if (!task.completed && task.dueDate != null) {
                    return new NotificationItem(task.id, null, task.title,
                            task.dueDate.toLocalDate().format(DATE_FORMAT), task.color);
                }
                return null;
            case TODO:
                if (!task.completed) {
                    return new NotificationItem(task.id, null, task.title, task.description, task.color);
                }
                return null;
            case REGULAR:
                TaskOccurrence occurrence = db.taskOccurrenceDao()
                        .getDueOrOverduePendingForTaskSync(task.id, LocalDate.now());
                if (occurrence != null) {
                    return new NotificationItem(task.id, occurrence.id, task.title,
                            occurrence.scheduledDate.format(DATE_FORMAT), task.color);
                }
                return null;
            default:
                return null;
        }
    }

    private static void post(Context context, NotificationItem item) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, item.notificationId(), openAppIntent, PendingIntent.FLAG_IMMUTABLE);

        boolean hasColor = item.color != null && item.color != TaskColour.NONE;
        int accentColor = hasColor ? ContextCompat.getColor(context, item.color.getColorRes()) : PINK_ACCENT;

        Bundle extras = new Bundle();
        extras.putBoolean("android.preferSmallIcon", true);
        extras.putBoolean("android.support.preferSmallIcon", true);

        Notification notification = new NotificationCompat.Builder(context, NoteToSelfApp.STICKY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setColor(accentColor)
                .setContentTitle(item.title)
                .setContentText(item.subtitle)
                .setContentIntent(contentIntent)
                .addAction(0, context.getString(R.string.action_complete),
                        buildActionPendingIntent(context, NotificationActionReceiver.ACTION_COMPLETE, item))
                .addAction(0, context.getString(R.string.action_snooze),
                        buildActionPendingIntent(context, NotificationActionReceiver.ACTION_SNOOZE, item))
                .addExtras(extras)
                .build();

        NotificationManagerCompat.from(context).notify(item.notificationId(), notification);
    }

    private static PendingIntent buildActionPendingIntent(Context context, String action, NotificationItem item) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction(action);
        intent.putExtra(NotificationActionReceiver.EXTRA_TASK_ID, item.taskId);
        if (item.occurrenceId != null) {
            intent.putExtra(NotificationActionReceiver.EXTRA_OCCURRENCE_ID, item.occurrenceId);
        }
        intent.putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, item.notificationId());

        int requestCode = item.notificationId() * 10 + (action.equals(NotificationActionReceiver.ACTION_COMPLETE) ? 1 : 2);
        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static final float STROKE_DARKEN_AMOUNT = 0.18f;
    private static final int LARGE_ICON_SIZE_DP = 48;
}
