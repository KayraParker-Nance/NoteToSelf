package kpn.projects.notetoself.tasks;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kpn.projects.notetoself.AppDatabase;
import kpn.projects.notetoself.enums.OccurrenceStatus;
import kpn.projects.notetoself.enums.TaskType;

public class TaskRepository {
    private static volatile TaskRepository instance;

    private final TaskDao taskDao;
    private final TaskOccurrenceDao occurrenceDao;
    private final NotificationConfigDao notificationConfigDao;
    private final ExecutorService executor;

    private TaskRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.taskDao = db.taskDao();
        this.occurrenceDao = db.taskOccurrenceDao();
        this.notificationConfigDao = db.notificationConfigDao();
        this.executor = Executors.newFixedThreadPool(2);
    }

    public static TaskRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (TaskRepository.class) {
                if (instance == null) {
                    instance = new TaskRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    // getters

    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getActiveTodos() {
        return taskDao.getActiveTodos();
    }

    public LiveData<List<Task>> getUpcomingDueDateTasks() {
        LocalDate today = LocalDate.now();
        return taskDao.getDueDateTasksInRange(today, today.plusDays(7));
    }

    public LiveData<List<TaskOccurrence>> getPendingOccurrencesForDay(LocalDate day) {
        return occurrenceDao.getPendingForDay(day);
    }

    public LiveData<List<TaskOccurrence>> getHistoryForTask(long taskId) {
        return occurrenceDao.getHistoryForTask(taskId);
    }

    public LiveData<NotificationConfig> getNotificationConfig(long taskId) {
        return notificationConfigDao.getForTask(taskId);
    }

    // setters

    public void addTask(Task task, NotificationConfig config, Runnable onComplete) {
        executor.execute(() -> {
            long taskId = taskDao.insertTask(task);

            if (config != null) {
                config.taskId = taskId;
                notificationConfigDao.insert(config);
            }

            if (task.type == TaskType.REGULAR) {
                TaskOccurrence first = new TaskOccurrence();
                first.taskId = taskId;
                first.scheduledDate = task.recurrenceStartDate;
                first.status = OccurrenceStatus.PENDING;
                occurrenceDao.insert(first);
            }
            if (onComplete != null) onComplete.run();
        });
    }

    public void updateTask(Task task, Runnable onComplete) {
        executor.execute(() -> {
            taskDao.updateTask(task);
            if (onComplete != null) onComplete.run();
        });
    }

    public void deleteTask(Task task) {
        executor.execute(() -> taskDao.deleteTask(task));
    }

    public void completeSimpleTask(long taskId, Runnable onComplete) {
        executor.execute(() -> {
            Task task = taskDao.getByIdSync(taskId);
            if (task != null) {
                task.completed = true;
                taskDao.updateTask(task);
            }
            if (onComplete != null) onComplete.run();
        });
    }

    public void completeOccurrence(long occurrenceId) {
        executor.execute(() ->
                occurrenceDao.markCompleted(occurrenceId, LocalDateTime.now()));
    }

    public void updateNotificationConfig(NotificationConfig config, Runnable onComplete) {
        executor.execute(() -> {
            notificationConfigDao.insert(config); // REPLACE strategy handles upsert
            if (onComplete != null) onComplete.run();
        });
    }

    public LiveData<List<TaskOccurrenceDao.TaskOccurrenceWithTitle>> getUpcomingRegularOccurrences() {
        return occurrenceDao.getPendingUpTo(LocalDate.now().plusDays(7));
    }

    public LiveData<Task> getTask(long taskId) {
        return taskDao.getTaskByIdLive(taskId);
    }

    public LiveData<List<Task>> getDueDateTasksInRange(LocalDate start, LocalDate end) {
        return taskDao.getDueDateTasksInRange(start, end);
    }

    public LiveData<List<TaskOccurrenceDao.TaskOccurrenceWithTitle>> getOccurrencesInRange(LocalDate start, LocalDate end) {
        return occurrenceDao.getOccurrencesInRange(start, end);
    }

    public void updateTaskCompleted(long taskId, boolean completed) {
        executor.execute(() -> {
            Task task = taskDao.getByIdSync(taskId);
            if (task != null) {
                task.completed = completed;
                taskDao.updateTask(task);
            }
        });
    }
}
