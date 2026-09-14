package kpn.projects.notetoself.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import kpn.projects.notetoself.R;
import kpn.projects.notetoself.adapters.CalendarDayAdapter;
import kpn.projects.notetoself.adapters.CalendarTaskAdapter;
import kpn.projects.notetoself.notifications.NotificationRefreshReceiver;

public class CalendarFragment extends Fragment implements CalendarDayAdapter.Listener, CalendarTaskAdapter.Listener{

    private static final DateTimeFormatter MONTH_YEAR_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter SELECTED_DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, d MMM");

    private CalendarViewModel viewModel;
    private CalendarDayAdapter dayAdapter;
    private CalendarTaskAdapter taskAdapter;

    private TextView textMonthYear;
    private TextView textSelectedDate;
    private TextView textEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        textMonthYear = view.findViewById(R.id.text_month_year);
        textSelectedDate = view.findViewById(R.id.text_selected_date);
        textEmptyState = view.findViewById(R.id.text_empty_state);

        view.findViewById(R.id.button_prev_month).setOnClickListener(v -> viewModel.goToPreviousMonth());
        view.findViewById(R.id.button_next_month).setOnClickListener(v -> viewModel.goToNextMonth());

        setupWeekdayHeader(view);

        dayAdapter = new CalendarDayAdapter(this);
        RecyclerView recyclerDays = view.findViewById(R.id.recycler_days);
        recyclerDays.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        recyclerDays.setNestedScrollingEnabled(false);
        recyclerDays.setAdapter(dayAdapter);

        taskAdapter = new CalendarTaskAdapter(this);
        RecyclerView recyclerTasks = view.findViewById(R.id.recycler_day_tasks);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTasks.setAdapter(taskAdapter);

        ((Button) view.findViewById(R.id.button_add_due_date))
                .setOnClickListener(v -> navigateToAddTask("DUE_DATE"));
        ((Button) view.findViewById(R.id.button_add_regular))
                .setOnClickListener(v -> navigateToAddTask("REGULAR"));

        observeViewModel();
    }

    private void setupWeekdayHeader(View root) {
        int[] ids = {R.id.text_weekday_1, R.id.text_weekday_2, R.id.text_weekday_3,
                R.id.text_weekday_4, R.id.text_weekday_5, R.id.text_weekday_6, R.id.text_weekday_7};
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        for (int i = 0; i < ids.length; i++) {
            ((TextView) root.findViewById(ids[i]))
                    .setText(monday.plusDays(i).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
        }
    }

    private void observeViewModel() {
        viewModel.getCurrentMonth().observe(getViewLifecycleOwner(), month ->
                textMonthYear.setText(month.atDay(1).format(MONTH_YEAR_FORMAT)));

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date ->
                textSelectedDate.setText(date.format(SELECTED_DATE_FORMAT)));

        viewModel.getDayCells().observe(getViewLifecycleOwner(), cells ->
                dayAdapter.submitCells(cells, viewModel.getSelectedDate().getValue()));

        viewModel.getTasksForSelectedDate().observe(getViewLifecycleOwner(), items -> {
            taskAdapter.submitList(items);
            textEmptyState.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void navigateToAddTask(String presetType) {
        LocalDate date = viewModel.getSelectedDate().getValue();
        if (date == null) date = LocalDate.now();

        Bundle args = new Bundle();
        args.putLong("presetDateEpochDay", date.toEpochDay());
        args.putString("presetType", presetType);
        NavHostFragment.findNavController(this).navigate(R.id.action_calendar_to_addEditTask, args);
    }

    @Override
    public void onDayClicked(LocalDate date) {
        viewModel.selectDate(date);
    }

    @Override
    public void onItemClicked(long taskId) {
        Bundle args = new Bundle();
        args.putLong("taskId", taskId);
        NavHostFragment.findNavController(this).navigate(R.id.action_calendar_to_addEditTask, args);
    }

    @Override
    public void onDueDateChecked(long taskId, boolean checked) {
        viewModel.completeSimpleTask(taskId, checked);
        NotificationRefreshReceiver.triggerImmediateRefresh(requireContext());
    }

    @Override
    public void onOccurrenceCompleted(long occurrenceId) {
        viewModel.completeOccurrence(occurrenceId);
    }
}