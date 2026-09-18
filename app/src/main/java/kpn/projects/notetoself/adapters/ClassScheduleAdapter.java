package kpn.projects.notetoself.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kpn.projects.notetoself.R;
import kpn.projects.notetoself.schedule.Class;

public class ClassScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public interface Listener {
        void onClassClicked(Class classEntry);
    }

    private static abstract class Row {}
    private static class HeaderRow extends Row {
        final String text;
        HeaderRow(String text) { this.text = text; }
    }
    private static class ClassRow extends Row {
        final Class entry;
        ClassRow(Class entry) { this.entry = entry; }
    }

    private final Listener listener;
    private List<Row> rows = new ArrayList<>();

    public ClassScheduleAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Class> classes) {
        Map<DayOfWeek, List<Class>> byDay = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) byDay.put(day, new ArrayList<>());
        if (classes != null) {
            for (Class entry : classes) byDay.get(entry.dayOfWeek).add(entry);
        }

        List<Row> newRows = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            newRows.add(new HeaderRow(day.getDisplayName(TextStyle.FULL, Locale.getDefault())));
            for (Class entry : byDay.get(day)) {
                newRows.add(new ClassRow(entry));
            }
        }

        this.rows = newRows;
        notifyDataSetChanged();
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
        return new ClassViewHolder(inflater.inflate(R.layout.item_class, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(((HeaderRow) row).text);
        } else {
            ((ClassViewHolder) holder).bind(((ClassRow) row).entry);
        }
    }

    class ClassViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView time;
        private final TextView title;
        private final TextView location;

        ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            time = itemView.findViewById(R.id.text_class_time);
            title = itemView.findViewById(R.id.text_class_title);
            location = itemView.findViewById(R.id.text_class_location);
        }

        void bind(Class entry) {
            time.setText(itemView.getContext().getString(R.string.class_time_format,
                    entry.startTime.format(TIME_FORMAT), entry.endTime.format(TIME_FORMAT)));
            title.setText(entry.title);

            if (entry.location != null && !entry.location.isEmpty()) {
                location.setVisibility(View.VISIBLE);
                location.setText(entry.location);
            } else {
                location.setVisibility(View.GONE);
            }

            TaskColourUI.apply(card, entry.color);

            itemView.setOnClickListener(v -> listener.onClassClicked(entry));
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
}
