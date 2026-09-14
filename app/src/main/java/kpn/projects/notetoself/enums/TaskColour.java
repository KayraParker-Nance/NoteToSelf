package kpn.projects.notetoself.enums;

import androidx.annotation.ColorRes;

import kpn.projects.notetoself.R;

public enum TaskColour {
    NONE(-1),
    PINK(R.color.task_pink),
    PEACH(R.color.task_peach),
    YELLOW(R.color.task_yellow),
    MINT(R.color.task_mint),
    SKY(R.color.task_sky),
    LAVENDER(R.color.task_lavender);

    @ColorRes
    private final int colorRes;

    TaskColour(@ColorRes int colorRes) {
        this.colorRes = colorRes;
    }

    @ColorRes
    public int getColorRes() {
        return colorRes;
    }
}
