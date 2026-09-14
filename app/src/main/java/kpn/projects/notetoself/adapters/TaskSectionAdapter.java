package kpn.projects.notetoself.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kpn.projects.notetoself.R;
import kpn.projects.notetoself.enums.TaskType;
import kpn.projects.notetoself.tasks.Task;

public class TaskSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, d MMM");

    public interface Listener {
        void onTaskChecked(Task task, boolean checked);
        void onTaskClicked(Task task);
    }

    private static abstract class Row {}
    private static class HeaderRow extends Row {
        final String text;
        HeaderRow(String text) { this.text = text; }
    }
    private static class TaskRow extends Row {
        final Task task;
        TaskRow(Task task) { this.task = task; }
    }

    private final String headerText;
    private final Listener listener;
    private List<Row> rows = new ArrayList<>();

    public TaskSectionAdapter(String headerText, Listener listener) {
        this.headerText = headerText;
        this.listener = listener;
    }

    public void submitList(List<Task> tasks) {
        List<Row> newRows = new ArrayList<>();
        if (tasks != null && !tasks.isEmpty()) {
            newRows.add(new HeaderRow(headerText));
            for (Task t : tasks) {
                newRows.add(new TaskRow(t));
            }
        }

        List<Row> oldRows = this.rows;
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new RowDiffCallback(oldRows, newRows));
        this.rows = newRows;
        result.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position) instanceof HeaderRow ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(((HeaderRow) row).text);
        } else {
            ((TaskViewHolder) holder).bind(((TaskRow) row).task);
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkbox;
        private final TextView title;
        private final TextView subtitle;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox_task_complete);
            title = itemView.findViewById(R.id.text_task_title);
            subtitle = itemView.findViewById(R.id.text_task_subtitle);
        }

        void bind(Task task) {
            title.setText(task.title);

            if (task.type == TaskType.DUE_DATE && task.dueDate != null) {
                subtitle.setVisibility(View.VISIBLE);
                subtitle.setText(itemView.getContext().getString(
                        R.string.due_date_format, task.dueDate.format(DUE_DATE_FORMAT)));
            } else {
                subtitle.setVisibility(View.GONE);
            }

            // avoid re-triggering the listener while we set the checkbox to match data
            checkbox.setOnCheckedChangeListener(null);
            checkbox.setChecked(task.completed);
            checkbox.setOnCheckedChangeListener((buttonView, isChecked) ->
                    listener.onTaskChecked(task, isChecked));

            itemView.setOnClickListener(v -> listener.onTaskClicked(task));
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView headerView;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerView = (TextView) itemView;
        }

        void bind(String text) {
            headerView.setText(text);
        }
    }

    private static class RowDiffCallback extends DiffUtil.Callback {
        private final List<Row> oldRows;
        private final List<Row> newRows;

        RowDiffCallback(List<Row> oldRows, List<Row> newRows) {
            this.oldRows = oldRows;
            this.newRows = newRows;
        }

        @Override
        public int getOldListSize() {
            return oldRows.size();
        }

        @Override
        public int getNewListSize() {
            return newRows.size();
        }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            Row o = oldRows.get(oldPos);
            Row n = newRows.get(newPos);
            if (o instanceof HeaderRow && n instanceof HeaderRow) return true;
            if (o instanceof TaskRow && n instanceof TaskRow) {
                return ((TaskRow) o).task.id == ((TaskRow) n).task.id;
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            Row o = oldRows.get(oldPos);
            Row n = newRows.get(newPos);
            if (o instanceof HeaderRow) return true; // header text is constant per adapter instance
            Task ot = ((TaskRow) o).task;
            Task nt = ((TaskRow) n).task;
            return ot.completed == nt.completed
                    && Objects.equals(ot.title, nt.title)
                    && Objects.equals(ot.dueDate, nt.dueDate);
        }
    }
}