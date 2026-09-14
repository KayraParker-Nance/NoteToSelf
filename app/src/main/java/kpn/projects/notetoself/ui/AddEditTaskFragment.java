package kpn.projects.notetoself.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import kpn.projects.notetoself.R;
import kpn.projects.notetoself.enums.TaskColour;
import kpn.projects.notetoself.notifications.NotificationRefreshReceiver;
import kpn.projects.notetoself.tasks.NotificationConfig;
import kpn.projects.notetoself.enums.RecurrenceUnit;
import kpn.projects.notetoself.enums.ScheduleMode;
import kpn.projects.notetoself.tasks.Task;
import kpn.projects.notetoself.enums.TaskType;

public class AddEditTaskFragment extends Fragment {

    private static final long NO_TASK_ID = -1L;
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");

    private TaskViewModel viewModel;
    private long taskId = NO_TASK_ID;
    private boolean isEditMode;
    private boolean fieldsPopulated = false;
    private Task existingTask;

    private EditText inputTitle;
    private EditText inputDescription;
    private RadioGroup radioGroupType;
    private View groupDueDate;
    private View groupRegular;
    private TextView textDueDateValue;
    private Button buttonPickDueDate;
    private EditText inputInterval;
    private Spinner spinnerRecurrenceUnit;
    private RadioGroup radioGroupScheduleMode;
    private Button buttonPickStartDate;
    private TextView textStartDateValue;
    private SwitchMaterial switchSticky;
    private Button buttonPickShowAfter;
    private TextView textShowAfterValue;
    private EditText inputRepeatHours;
    private Button buttonSave;
    private Button buttonDelete;

    private LocalDate selectedDueDate;
    private LocalDate selectedStartDate = LocalDate.now();
    private LocalDate selectedShowAfterDate;

    private LinearLayout layoutColorSwatches;
    private TaskColour selectedColor = TaskColour.NONE;
    private final List<View> colorSwatchViews = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        taskId = getArguments() != null ? getArguments().getLong("taskId", NO_TASK_ID) : NO_TASK_ID;
        isEditMode = taskId != NO_TASK_ID;

        bindViews(view);
        setupTypeGroup();
        setupSpinner();
        setupDatePickers();
        setupColorPicker();
        setupSaveAndDelete();

        if (isEditMode) {
            buttonDelete.setVisibility(View.VISIBLE);
            observeExistingTask();
        } else {
            applyPresetsOrDefault();
        }
    }

    private void bindViews(View view) {
        inputTitle = view.findViewById(R.id.input_title);
        inputDescription = view.findViewById(R.id.input_description);
        radioGroupType = view.findViewById(R.id.radio_group_type);
        groupDueDate = view.findViewById(R.id.group_due_date);
        groupRegular = view.findViewById(R.id.group_regular);
        textDueDateValue = view.findViewById(R.id.text_due_date_value);
        buttonPickDueDate = view.findViewById(R.id.button_pick_due_date);
        inputInterval = view.findViewById(R.id.input_interval);
        spinnerRecurrenceUnit = view.findViewById(R.id.spinner_recurrence_unit);
        radioGroupScheduleMode = view.findViewById(R.id.radio_group_schedule_mode);
        buttonPickStartDate = view.findViewById(R.id.button_pick_start_date);
        textStartDateValue = view.findViewById(R.id.text_start_date_value);
        switchSticky = view.findViewById(R.id.switch_sticky);
        buttonPickShowAfter = view.findViewById(R.id.button_pick_show_after);
        textShowAfterValue = view.findViewById(R.id.text_show_after_value);
        inputRepeatHours = view.findViewById(R.id.input_repeat_hours);
        buttonSave = view.findViewById(R.id.button_save);
        buttonDelete = view.findViewById(R.id.button_delete);
        layoutColorSwatches = view.findViewById(R.id.layout_color_swatches);
    }

    private void setupTypeGroup() {
        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            groupDueDate.setVisibility(checkedId == R.id.radio_due_date ? View.VISIBLE : View.GONE);
            groupRegular.setVisibility(checkedId == R.id.radio_regular ? View.VISIBLE : View.GONE);
        });
    }

    private void setupSpinner() {
        ArrayAdapter<RecurrenceUnit> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, RecurrenceUnit.values());
        spinnerRecurrenceUnit.setAdapter(adapter);
    }

    private void setupDatePickers() {
        buttonPickDueDate.setOnClickListener(v -> showDatePicker(
                selectedDueDate != null ? selectedDueDate : LocalDate.now(),
                date -> {
                    selectedDueDate = date;
                    textDueDateValue.setText(date.format(DISPLAY_FORMAT));
                }));

        buttonPickStartDate.setOnClickListener(v -> showDatePicker(
                selectedStartDate,
                date -> {
                    selectedStartDate = date;
                    textStartDateValue.setText(date.format(DISPLAY_FORMAT));
                }));
        textStartDateValue.setText(selectedStartDate.format(DISPLAY_FORMAT)); // default shown immediately

        buttonPickShowAfter.setOnClickListener(v -> showDatePicker(
                selectedShowAfterDate != null ? selectedShowAfterDate : LocalDate.now(),
                date -> {
                    selectedShowAfterDate = date;
                    textShowAfterValue.setText(date.format(DISPLAY_FORMAT));
                }));
    }

    private interface OnDateChosen {
        void onChosen(LocalDate date);
    }

    private void showDatePicker(LocalDate initial, OnDateChosen callback) {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) ->
                        callback.onChosen(LocalDate.of(year, month + 1, dayOfMonth)),
                initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth());
        dialog.show();
    }

    private void observeExistingTask() {
        viewModel.getTask(taskId).observe(getViewLifecycleOwner(), task -> {
            if (task == null || fieldsPopulated) return;
            fieldsPopulated = true;
            existingTask = task;
            populateFields(task);
        });

        viewModel.getNotificationConfig(taskId).observe(getViewLifecycleOwner(), config -> {
            if (config == null) return;
            switchSticky.setChecked(config.stickyEnabled);
            if (config.showAfterDate != null) {
                selectedShowAfterDate = config.showAfterDate.toLocalDate();
                textShowAfterValue.setText(selectedShowAfterDate.format(DISPLAY_FORMAT));
            }
            if (config.repeatIntervalHours > 0) {
                inputRepeatHours.setText(String.valueOf(config.repeatIntervalHours));
            }
        });
    }

    private void populateFields(Task task) {
        inputTitle.setText(task.title);
        inputDescription.setText(task.description);
        selectedColor = task.color != null ? task.color : TaskColour.NONE;
        updateSwatchSelectionUi();

        switch (task.type) {
            case DUE_DATE:
                radioGroupType.check(R.id.radio_due_date);
                if (task.dueDate != null) {
                    selectedDueDate = task.dueDate.toLocalDate();
                    textDueDateValue.setText(selectedDueDate.format(DISPLAY_FORMAT));
                }
                break;
            case REGULAR:
                radioGroupType.check(R.id.radio_regular);
                inputInterval.setText(String.valueOf(task.recurrenceInterval));
                @SuppressWarnings("unchecked")
                ArrayAdapter<RecurrenceUnit> adapter = (ArrayAdapter<RecurrenceUnit>) spinnerRecurrenceUnit.getAdapter();
                spinnerRecurrenceUnit.setSelection(adapter.getPosition(task.recurrenceUnit));
                radioGroupScheduleMode.check(
                        task.scheduleMode == ScheduleMode.FIXED ? R.id.radio_fixed : R.id.radio_floating);
                if (task.recurrenceStartDate != null) {
                    selectedStartDate = task.recurrenceStartDate;
                    textStartDateValue.setText(selectedStartDate.format(DISPLAY_FORMAT));
                }
                break;
            case TODO:
            default:
                radioGroupType.check(R.id.radio_todo);
                break;
        }
    }

    private void setupSaveAndDelete() {
        buttonSave.setOnClickListener(v -> onSaveClicked());
        buttonDelete.setOnClickListener(v -> {
            if (existingTask != null) {
                viewModel.deleteTask(existingTask);
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
    }

    private void onSaveClicked() {
        String title = inputTitle.getText().toString().trim();
        if (title.isEmpty()) {
            inputTitle.setError(getString(R.string.error_title_required));
            return;
        }

        Task task = isEditMode ? existingTask : new Task();
        task.title = title;
        task.description = inputDescription.getText().toString().trim();
        task.createdAt = isEditMode ? task.createdAt : LocalDateTime.now();
        task.color = selectedColor;

        int checkedTypeId = radioGroupType.getCheckedRadioButtonId();
        if (checkedTypeId == R.id.radio_due_date) {
            task.type = TaskType.DUE_DATE;
            task.dueDate = selectedDueDate != null ? selectedDueDate.atStartOfDay() : LocalDateTime.now();
        } else if (checkedTypeId == R.id.radio_regular) {
            task.type = TaskType.REGULAR;
            task.recurrenceInterval = parseIntervalOrDefault();
            task.recurrenceUnit = (RecurrenceUnit) spinnerRecurrenceUnit.getSelectedItem();
            task.scheduleMode = radioGroupScheduleMode.getCheckedRadioButtonId() == R.id.radio_fixed
                    ? ScheduleMode.FIXED : ScheduleMode.FLOATING;
            task.recurrenceStartDate = selectedStartDate;
        } else {
            task.type = TaskType.TODO;
        }

        NotificationConfig config = new NotificationConfig();
        config.stickyEnabled = switchSticky.isChecked();
        config.enabled = switchSticky.isChecked();
        config.showAfterDate = selectedShowAfterDate != null ? selectedShowAfterDate.atStartOfDay() : null;
        config.repeatIntervalHours = parseRepeatHoursOrDefault();

        if (isEditMode) {
            config.taskId = taskId;
            viewModel.updateTask(task, () ->
                    viewModel.updateNotificationConfig(config, this::onSaveComplete));
        } else {
            viewModel.addTask(task, config, this::onSaveComplete);
        }
    }

    private void onSaveComplete() {
        NotificationRefreshReceiver.triggerImmediateRefresh(requireContext());
        requireActivity().runOnUiThread(() ->
                NavHostFragment.findNavController(AddEditTaskFragment.this).popBackStack());
    }

    private int parseIntervalOrDefault() {
        String text = inputInterval.getText().toString().trim();
        try {
            int value = Integer.parseInt(text);
            return value > 0 ? value : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private int parseRepeatHoursOrDefault() {
        String text = inputRepeatHours.getText().toString().trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void applyPresetsOrDefault() {
        long presetEpochDay = getArguments() != null ? getArguments().getLong("presetDateEpochDay", -1L) : -1L;
        String presetType = getArguments() != null ? getArguments().getString("presetType") : null;
        LocalDate presetDate = presetEpochDay != -1L ? LocalDate.ofEpochDay(presetEpochDay) : null;

        if ("REGULAR".equals(presetType)) {
            radioGroupType.check(R.id.radio_regular);
            if (presetDate != null) {
                selectedStartDate = presetDate;
                textStartDateValue.setText(presetDate.format(DISPLAY_FORMAT));
            }
        } else if ("DUE_DATE".equals(presetType)) {
            radioGroupType.check(R.id.radio_due_date);
            if (presetDate != null) {
                selectedDueDate = presetDate;
                textDueDateValue.setText(presetDate.format(DISPLAY_FORMAT));
            }
        } else {
            radioGroupType.check(R.id.radio_todo);
        }
    }

    private void setupColorPicker() {
        layoutColorSwatches.removeAllViews();
        colorSwatchViews.clear();

        float density = getResources().getDisplayMetrics().density;
        int size = (int) (36 * density);
        int margin = (int) (8 * density);

        for (TaskColour color : TaskColour.values()) {
            View swatch = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMarginEnd(margin);
            swatch.setLayoutParams(params);

            if (color == TaskColour.NONE) {
                swatch.setBackgroundResource(R.drawable.bg_colour_swatch_none);
            } else {
                swatch.setBackgroundResource(R.drawable.bg_colour_swatch);
                swatch.getBackground().setTint(ContextCompat.getColor(requireContext(), color.getColorRes()));
            }

            swatch.setTag(color);
            swatch.setOnClickListener(v -> {
                selectedColor = color;
                updateSwatchSelectionUi();
            });
            layoutColorSwatches.addView(swatch);
            colorSwatchViews.add(swatch);
        }

        updateSwatchSelectionUi();
    }

    private void updateSwatchSelectionUi() {
        for (View swatch : colorSwatchViews) {
            boolean isSelected = swatch.getTag() == selectedColor;
            swatch.setScaleX(isSelected ? 1.2f : 1f);
            swatch.setScaleY(isSelected ? 1.2f : 1f);
            swatch.setAlpha(isSelected ? 1f : 0.7f);
        }
    }
}