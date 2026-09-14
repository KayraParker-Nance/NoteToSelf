package kpn.projects.notetoself.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class NotificationAlarmScheduler {
    private static final long HOURLY_CHECK_INTERVAL_MS = Duration.ofHours(1).toMillis();
    private static final int REQUEST_HOURLY = 100;
    private static final int REQUEST_MIDNIGHT = 101;
    private static final int REQUEST_SNOOZE = 102;

    // call on app start / boot — cheap no-op if alarms are already pending
    public static void ensureScheduled(Context context) {
        if (!isPending(context, REQUEST_HOURLY)) {
            scheduleHourlyCheck(context);
        }
        if (!isPending(context, REQUEST_MIDNIGHT)) {
            scheduleMidnightCheck(context);
        }
    }

    public static void scheduleHourlyCheck(Context context) {
        schedule(context, NotificationRefreshReceiver.ACTION_HOURLY_CHECK, REQUEST_HOURLY,
                System.currentTimeMillis() + HOURLY_CHECK_INTERVAL_MS);
    }

    public static void scheduleMidnightCheck(Context context) {
        LocalDateTime nextMidnight = LocalDate.now().plusDays(1).atStartOfDay();
        long triggerAt = nextMidnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        schedule(context, NotificationRefreshReceiver.ACTION_MIDNIGHT, REQUEST_MIDNIGHT, triggerAt);
    }

    // fires a one-off refresh right when a snooze period ends, for tighter timing than the hourly tick
    public static void scheduleSnoozeCheck(Context context, long triggerAtMillis) {
        schedule(context, NotificationRefreshReceiver.ACTION_REFRESH, REQUEST_SNOOZE, triggerAtMillis);
    }

    private static boolean isPending(Context context, int requestCode) {
        Intent intent = new Intent(context, NotificationRefreshReceiver.class);
        PendingIntent existing = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        return existing != null;
    }

    private static void schedule(Context context, String action, int requestCode, long triggerAtMillis) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        Intent intent = new Intent(context, NotificationRefreshReceiver.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }
}
