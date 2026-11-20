package kpn.projects.notetoself.database;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Repository {
    private static volatile Repository INSTANCE; //thread safe
    private final TaskDao taskDao;
    private final TaskOccurrenceDao occurrenceDao;
    private final NotificationConfigDao notificationConfigDao;
    private final ScheduleItemDao scheduleItemDao;
    private final ExecutorService executorService;
    private final AppDatabase db;

    private Repository(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.taskDao = db.taskDao();
        this.occurrenceDao = db.taskOccurrenceDao();
        this.notificationConfigDao = db.notificationConfigDao();
        this.scheduleItemDao = db.scheduleItemDao();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public static Repository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Repository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Repository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    //getters
    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public List<Task> getAllTasksSync() {
        return taskDao.getAllTasksSync();
    }

    public LiveData<Task> getTaskById(long id) {
        return taskDao.getById(id);
    }

    public Task getTaskByIdSync(long id) {
        return taskDao.getByIdSync(id);
    }

    public LiveData<List<Task>> getActiveTodos() {
        return taskDao.getActiveTodos();
    }

    LiveData<List<Task>> getDueDateTasksInRange(LocalDate start, LocalDate end) {
        return taskDao.getDueDateTasksInRange(start, end);
    }

    LiveData<List<Task>> getActiveDueDateTasksInRange(LocalDate start, LocalDate end) {
        return taskDao.getActiveDueDateTasksInRange(start, end);
    }

    public LiveData<List<Task>> getAllRegularTasks() {
        return taskDao.getAllRegularTasks();
    }

    public List<Task> getAllRegularTasksSync() {
        return taskDao.getAllRegularTasksSync();
    }

    public int getTaskCount() {
        return taskDao.countTasksSync();
    }

    public TaskOccurrence getOccurrenceById(long id) {
        return occurrenceDao.getByIdSync(id);
    }

    public List<TaskOccurrence> getAllOccurrences() {
        return occurrenceDao.getAllSync();
    }

    public LiveData<List<TaskOccurrence>> getUncompletedOccurrencesForDay(LocalDate day) {
        return occurrenceDao.getUncompletedForDay(day);
    }

    public LiveData<List<TaskOccurrence>> getOccurrenceHistoryForTask(long taskId) {
        return occurrenceDao.getHistoryForTask(taskId);
    }

    public TaskOccurrence getMostRecentOccurrenceForTask(long taskId) {
        return occurrenceDao.getMostRecent(taskId);
    }

    public List<NotificationConfig> getAllNotificationConfigs() {
        return notificationConfigDao.getAllConfigsSync();
    }

    public LiveData<NotificationConfig> getNotificationConfigForTask(long taskId) {
        return notificationConfigDao.getForTask(taskId);
    }

    public NotificationConfig getNotificationConfigForTaskSync(long taskId) {
        return notificationConfigDao.getForTaskSync(taskId);
    }

    public LiveData<List<NotificationConfig>> getActiveNotificationConfigs() {
        return notificationConfigDao.getActiveConfigs(LocalDateTime.now());
    }

    public List<NotificationConfig> getActiveNotificationConfigsSync() {
        return notificationConfigDao.getActiveConfigsSync(LocalDateTime.now());
    }
    public LiveData<List<ScheduleItem>> getAllScheduleItems(){
        return scheduleItemDao.getAll();
    }
    public LiveData<ScheduleItem> getScheduleItemById(long id){
        return scheduleItemDao.getById(id);
    }
}
