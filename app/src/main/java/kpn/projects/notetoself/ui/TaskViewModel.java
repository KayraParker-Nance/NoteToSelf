package kpn.projects.notetoself.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import kpn.projects.notetoself.tasks.NotificationConfig;
import kpn.projects.notetoself.tasks.Task;
import kpn.projects.notetoself.tasks.TaskOccurrence;
import kpn.projects.notetoself.tasks.TaskOccurrenceDao;
import kpn.projects.notetoself.tasks.TaskRepository;

public class TaskViewModel extends AndroidViewModel {
    private final TaskRepository repository;
    private final LocalDate today = LocalDate.now();
    private final LiveData<List<Task>> activeTodos;
    private final LiveData<List<Task>> dueDateTasksUpcoming;
    private final LiveData<List<TaskOccurrence>> pendingOccurrencesToday;

    private final LiveData<List<TaskOccurrenceDao.TaskOccurrenceWithTitle>> regularOccurrencesUpcoming;

// constructor:
    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = TaskRepository.getInstance(application);

        activeTodos = repository.getActiveTodos();
        dueDateTasksUpcoming = repository.getUpcomingDueDateTasks();
        pendingOccurrencesToday = repository.getPendingOccurrencesForDay(today);
        regularOccurrencesUpcoming = repository.getUpcomingRegularOccurrences();

    }

    public LiveData<List<Task>> getActiveTodos() {
        return activeTodos;
    }

    public LiveData<List<Task>> getDueDateTasksUpcoming() {
        return dueDateTasksUpcoming;
    }
    public LiveData<List<TaskOccurrence>> getPendingOccurrencesToday() {
        return pendingOccurrencesToday;
    }

    public LiveData<List<TaskOccurrence>> getHistoryForTask(long taskId) {
        return repository.getHistoryForTask(taskId);
    }

    public LiveData<NotificationConfig> getNotificationConfig(long taskId) {
        return repository.getNotificationConfig(taskId);
    }

    public void addTask(Task task, NotificationConfig config, Runnable onComplete) {
        repository.addTask(task, config, onComplete);
    }

    public void updateTask(Task task, Runnable onComplete) {
        repository.updateTask(task, onComplete);
    }

    public void deleteTask(Task task) {
        repository.deleteTask(task);
    }

    public void completeSimpleTask(long taskId, Runnable onComplete) {
        repository.completeSimpleTask(taskId, onComplete);
    }
    public void completeOccurrence(long occurrenceId) {
        repository.completeOccurrence(occurrenceId);
    }

    public void updateNotificationConfig(NotificationConfig config, Runnable onComplete) {
        repository.updateNotificationConfig(config, onComplete);
    }

    public LiveData<List<TaskOccurrenceDao.TaskOccurrenceWithTitle>> getRegularOccurrencesUpcoming() {
        return regularOccurrencesUpcoming;
    }

    public LiveData<Task> getTask(long taskId) {
        return repository.getTask(taskId);
    }
}
