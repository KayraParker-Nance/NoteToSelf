package kpn.projects.notetoself.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import kpn.projects.notetoself.R;
import kpn.projects.notetoself.enums.TaskColour;
import kpn.projects.notetoself.schedule.Class;

public class AddEditClassFragment extends Fragment {

    private static final long NO_CLASS_ID = -1L;
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private ScheduleViewModel viewModel;
    private long classId = NO_CLASS_ID;
    private boolean isEditMode;
    private boolean fieldsPopulated = false;
    private Class existingClass;

    private EditText inputTitle;
    private EditText inputLocation;
    private Spinner spinnerDay;
    private Button buttonPickStartTime;
    private TextView textStartTimeValue;
    private Button buttonPickEndTime;
    private TextView textEndTimeValue;
    private LinearLayout layoutColorSwatches;
    private Button buttonSave;
    private Button buttonDelete;

    private LocalTime selectedStartTime = LocalTime.of(9, 0);
    private LocalTime selectedEndTime = LocalTime.of(10, 0);
    private TaskColour selectedColor = TaskColour.NONE;
    private final List<View> colorSwatchViews = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_class, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);

        classId = getArguments() != null ? getArguments().getLong("classId", NO_CLASS_ID) : NO_CLASS_ID;
        isEditMode = classId != NO_CLASS_ID;

        bindViews(view);
        setupDaySpinner();
        setupTimePickers();
        setupColorPicker();
        setupSaveAndDelete();

        if (isEditMode) {
            buttonDelete.setVisibility(View.VISIBLE);
            observeExistingClass();
        }
    }

    private void bindViews(View view) {
        inputTitle = view.findViewById(R.id.input_class_title);
        inputLocation = view.findViewById(R.id.input_class_location);
        spinnerDay = view.findViewById(R.id.spinner_day);
        buttonPickStartTime = view.findViewById(R.id.button_pick_start_time);
        textStartTimeValue = view.findViewById(R.id.text_start_time_value);
        buttonPickEndTime = view.findViewById(R.id.button_pick_end_time);
        textEndTimeValue = view.findViewById(R.id.text_end_time_value);
        layoutColorSwatches = view.findViewById(R.id.layout_color_swatches);
        buttonSave = view.findViewById(R.id.button_save_class);
        buttonDelete = view.findViewById(R.id.button_delete_class);
    }

    private void setupDaySpinner() {
        ArrayAdapter<DayOfWeek> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, DayOfWeek.values());
        spinnerDay.setAdapter(adapter);
    }

    private void setupTimePickers() {
        textStartTimeValue.setText(selectedStartTime.format(DISPLAY_TIME_FORMAT));
        textEndTimeValue.setText(selectedEndTime.format(DISPLAY_TIME_FORMAT));

        buttonPickStartTime.setOnClickListener(v -> showTimePicker(selectedStartTime, time -> {
            selectedStartTime = time;
            textStartTimeValue.setText(time.format(DISPLAY_TIME_FORMAT));
        }));

        buttonPickEndTime.setOnClickListener(v -> showTimePicker(selectedEndTime, time -> {
            selectedEndTime = time;
            textEndTimeValue.setText(time.format(DISPLAY_TIME_FORMAT));
        }));
    }

    private interface OnTimeChosen {
        void onChosen(LocalTime time);
    }

    private void showTimePicker(LocalTime initial, OnTimeChosen callback) {
        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> callback.onChosen(LocalTime.of(hourOfDay, minute)),
                initial.getHour(), initial.getMinute(), true);
        dialog.show();
    }

    private void observeExistingClass() {
        viewModel.getClass(classId).observe(getViewLifecycleOwner(), classEntry -> {
            if (classEntry == null || fieldsPopulated) return;
            fieldsPopulated = true;
            existingClass = classEntry;
            populateFields(classEntry);
        });
    }

    private void populateFields(Class entry) {
        inputTitle.setText(entry.title);
        inputLocation.setText(entry.location);
        selectedColor = entry.color != null ? entry.color : TaskColour.NONE;
        updateSwatchSelectionUi();

        @SuppressWarnings("unchecked")
        ArrayAdapter<DayOfWeek> adapter = (ArrayAdapter<DayOfWeek>) spinnerDay.getAdapter();
        spinnerDay.setSelection(adapter.getPosition(entry.dayOfWeek));

        selectedStartTime = entry.startTime;
        selectedEndTime = entry.endTime;
        textStartTimeValue.setText(selectedStartTime.format(DISPLAY_TIME_FORMAT));
        textEndTimeValue.setText(selectedEndTime.format(DISPLAY_TIME_FORMAT));
    }

    private void setupSaveAndDelete() {
        buttonSave.setOnClickListener(v -> onSaveClicked());
        buttonDelete.setOnClickListener(v -> {
            if (existingClass != null) {
                viewModel.deleteClass(existingClass);
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

        Class entry = isEditMode ? existingClass : new Class();
        entry.title = title;
        entry.location = inputLocation.getText().toString().trim();
        entry.dayOfWeek = (DayOfWeek) spinnerDay.getSelectedItem();
        entry.startTime = selectedStartTime;
        entry.endTime = selectedEndTime;
        entry.color = selectedColor;

        if (isEditMode) {
            viewModel.updateClass(entry, this::onSaveComplete);
        } else {
            viewModel.addClass(entry, this::onSaveComplete);
        }
    }

    private void onSaveComplete() {
        requireActivity().runOnUiThread(() ->
                NavHostFragment.findNavController(AddEditClassFragment.this).popBackStack());
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