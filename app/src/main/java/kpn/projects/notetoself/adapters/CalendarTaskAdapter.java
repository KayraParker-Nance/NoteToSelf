package kpn.projects.notetoself.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kpn.projects.notetoself.R;
import kpn.projects.notetoself.enums.OccurrenceStatus;

public class CalendarTaskAdapter extends RecyclerView.Adapter<CalendarTaskAdapter.ViewHolder> {
    public static class Item {
        public final long taskId;
        public final Long occurrenceId;
        public final String title;
        public final boolean completed;
        public final OccurrenceStatus occurrenceStatus;

        public Item(long taskId, Long occurrenceId, String title, boolean completed, OccurrenceStatus occurrenceStatus) {
            this.taskId = taskId;
            this.occurrenceId = occurrenceId;
            this.title = title;
            this.completed = completed;
            this.occurrenceStatus = occurrenceStatus;
        }
    }

    public interface Listener {
        void onItemClicked(long taskId);
        void onDueDateChecked(long taskId, boolean checked);
        void onOccurrenceCompleted(long occurrenceId);
    }

    private final Listener listener;
    private List<Item> items = new ArrayList<>();

    public CalendarTaskAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Item> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkbox;
        private final TextView title;
        private final TextView subtitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox_task_complete);
            title = itemView.findViewById(R.id.text_task_title);
            subtitle = itemView.findViewById(R.id.text_task_subtitle);
        }

        void bind(Item item) {
            title.setText(item.title);
            checkbox.setOnCheckedChangeListener(null);

            if (item.occurrenceId != null) {
                boolean isPending = item.occurrenceStatus == OccurrenceStatus.PENDING;
                subtitle.setVisibility(View.VISIBLE);
                subtitle.setText(item.occurrenceStatus.name());
                checkbox.setEnabled(isPending);
                checkbox.setChecked(item.occurrenceStatus == OccurrenceStatus.COMPLETED);
                checkbox.setOnCheckedChangeListener((buttonView, checked) -> {
                    if (checked && isPending) listener.onOccurrenceCompleted(item.occurrenceId);
                });
            } else {
                subtitle.setVisibility(View.GONE);
                checkbox.setEnabled(true);
                checkbox.setChecked(item.completed);
                checkbox.setOnCheckedChangeListener((buttonView, checked) ->
                        listener.onDueDateChecked(item.taskId, checked));
            }

            itemView.setOnClickListener(v -> listener.onItemClicked(item.taskId));
        }
    }
}
