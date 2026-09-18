package kpn.projects.notetoself.adapters;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.card.MaterialCardView;

import kpn.projects.notetoself.R;
import kpn.projects.notetoself.enums.TaskColour;

public class TaskColourUI {
    private static final float STROKE_DARKEN_AMOUNT = 0.18f;
    private static final int STROKE_WIDTH_DP = 2;

    public static void apply(MaterialCardView card, TaskColour color) {
        Context context = card.getContext();

        float density = context.getResources().getDisplayMetrics().density;

        if (color == null || color == TaskColour.NONE) {
            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.nts_surface));
            card.setStrokeColor(ContextCompat.getColor(context, R.color.nts_outline));
            card.setStrokeWidth((int) (1 * density));
            return;
        }

        int baseColor = ContextCompat.getColor(context, color.getColorRes());
        int strokeColor = ColorUtils.blendARGB(baseColor, Color.BLACK, STROKE_DARKEN_AMOUNT);

        card.setCardBackgroundColor(baseColor);
        card.setStrokeColor(strokeColor);
        card.setStrokeWidth((int) (1 * density));
    }
}
