package kpn.projects.notetoself.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import kpn.projects.notetoself.R;
import kpn.projects.notetoself.adapters.RegularTaskSectionAdapter;
import kpn.projects.notetoself.adapters.TaskSectionAdapter;
import kpn.projects.notetoself.notifications.NotificationRefreshReceiver;
import kpn.projects.notetoself.tasks.Task;


public class HomeFragment extends Fragment implements TaskSectionAdapter.Listener, RegularTaskSectionAdapter.Listener {

    private TaskViewModel viewModel;
    private TaskSectionAdapter dueDateAdapter;
    private TaskSectionAdapter todoAdapter;
    private RegularTaskSectionAdapter regularAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        dueDateAdapter = new TaskSectionAdapter(getString(R.string.section_due_this_week), this);
        todoAdapter = new TaskSectionAdapter(getString(R.string.section_todos), this);
        regularAdapter = new RegularTaskSectionAdapter(getString(R.string.section_regular), this);

        ConcatAdapter concatAdapter = new ConcatAdapter(dueDateAdapter, regularAdapter, todoAdapter);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_home);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(concatAdapter);

        observeViewModel();

        FloatingActionButton fab = view.findViewById(R.id.fab_add_task);
        fab.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_home_to_addEditTask));
    }

    private void observeViewModel() {
        viewModel.getDueDateTasksUpcoming().observe(getViewLifecycleOwner(),
                dueDateAdapter::submitList);

        viewModel.getActiveTodos().observe(getViewLifecycleOwner(),
                todoAdapter::submitList);

        viewModel.getRegularOccurrencesUpcoming().observe(getViewLifecycleOwner(),
                regularAdapter::submitList);
    }

    @Override
    public void onTaskChecked(Task task, boolean checked) {
        Runnable refreshNotifications = () -> NotificationRefreshReceiver.triggerImmediateRefresh(requireContext());

        if (checked) {
            viewModel.completeSimpleTask(task.id, refreshNotifications);
        } else {
            task.completed = false;
            viewModel.updateTask(task, refreshNotifications);
        }
    }

    @Override
    public void onTaskClicked(Task task) {
        Bundle args = new Bundle();
        args.putLong("taskId", task.id);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_home_to_addEditTask, args);
    }

    @Override
    public void onOccurrenceChecked(long occurrenceId, boolean checked) {
        if (checked) {
            viewModel.completeOccurrence(occurrenceId);
        }
    }

    @Override
    public void onOccurrenceClicked(long taskId) {
        Bundle args = new Bundle();
        args.putLong("taskId", taskId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_home_to_addEditTask, args);
    }
}