package kpn.projects.notetoself.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import kpn.projects.notetoself.R;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {
    public static class DayCell {
        public final LocalDate date;
        public final boolean inCurrentMonth;
        public boolean hasDueDate;
        public boolean hasRegular;

        public DayCell(LocalDate date, boolean inCurrentMonth) {
            this.date = date;
            this.inCurrentMonth = inCurrentMonth;
        }
    }

    public interface Listener {
        void onDayClicked(LocalDate date);
    }

    private final Listener listener;
    private List<DayCell> cells = new ArrayList<>();
    private LocalDate selectedDate;
    private final LocalDate today = LocalDate.now();

    public CalendarDayAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitCells(List<DayCell> newCells, LocalDate selectedDate) {
        this.cells = newCells;
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(cells.get(position));
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private final TextView textDayNumber;
        private final View dotDueDate;
        private final View dotRegular;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            textDayNumber = itemView.findViewById(R.id.text_day_number);
            dotDueDate = itemView.findViewById(R.id.dot_due_date);
            dotRegular = itemView.findViewById(R.id.dot_regular);
        }

        void bind(DayCell cell) {
            textDayNumber.setText(String.valueOf(cell.date.getDayOfMonth()));
            textDayNumber.setAlpha(cell.inCurrentMonth ? 1f : 0.35f);

            boolean isSelected = cell.date.equals(selectedDate);
            boolean isToday = cell.date.equals(today);

            textDayNumber.setBackgroundResource(isSelected ? R.drawable.bg_day_selected : 0);
            int colorRes = isSelected ? R.color.white : (isToday ? R.color.nts_accent : R.color.nts_text_primary);
            textDayNumber.setTextColor(itemView.getContext().getColor(colorRes));

            dotDueDate.setVisibility(cell.hasDueDate ? View.VISIBLE : View.GONE);
            dotRegular.setVisibility(cell.hasRegular ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> listener.onDayClicked(cell.date));
        }
    }
}
