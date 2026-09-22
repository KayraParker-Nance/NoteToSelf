package kpn.projects.notetoself.notifications;

import kpn.projects.notetoself.enums.TaskColour;

public class NotificationItem {
    public final long taskId;
    public final Long occurrenceId;
    public final String title;
    public final String subtitle;
    public final TaskColour color;

    public NotificationItem(long taskId, Long occurrenceId, String title, String subtitle, TaskColour color) {
        this.taskId = taskId;
        this.occurrenceId = occurrenceId;
        this.title = title;
        this.subtitle = subtitle;
        this.color = color;
    }

    public static int notificationIdFor(long taskId) {
        return (int) (2000 + taskId);
    }

    public int notificationId() {
        return notificationIdFor(taskId);
    }


}
