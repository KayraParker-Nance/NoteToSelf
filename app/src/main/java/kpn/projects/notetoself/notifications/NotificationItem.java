package kpn.projects.notetoself.notifications;

public class NotificationItem {
    public final long taskId;
    public final Long occurrenceId; // null for DUE_DATE/TODO
    public final String title;
    public final String subtitle;

    public NotificationItem(long taskId, Long occurrenceId, String title, String subtitle) {
        this.taskId = taskId;
        this.occurrenceId = occurrenceId;
        this.title = title;
        this.subtitle = subtitle;
    }

    public int notificationId() {
        // stable per-task id — occurrences change daily but the notification slot for a task shouldn't
        return (int) (2000 + taskId);
    }
}
