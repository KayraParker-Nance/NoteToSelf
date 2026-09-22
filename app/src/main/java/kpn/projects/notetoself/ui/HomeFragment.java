package kpn.projects.notetoself.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import kpn.projects.notetoself.R;
import kpn.projects.notetoself.adapters.RegularTaskSectionAdapter;
import kpn.projects.notetoself.adapters.TaskSectionAdapter;
import kpn.projects.notetoself.enums.TaskType;
import kpn.projects.notetoself.notifications.NotificationItem;
import kpn.projects.notetoself.notifications.NotificationRefreshReceiver;
import kpn.projects.notetoself.tasks.Task;
import kpn.projects.notetoself.tasks.TaskOccurrenceDao;


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

        new ItemTouchHelper(new SwipeActionCallback(requireContext(), new SwipeActionCallback.Listener() {
            @Override
            public boolean isSwipeable(RecyclerView.ViewHolder holder) {
                return taskFor(holder) != null || occurrenceFor(holder) != null;
            }

            @Override
            public void onSwipedRight(RecyclerView.ViewHolder holder) {
                Task task = taskFor(holder);
                TaskOccurrenceDao.TaskOccurrenceWithTitle occ = occurrenceFor(holder);

                if (task != null) {
                    onTaskChecked(task, true);
                } else if (occ != null) {
                    onOccurrenceChecked(occ.occurrenceId, true);
                }
            }

            @Override
            public void onSwipedLeft(RecyclerView.ViewHolder holder) {
                Task task = taskFor(holder);
                TaskOccurrenceDao.TaskOccurrenceWithTitle occ = occurrenceFor(holder);
                RecyclerView.Adapter<?> adapter = holder.getBindingAdapter();
                int pos = holder.getBindingAdapterPosition();

                if (task != null) {
                    confirmDelete(task.id, false, adapter, pos);
                } else if (occ != null) {
                    confirmDelete(occ.taskId, true, adapter, pos);
                }
            }
        })).attachToRecyclerView(recyclerView);

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
            Context appContext = requireContext().getApplicationContext();
            viewModel.completeOccurrence(occurrenceId,
                    () -> NotificationRefreshReceiver.triggerImmediateRefresh(appContext));
        }
    }

    @Override
    public void onOccurrenceClicked(long taskId) {
        Bundle args = new Bundle();
        args.putLong("taskId", taskId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_home_to_addEditTask, args);
    }

    private Task taskFor(RecyclerView.ViewHolder holder) {
        RecyclerView.Adapter<?> adapter = holder.getBindingAdapter();
        int pos = holder.getBindingAdapterPosition();
        if (pos == RecyclerView.NO_POSITION || !(adapter instanceof TaskSectionAdapter)) return null;
        return ((TaskSectionAdapter) adapter).getTaskAt(pos);
    }

    private TaskOccurrenceDao.TaskOccurrenceWithTitle occurrenceFor(RecyclerView.ViewHolder holder) {
        RecyclerView.Adapter<?> adapter = holder.getBindingAdapter();
        int pos = holder.getBindingAdapterPosition();
        if (pos == RecyclerView.NO_POSITION || !(adapter instanceof RegularTaskSectionAdapter)) return null;
        return ((RegularTaskSectionAdapter) adapter).getOccurrenceAt(pos);
    }

    private void confirmDelete(long taskId, boolean repeating, RecyclerView.Adapter<?> adapter, int pos) {
        Context appContext = requireContext().getApplicationContext();
        Runnable restoreRow = () -> { if (adapter != null) adapter.notifyItemChanged(pos); };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_task_title)
                .setMessage(repeating ? R.string.delete_repeating_message : R.string.delete_task_message)
                .setPositiveButton(R.string.action_delete, (d, w) ->
                        viewModel.deleteTaskById(taskId, () ->
                                appContext.getSystemService(NotificationManager.class)
                                        .cancel(NotificationItem.notificationIdFor(taskId))))
                .setNegativeButton(android.R.string.cancel, (d, w) -> restoreRow.run())
                .setOnCancelListener(d -> restoreRow.run()) // back button / tap outside
                .show();
    }
}