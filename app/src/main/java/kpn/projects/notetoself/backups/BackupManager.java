package kpn.projects.notetoself.backups;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import kpn.projects.notetoself.AppDatabase;
import kpn.projects.notetoself.enums.OccurrenceStatus;
import kpn.projects.notetoself.enums.RecurrenceUnit;
import kpn.projects.notetoself.enums.ScheduleMode;
import kpn.projects.notetoself.enums.TaskType;
import kpn.projects.notetoself.tasks.NotificationConfig;
import kpn.projects.notetoself.tasks.Task;
import kpn.projects.notetoself.tasks.TaskOccurrence;

public class BackupManager {
    private static final int SCHEMA_VERSION = 1;

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }

    public static void export(Context context, Uri uri, Callback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                JSONObject root = new JSONObject();
                root.put("schemaVersion", SCHEMA_VERSION);
                root.put("tasks", tasksToJson(db.taskDao().getAllTasksSync()));
                root.put("occurrences", occurrencesToJson(db.taskOccurrenceDao().getAllSync()));
                root.put("configs", configsToJson(db.notificationConfigDao().getAllConfigsSync()));

                try (OutputStream out = context.getContentResolver().openOutputStream(uri, "w")) {
                    out.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
                }
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public static void restore(Context context, Uri uri, Callback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        context.getContentResolver().openInputStream(uri), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }
                JSONObject root = new JSONObject(sb.toString());

                List<Task> tasks = tasksFromJson(root.getJSONArray("tasks"));
                List<TaskOccurrence> occurrences = occurrencesFromJson(root.getJSONArray("occurrences"));
                List<NotificationConfig> configs = configsFromJson(root.getJSONArray("configs"));

                AppDatabase db = AppDatabase.getInstance(context);
                db.runInTransaction(() -> {
                    db.taskDao().deleteAllTasks(); // cascades occurrences + configs
                    db.taskDao().insertTasks(tasks);
                    db.taskOccurrenceDao().insertOccurrences(occurrences);
                    db.notificationConfigDao().insertConfigs(configs);
                });
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    private static JSONArray tasksToJson(List<Task> tasks) throws JSONException {
        JSONArray arr = new JSONArray();
        for (Task t : tasks) {
            JSONObject o = new JSONObject();
            o.put("id", t.id);
            o.put("title", t.title);
            o.put("description", t.description);
            o.put("type", t.type.name());
            o.put("dueDate", t.dueDate != null ? t.dueDate.toString() : JSONObject.NULL);
            o.put("recurrenceInterval", t.recurrenceInterval);
            o.put("recurrenceUnit", t.recurrenceUnit != null ? t.recurrenceUnit.name() : JSONObject.NULL);
            o.put("recurrenceStartDate", t.recurrenceStartDate != null ? t.recurrenceStartDate.toString() : JSONObject.NULL);
            o.put("scheduleMode", t.scheduleMode != null ? t.scheduleMode.name() : JSONObject.NULL);
            o.put("completed", t.completed);
            o.put("createdAt", t.createdAt.toString());
            arr.put(o);
        }
        return arr;
    }

    private static List<Task> tasksFromJson(JSONArray arr) throws JSONException {
        List<Task> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            Task t = new Task();
            t.id = o.getLong("id");
            t.title = o.getString("title");
            t.description = o.isNull("description") ? null : o.getString("description");
            t.type = TaskType.valueOf(o.getString("type"));
            t.dueDate = o.isNull("dueDate") ? null : LocalDateTime.parse(o.getString("dueDate"));
            t.recurrenceInterval = o.getInt("recurrenceInterval");
            t.recurrenceUnit = o.isNull("recurrenceUnit") ? null : RecurrenceUnit.valueOf(o.getString("recurrenceUnit"));
            t.recurrenceStartDate = o.isNull("recurrenceStartDate") ? null : LocalDate.parse(o.getString("recurrenceStartDate"));
            t.scheduleMode = o.isNull("scheduleMode") ? null : ScheduleMode.valueOf(o.getString("scheduleMode"));
            t.completed = o.getBoolean("completed");
            t.createdAt = LocalDateTime.parse(o.getString("createdAt"));
            list.add(t);
        }
        return list;
    }

    private static JSONArray occurrencesToJson(List<TaskOccurrence> list) throws JSONException {
        JSONArray arr = new JSONArray();
        for (TaskOccurrence occ : list) {
            JSONObject o = new JSONObject();
            o.put("id", occ.id);
            o.put("taskId", occ.taskId);
            o.put("scheduledDate", occ.scheduledDate.toString());
            o.put("completedAt", occ.completedAt != null ? occ.completedAt.toString() : JSONObject.NULL);
            o.put("status", occ.status.name());
            arr.put(o);
        }
        return arr;
    }

    private static List<TaskOccurrence> occurrencesFromJson(JSONArray arr) throws JSONException {
        List<TaskOccurrence> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            TaskOccurrence occ = new TaskOccurrence();
            occ.id = o.getLong("id");
            occ.taskId = o.getLong("taskId");
            occ.scheduledDate = LocalDate.parse(o.getString("scheduledDate"));
            occ.completedAt = o.isNull("completedAt") ? null : LocalDateTime.parse(o.getString("completedAt"));
            occ.status = OccurrenceStatus.valueOf(o.getString("status"));
            list.add(occ);
        }
        return list;
    }

    private static JSONArray configsToJson(List<NotificationConfig> list) throws JSONException {
        JSONArray arr = new JSONArray();
        for (NotificationConfig c : list) {
            JSONObject o = new JSONObject();
            o.put("taskId", c.taskId);
            o.put("enabled", c.enabled);
            o.put("showAfterDate", c.showAfterDate != null ? c.showAfterDate.toString() : JSONObject.NULL);
            o.put("repeatIntervalHours", c.repeatIntervalHours);
            o.put("stickyEnabled", c.stickyEnabled);
            o.put("snoozedUntil", c.snoozedUntil != null ? c.snoozedUntil.toString() : JSONObject.NULL);
            arr.put(o);
        }
        return arr;
    }

    private static List<NotificationConfig> configsFromJson(JSONArray arr) throws JSONException {
        List<NotificationConfig> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            NotificationConfig c = new NotificationConfig();
            c.taskId = o.getLong("taskId");
            c.enabled = o.getBoolean("enabled");
            c.showAfterDate = o.isNull("showAfterDate") ? null : LocalDateTime.parse(o.getString("showAfterDate"));
            c.repeatIntervalHours = o.getInt("repeatIntervalHours");
            c.stickyEnabled = o.getBoolean("stickyEnabled");
            c.snoozedUntil = o.isNull("snoozedUntil") ? null : LocalDateTime.parse(o.getString("snoozedUntil"));
            list.add(c);
        }
        return list;
    }
}
