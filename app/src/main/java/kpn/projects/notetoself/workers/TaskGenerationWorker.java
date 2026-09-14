package kpn.projects.notetoself.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.time.LocalDate;
import java.util.List;

import kpn.projects.notetoself.AppDatabase;
import kpn.projects.notetoself.enums.OccurrenceStatus;
import kpn.projects.notetoself.enums.RecurrenceUnit;
import kpn.projects.notetoself.enums.ScheduleMode;
import kpn.projects.notetoself.tasks.Task;
import kpn.projects.notetoself.tasks.TaskDao;
import kpn.projects.notetoself.tasks.TaskOccurrence;
import kpn.projects.notetoself.tasks.TaskOccurrenceDao;

public class TaskGenerationWorker extends Worker{

    public TaskGenerationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        TaskDao taskDao = db.taskDao();
        TaskOccurrenceDao occurrenceDao = db.taskOccurrenceDao();

        List<Task> regularTasks = taskDao.getAllRegularTasksSync();
        LocalDate today = LocalDate.now();

        for (Task task : regularTasks) {
            TaskOccurrence latest = occurrenceDao.getMostRecent(task.id);

            if (latest == null) {
                // First occurrence ever for this task
                occurrenceDao.insert(buildOccurrence(task.id, task.recurrenceStartDate));
                continue;
            }

            boolean isPast = latest.scheduledDate.isBefore(today);
            boolean isPending = latest.status == OccurrenceStatus.PENDING;

            if (isPast && isPending) {
                occurrenceDao.markMissed(latest.id);
                latest.status = OccurrenceStatus.MISSED;
            }

            boolean needsNext = latest.status != OccurrenceStatus.PENDING
                    && occurrenceDao.countPendingForTask(task.id) == 0;

            if (!needsNext) continue;

            LocalDate nextDate = computeNextDate(task, latest);
            occurrenceDao.insert(buildOccurrence(task.id, nextDate));
        }

        return Result.success();
    }

    private LocalDate computeNextDate(Task task, TaskOccurrence latest) {
        if (task.scheduleMode == ScheduleMode.FIXED) {
            // Pure calendar math — walk forward from recurrenceStartDate by the interval
            // until we pass today. Ignores completedAt entirely.
            LocalDate candidate = task.recurrenceStartDate;
            LocalDate today = LocalDate.now();
            while (!candidate.isAfter(today)) {
                candidate = addInterval(candidate, task.recurrenceInterval, task.recurrenceUnit);
            }
            return candidate;
        } else {
            // FLOATING: anchor to completion, or fall back to scheduled date if missed
            LocalDate anchor = latest.status == OccurrenceStatus.COMPLETED
                    ? latest.completedAt.toLocalDate()
                    : latest.scheduledDate;
            return addInterval(anchor, task.recurrenceInterval, task.recurrenceUnit);
        }
    }

    private LocalDate addInterval(LocalDate date, int interval, RecurrenceUnit unit) {
        switch (unit) {
            case DAY:   return date.plusDays(interval);
            case WEEK:  return date.plusWeeks(interval);
            case MONTH: return date.plusMonths(interval); // java.time clamps overflowing days automatically
            default:    throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    private TaskOccurrence buildOccurrence(long taskId, LocalDate scheduledDate) {
        TaskOccurrence occ = new TaskOccurrence();
        occ.taskId = taskId;
        occ.scheduledDate = scheduledDate;
        occ.status = OccurrenceStatus.PENDING;
        return occ;
    }
}
