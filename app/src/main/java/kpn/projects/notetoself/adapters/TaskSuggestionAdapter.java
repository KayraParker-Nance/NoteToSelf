package kpn.projects.notetoself.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import kpn.projects.notetoself.tasks.TaskDao;

public class TaskSuggestionAdapter extends BaseAdapter implements Filterable {
    private final LayoutInflater inflater;
    private final Function<String, List<TaskDao.TaskTemplate>> source;
    private List<TaskDao.TaskTemplate> items = new ArrayList<>();

    public TaskSuggestionAdapter(Context context, Function<String, List<TaskDao.TaskTemplate>> source) {
        this.inflater = LayoutInflater.from(context);
        this.source = source;
    }

    @Override public int getCount() { return items.size(); }
    @Override public TaskDao.TaskTemplate getItem(int i) { return items.get(i); }
    @Override public long getItemId(int i) { return i; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv = (TextView) (convertView != null ? convertView
                : inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false));
        tv.setText(items.get(position).title);
        return tv;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults r = new FilterResults();
                List<TaskDao.TaskTemplate> result = new ArrayList<>();
                if (constraint != null && !constraint.toString().trim().isEmpty()) {
                    result = source.apply(constraint.toString().trim());
                }
                r.values = result;
                r.count = result.size();
                return r;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults r) {
                items = (List<TaskDao.TaskTemplate>) r.values;
                if (r.count > 0) notifyDataSetChanged(); else notifyDataSetInvalidated();
            }

            @Override
            public CharSequence convertResultToString(Object value) {
                return ((TaskDao.TaskTemplate) value).title;
            }
        };
    }
}
