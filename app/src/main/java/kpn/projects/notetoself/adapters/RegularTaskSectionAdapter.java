package kpn.projects.notetoself.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kpn.projects.notetoself.R;
import kpn.projects.notetoself.tasks.TaskOccurrenceDao;

public class RegularTaskSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, d MMM");

    public interface Listener {
        void onOccurrenceChecked(long occurrenceId, boolean checked);
        void onOccurrenceClicked(long taskId);
    }

    private static abstract class Row {}
    private static class HeaderRow extends Row {
        final String text;
        HeaderRow(String text) { this.text = text; }
    }
    private static class OccurrenceRow extends Row {
        final TaskOccurrenceDao.TaskOccurrenceWithTitle occurrence;
        OccurrenceRow(TaskOccurrenceDao.TaskOccurrenceWithTitle occurrence) { this.occurrence = occurrence; }
    }

    private final String headerText;
    private final Listener listener;
    private List<Row> rows = new ArrayList<>();

    public RegularTaskSectionAdapter(String headerText, Listener listener) {
        this.headerText = headerText;
        this.listener = listener;
    }

    public void submitList(List<TaskOccurrenceDao.TaskOccurrenceWithTitle> occurrences) {
        List<Row> newRows = new ArrayList<>();
        if (occurrences != null && !occurrences.isEmpty()) {
            newRows.add(new HeaderRow(headerText));
            for (TaskOccurrenceDao.TaskOccurrenceWithTitle o : occurrences) {
                newRows.add(new OccurrenceRow(o));
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
            return new HeaderViewHolder(inflater.inflate(R.layout.item_section_header, parent, false));
        }
        return new OccurrenceViewHolder(inflater.inflate(R.layout.item_task, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(((HeaderRow) row).text);
        } else {
            ((OccurrenceViewHolder) holder).bind(((OccurrenceRow) row).occurrence);
        }
    }

    class OccurrenceViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final CheckBox checkbox;
        private final TextView title;
        private final TextView subtitle;

        OccurrenceViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            checkbox = itemView.findViewById(R.id.checkbox_task_complete);
            title = itemView.findViewById(R.id.text_task_title);
            subtitle = itemView.findViewById(R.id.text_task_subtitle);
        }

        void bind(TaskOccurrenceDao.TaskOccurrenceWithTitle occurrence) {
            title.setText(occurrence.title);

            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(itemView.getContext().getString(
                    R.string.due_date_format, occurrence.scheduledDate.format(DATE_FORMAT)));

            TaskColourUI.apply(card, occurrence.color);

            checkbox.setOnCheckedChangeListener(null);
            checkbox.setChecked(false); // row only exists while PENDING — always shown unchecked
            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    listener.onOccurrenceChecked(occurrence.occurrenceId, true);
                }
            });

            itemView.setOnClickListener(v -> listener.onOccurrenceClicked(occurrence.taskId));
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

        @Override public int getOldListSize() { return oldRows.size(); }
        @Override public int getNewListSize() { return newRows.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            Row o = oldRows.get(oldPos);
            Row n = newRows.get(newPos);
            if (o instanceof HeaderRow && n instanceof HeaderRow) return true;
            if (o instanceof OccurrenceRow && n instanceof OccurrenceRow) {
                return ((OccurrenceRow) o).occurrence.occurrenceId == ((OccurrenceRow) n).occurrence.occurrenceId;
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            Row o = oldRows.get(oldPos);
            Row n = newRows.get(newPos);
            if (o instanceof HeaderRow) return true;
            TaskOccurrenceDao.TaskOccurrenceWithTitle ot = ((OccurrenceRow) o).occurrence;
            TaskOccurrenceDao.TaskOccurrenceWithTitle nt = ((OccurrenceRow) n).occurrence;

            return Objects.equals(ot.title, nt.title)
                    && Objects.equals(ot.scheduledDate, nt.scheduledDate)
                    && ot.color == nt.color;
        }
    }
}
