package kpn.projects.notetoself;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import kpn.projects.notetoself.notifications.NotificationAlarmScheduler;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            TaskScheduler.scheduleDailyGeneration(context);
            NotificationAlarmScheduler.ensureScheduled(context);
        }
    }
}
