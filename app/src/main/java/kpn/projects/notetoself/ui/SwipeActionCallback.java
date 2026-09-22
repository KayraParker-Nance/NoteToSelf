package kpn.projects.notetoself.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import kpn.projects.notetoself.R;

public class SwipeActionCallback extends ItemTouchHelper.SimpleCallback {

    public interface Listener {
        boolean isSwipeable(RecyclerView.ViewHolder holder);
        void onSwipedRight(RecyclerView.ViewHolder holder); // complete
        void onSwipedLeft(RecyclerView.ViewHolder holder);  // delete
    }

    private final Listener listener;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Drawable completeIcon;
    private final Drawable deleteIcon;
    private final int completeColor;
    private final int deleteColor;
    private final float cornerRadius;
    private final int iconMargin;

    public SwipeActionCallback(Context context, Listener listener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.listener = listener;
        float density = context.getResources().getDisplayMetrics().density;
        this.cornerRadius = 12 * density; // matches the card's cardCornerRadius
        this.iconMargin = (int) (24 * density);
        this.completeColor = ContextCompat.getColor(context, R.color.nts_swipe_complete);
        this.deleteColor = ContextCompat.getColor(context, R.color.nts_due_soon);
        this.completeIcon = ContextCompat.getDrawable(context, R.drawable.ic_swipe_check);
        this.deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_swipe_delete);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh) {
        return listener.isSwipeable(vh) ? super.getSwipeDirs(rv, vh) : 0;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
        if (direction == ItemTouchHelper.RIGHT) {
            listener.onSwipedRight(vh);
        } else {
            listener.onSwipedLeft(vh);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX != 0) {
            View item = vh.itemView;
            boolean completing = dX > 0;

            paint.setColor(completing ? completeColor : deleteColor);
            RectF bg = completing
                    ? new RectF(item.getLeft(), item.getTop(), item.getLeft() + dX, item.getBottom())
                    : new RectF(item.getRight() + dX, item.getTop(), item.getRight(), item.getBottom());
            c.drawRoundRect(bg, cornerRadius, cornerRadius, paint);

            Drawable icon = completing ? completeIcon : deleteIcon;
            int size = icon.getIntrinsicHeight();
            int top = item.getTop() + (item.getHeight() - size) / 2;
            int left = completing
                    ? item.getLeft() + iconMargin
                    : item.getRight() - iconMargin - size;

            if (Math.abs(dX) > iconMargin + size) { // only once there's room for it
                icon.setBounds(left, top, left + size, top + size);
                icon.draw(c);
            }
        }
        super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
    }
}