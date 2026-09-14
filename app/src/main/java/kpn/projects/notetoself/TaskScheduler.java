package kpn.projects.notetoself;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.time.Duration;

import kpn.projects.notetoself.workers.TaskGenerationWorker;

public class TaskScheduler {
    private static final String GENERATION_WORK_NAME = "task_generation_daily";

    public static void scheduleDailyGeneration(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                TaskGenerationWorker.class,
                Duration.ofDays(1)
        ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                GENERATION_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // don't reset the schedule if already enqueued
                request
        );
    }
}
