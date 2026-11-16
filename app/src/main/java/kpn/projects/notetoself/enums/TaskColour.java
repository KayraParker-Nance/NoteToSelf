package kpn.projects.notetoself.enums;

import androidx.annotation.ColorRes;

import kpn.projects.notetoself.R;

public enum TaskColour {
    NONE(-1),
    RED(R.color.red),
    ORANGE(R.color.orange),
    YELLOW(R.color.yellow),
    GREEN(R.color.green),
    BLUE(R.color.blue),
    PURPLE(R.color.purple),
    PINK(R.color.pink);

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
