package kpn.projects.notetoself.debug;

import android.content.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;

import kpn.projects.notetoself.AppDatabase;
import kpn.projects.notetoself.enums.OccurrenceStatus;
import kpn.projects.notetoself.enums.RecurrenceUnit;
import kpn.projects.notetoself.enums.ScheduleMode;
import kpn.projects.notetoself.tasks.Task;
import kpn.projects.notetoself.enums.TaskType;

public class DebugSeeder {

    public static void seedIfEmpty(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);

        Executors.newSingleThreadExecutor().execute(() -> {
            if (db.taskDao().countTasksSync() > 0) return; // already seeded, don't duplicate

            LocalDateTime now = LocalDateTime.now();

            // Due-date tasks
            db.taskDao().insertTask(dueDateTask(
                    "Assignment due", "Finish the report", now.withMonth(9).withDayOfMonth(23), now));
            db.taskDao().insertTask(dueDateTask(
                    "Submit tax forms", null, now.plusDays(5), now));

            // Plain todos
            db.taskDao().insertTask(todoTask("Paint the room", "Living room, two coats", now));
            db.taskDao().insertTask(todoTask("Fix the fence", null, now));
            db.taskDao().insertTask(todoTask("Read that book", "The one on the shelf", now));

            // Regular (recurring) task — fixed schedule, weekly
            long tidyRoomId = db.taskDao().insertTask(regularTask(
                    "Tidy room", "Every Monday", 1, RecurrenceUnit.WEEK,
                    ScheduleMode.FIXED, LocalDate.now(), now));

            // Give it today's occurrence so it actually shows up on Home
            db.taskOccurrenceDao().insert(pendingOccurrence(tidyRoomId, LocalDate.now()));
        });
    }

    private static Task dueDateTask(String title, String description, LocalDateTime dueDate, LocalDateTime now) {
        Task task = new Task();
        task.title = title;
        task.description = description;
        task.type = TaskType.DUE_DATE;
        task.dueDate = dueDate;
        task.completed = false;
        task.createdAt = now;
        return task;
    }

    private static Task todoTask(String title, String description, LocalDateTime now) {
        Task task = new Task();
        task.title = title;
        task.description = description;
        task.type = TaskType.TODO;
        task.completed = false;
        task.createdAt = now;
        return task;
    }

    private static Task regularTask(String title, String description, int interval, RecurrenceUnit unit,
                                    ScheduleMode mode, LocalDate startDate, LocalDateTime now) {
        Task task = new Task();
        task.title = title;
        task.description = description;
        task.type = TaskType.REGULAR;
        task.recurrenceInterval = interval;
        task.recurrenceUnit = unit;
        task.scheduleMode = mode;
        task.recurrenceStartDate = startDate;
        task.createdAt = now;
        return task;
    }

    private static kpn.projects.notetoself.tasks.TaskOccurrence pendingOccurrence(long taskId, LocalDate scheduledDate) {
        kpn.projects.notetoself.tasks.TaskOccurrence occ = new kpn.projects.notetoself.tasks.TaskOccurrence();
        occ.taskId = taskId;
        occ.scheduledDate = scheduledDate;
        occ.status = OccurrenceStatus.PENDING;
        return occ;
    }
}
