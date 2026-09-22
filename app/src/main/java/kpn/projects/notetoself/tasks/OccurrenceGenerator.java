package kpn.projects.notetoself.tasks;

import java.time.LocalDate;
import java.time.LocalDateTime;

import kpn.projects.notetoself.AppDatabase;
import kpn.projects.notetoself.enums.OccurrenceStatus;
import kpn.projects.notetoself.enums.RecurrenceUnit;
import kpn.projects.notetoself.enums.ScheduleMode;

public final class OccurrenceGenerator {
    private OccurrenceGenerator() {}

    /** Marks the occurrence completed and immediately creates the next one. Call off the main thread. */
    public static void completeOccurrence(AppDatabase db, long occurrenceId) {
        db.runInTransaction(() -> {
            TaskOccurrenceDao occurrenceDao = db.taskOccurrenceDao();
            occurrenceDao.markCompleted(occurrenceId, LocalDateTime.now());

            TaskOccurrence occurrence = occurrenceDao.getByIdSync(occurrenceId);
            if (occurrence == null) return;
            Task task = db.taskDao().getByIdSync(occurrence.taskId);
            if (task != null) ensureNextOccurrence(db, task);
        });
    }

    /** Flags an overdue pending occurrence as missed and creates the next one if none is pending. Safe to call repeatedly. */
    public static void ensureNextOccurrence(AppDatabase db, Task task) {
        db.runInTransaction(() -> { // transaction so the worker and a completion can't both insert
            TaskOccurrenceDao dao = db.taskOccurrenceDao();
            LocalDate today = LocalDate.now();
            TaskOccurrence latest = dao.getMostRecent(task.id);

            if (latest == null) {
                dao.insert(pending(task.id, task.recurrenceStartDate));
                return;
            }

            if (latest.status == OccurrenceStatus.PENDING) {
                if (!latest.scheduledDate.isBefore(today)) return; // still upcoming or due today
                dao.markMissed(latest.id);
                latest.status = OccurrenceStatus.MISSED;
            }

            if (dao.countPendingForTask(task.id) > 0) return;
            dao.insert(pending(task.id, computeNextDate(task, latest, today)));
        });
    }

    private static LocalDate computeNextDate(Task task, TaskOccurrence latest, LocalDate today) {
        if (task.scheduleMode == ScheduleMode.FIXED) {
            // Next slot on the fixed calendar that's after both today and the occurrence being closed out.
            // The second condition matters when you complete one early: without it you'd get a duplicate
            // of the date you just completed.
            LocalDate floor = latest.scheduledDate.isAfter(today) ? latest.scheduledDate : today;
            LocalDate candidate = task.recurrenceStartDate;
            while (!candidate.isAfter(floor)) {
                candidate = addInterval(candidate, task.recurrenceInterval, task.recurrenceUnit);
            }
            return candidate;
        }
        // FLOATING: anchor to completion, or the scheduled date if missed
        LocalDate anchor = latest.status == OccurrenceStatus.COMPLETED
                ? latest.completedAt.toLocalDate()
                : latest.scheduledDate;
        return addInterval(anchor, task.recurrenceInterval, task.recurrenceUnit);
    }

    private static LocalDate addInterval(LocalDate date, int interval, RecurrenceUnit unit) {
        switch (unit) {
            case DAY:   return date.plusDays(interval);
            case WEEK:  return date.plusWeeks(interval);
            case MONTH: return date.plusMonths(interval);
            default:    throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    private static TaskOccurrence pending(long taskId, LocalDate scheduledDate) {
        TaskOccurrence occ = new TaskOccurrence();
        occ.taskId = taskId;
        occ.scheduledDate = scheduledDate;
        occ.status = OccurrenceStatus.PENDING;
        return occ;
    }
}
