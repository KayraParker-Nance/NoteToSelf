package kpn.projects.notetoself.enums;

import java.time.LocalDateTime;

public enum ReminderUnit {
    HOUR, DAY, WEEK, MONTH;

    public LocalDateTime addTo(LocalDateTime time, int amount) {
        switch (this) {
            case HOUR:  return time.plusHours(amount);
            case DAY:   return time.plusDays(amount);
            case WEEK:  return time.plusWeeks(amount);
            case MONTH: return time.plusMonths(amount);
            default:    throw new IllegalStateException("Unknown unit: " + this);
        }
    }
}
