package kpn.projects.notetoself.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import kpn.projects.notetoself.R;
import kpn.projects.notetoself.adapters.ClassScheduleAdapter;
import kpn.projects.notetoself.schedule.Class;

public class ScheduleFragment extends Fragment implements ClassScheduleAdapter.Listener {

    private ScheduleViewModel viewModel;
    private ClassScheduleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        adapter = new ClassScheduleAdapter(this);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_schedule);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getAllClasses().observe(getViewLifecycleOwner(), adapter::submitList);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_class);
        fab.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_schedule_to_addEditClass));
    }

    @Override
    public void onClassClicked(Class classEntry) {
        Bundle args = new Bundle();
        args.putLong("classId", classEntry.id);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_schedule_to_addEditClass, args);
    }
}