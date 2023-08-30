package us.zoom.sdksample.view;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class TopCropImageView extends AppCompatImageView {
    public TopCropImageView(Context context) {
        super(context);
    }

    public TopCropImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TopCropImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        transformMatrix();
        return changed;
    }

    private void transformMatrix() {
        Matrix matrix = getImageMatrix();
        matrix.reset();

        float w = getWidth();
        float cw = getDrawable().getIntrinsicWidth();
        float widthScaleFactor = w / cw;
        matrix.postScale(widthScaleFactor, widthScaleFactor, 0, 0);
    }
}
