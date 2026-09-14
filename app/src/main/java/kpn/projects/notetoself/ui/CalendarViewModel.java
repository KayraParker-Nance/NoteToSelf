package kpn.projects.notetoself.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kpn.projects.notetoself.adapters.CalendarDayAdapter;
import kpn.projects.notetoself.adapters.CalendarTaskAdapter;
import kpn.projects.notetoself.tasks.Task;
import kpn.projects.notetoself.tasks.TaskOccurrenceDao;
import kpn.projects.notetoself.tasks.TaskRepository;

public class CalendarViewModel extends AndroidViewModel {
    private final TaskRepository repository;

    private final MutableLiveData<YearMonth> currentMonth = new MutableLiveData<>(YearMonth.now());
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>(LocalDate.now());

    private final LiveData<List<Task>> dueDateTasksInMonth;
    private final LiveData<List<TaskOccurrenceDao.TaskOccurrenceWithTitle>> occurrencesInMonth;

    private final MediatorLiveData<List<CalendarDayAdapter.DayCell>> dayCells = new MediatorLiveData<>();
    private final MediatorLiveData<List<CalendarTaskAdapter.Item>> tasksForSelectedDate = new MediatorLiveData<>();

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        repository = TaskRepository.getInstance(application);

        dueDateTasksInMonth = Transformations.switchMap(currentMonth, month ->
                repository.getDueDateTasksInRange(month.atDay(1), month.atEndOfMonth()));
        occurrencesInMonth = Transformations.switchMap(currentMonth, month ->
                repository.getOccurrencesInRange(month.atDay(1), month.atEndOfMonth()));

        dayCells.addSource(currentMonth, m -> recomputeDayCells());
        dayCells.addSource(selectedDate, d -> recomputeDayCells());
        dayCells.addSource(dueDateTasksInMonth, t -> recomputeDayCells());
        dayCells.addSource(occurrencesInMonth, o -> recomputeDayCells());

        tasksForSelectedDate.addSource(selectedDate, d -> recomputeSelectedDayTasks());
        tasksForSelectedDate.addSource(dueDateTasksInMonth, t -> recomputeSelectedDayTasks());
        tasksForSelectedDate.addSource(occurrencesInMonth, o -> recomputeSelectedDayTasks());
    }

    public LiveData<YearMonth> getCurrentMonth() { return currentMonth; }
    public LiveData<LocalDate> getSelectedDate() { return selectedDate; }
    public LiveData<List<CalendarDayAdapter.DayCell>> getDayCells() { return dayCells; }
    public LiveData<List<CalendarTaskAdapter.Item>> getTasksForSelectedDate() { return tasksForSelectedDate; }

    public void goToPreviousMonth() {
        YearMonth month = currentMonth.getValue();
        currentMonth.setValue((month != null ? month : YearMonth.now()).minusMonths(1));
    }

    public void goToNextMonth() {
        YearMonth month = currentMonth.getValue();
        currentMonth.setValue((month != null ? month : YearMonth.now()).plusMonths(1));
    }

    public void selectDate(LocalDate date) {
        selectedDate.setValue(date);
        YearMonth month = currentMonth.getValue();
        YearMonth dateMonth = YearMonth.from(date);
        if (month == null || !month.equals(dateMonth)) {
            currentMonth.setValue(dateMonth);
        }
    }

    public void completeSimpleTask(long taskId, boolean checked) {
        repository.updateTaskCompleted(taskId, checked);
    }

    public void completeOccurrence(long occurrenceId) {
        repository.completeOccurrence(occurrenceId);
    }

    private void recomputeDayCells() {
        YearMonth month = currentMonth.getValue();
        if (month == null) return;

        Map<LocalDate, boolean[]> markers = buildMarkers();

        List<CalendarDayAdapter.DayCell> cells = new ArrayList<>();
        LocalDate firstOfMonth = month.atDay(1);
        int leadingDays = firstOfMonth.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        LocalDate cursor = firstOfMonth.minusDays(leadingDays);

        for (int i = 0; i < 42; i++) {
            boolean inMonth = YearMonth.from(cursor).equals(month);
            CalendarDayAdapter.DayCell cell = new CalendarDayAdapter.DayCell(cursor, inMonth);
            boolean[] flags = markers.get(cursor);
            if (flags != null) {
                cell.hasDueDate = flags[0];
                cell.hasRegular = flags[1];
            }
            cells.add(cell);
            cursor = cursor.plusDays(1);
        }

        dayCells.setValue(cells);
    }

    private Map<LocalDate, boolean[]> buildMarkers() {
        Map<LocalDate, boolean[]> markers = new HashMap<>();

        List<Task> dueDateTasks = dueDateTasksInMonth.getValue();
        if (dueDateTasks != null) {
            for (Task t : dueDateTasks) {
                if (t.dueDate == null) continue;
                markers.computeIfAbsent(t.dueDate.toLocalDate(), k -> new boolean[2])[0] = true;
            }
        }

        List<TaskOccurrenceDao.TaskOccurrenceWithTitle> occurrences = occurrencesInMonth.getValue();
        if (occurrences != null) {
            for (TaskOccurrenceDao.TaskOccurrenceWithTitle o : occurrences) {
                markers.computeIfAbsent(o.scheduledDate, k -> new boolean[2])[1] = true;
            }
        }

        return markers;
    }

    private void recomputeSelectedDayTasks() {
        LocalDate date = selectedDate.getValue();
        if (date == null) return;

        List<CalendarTaskAdapter.Item> items = new ArrayList<>();

        List<Task> dueDateTasks = dueDateTasksInMonth.getValue();
        if (dueDateTasks != null) {
            for (Task t : dueDateTasks) {
                if (t.dueDate != null && t.dueDate.toLocalDate().equals(date)) {
                    items.add(new CalendarTaskAdapter.Item(t.id, null, t.title, t.completed, null));
                }
            }
        }

        List<TaskOccurrenceDao.TaskOccurrenceWithTitle> occurrences = occurrencesInMonth.getValue();
        if (occurrences != null) {
            for (TaskOccurrenceDao.TaskOccurrenceWithTitle o : occurrences) {
                if (o.scheduledDate.equals(date)) {
                    items.add(new CalendarTaskAdapter.Item(o.taskId, o.occurrenceId, o.title, false, o.status));
                }
            }
        }

        tasksForSelectedDate.setValue(items);
    }
}
