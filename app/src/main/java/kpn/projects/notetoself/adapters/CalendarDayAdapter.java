package kpn.projects.notetoself.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
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
        public List<Integer> dotColorResList = new ArrayList<>();

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
        private final LinearLayout layoutDots;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            textDayNumber = itemView.findViewById(R.id.text_day_number);
            layoutDots = itemView.findViewById(R.id.layout_dots);
        }

        void bind(DayCell cell) {
            textDayNumber.setText(String.valueOf(cell.date.getDayOfMonth()));
            textDayNumber.setAlpha(cell.inCurrentMonth ? 1f : 0.35f);

            boolean isSelected = cell.date.equals(selectedDate);
            boolean isToday = cell.date.equals(today);

            textDayNumber.setBackgroundResource(isSelected ? R.drawable.bg_day_selected : 0);
            int colorRes = isSelected ? R.color.white : (isToday ? R.color.nts_accent : R.color.nts_text_primary);
            textDayNumber.setTextColor(itemView.getContext().getColor(colorRes));

            bindDots(cell.dotColorResList);

            itemView.setOnClickListener(v -> listener.onDayClicked(cell.date));
        }


        private void bindDots(List<Integer> colorResList) {
            layoutDots.removeAllViews();
            Context context = itemView.getContext();
            float density = context.getResources().getDisplayMetrics().density;
            int dotSize = (int) (6 * density);
            int dotMargin = (int) (2 * density);

            for (Integer colorRes : colorResList) {
                View dot = new View(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
                params.setMarginEnd(dotMargin);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.dot_indicator);
                dot.setBackgroundTintList(ColorStateList.valueOf(context.getColor(colorRes)));
                layoutDots.addView(dot);
            }
        }

    }
}
