package us.zoom.sdksample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import us.zoom.sdksample.R;

public class CycleColorImageView extends AppCompatImageView {
    private static final int DEFAULT_BOARDER_WIDTH = 3;
    private static final int DEFAULT_BOARDER_COLOR = Color.BLACK;
    private static final int DEFAULT_CIRCLE_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final int DEFAULT_IMAGE_COLOR = Color.WHITE;
    private static final int DEFAULT_IMAGE_ALPHA = 255;

    @NonNull
    private final Paint boarderPaint = new Paint();
    @NonNull
    private final Paint backgroundPaint = new Paint();
    @NonNull
    private final Paint colorPaint = new Paint();

    private int boarderWidth = DEFAULT_BOARDER_WIDTH;
    private int boarderColor = DEFAULT_BOARDER_COLOR;
    private int circleBgColor = DEFAULT_CIRCLE_BACKGROUND_COLOR;
    private int imageColor = DEFAULT_IMAGE_COLOR;
    private boolean selected = false;

    @NonNull
    private RectF drawableRect = new RectF();
    @NonNull
    private RectF boarderRect = new RectF();

    private float boarderRadius = 0f;
    private float drawableRadius = 0f;

    public CycleColorImageView(@NonNull Context context) {
        this(context, null);
    }

    public CycleColorImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CycleColorImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CycleColorImageView, defStyleAttr, 0);
        boarderWidth = typedArray.getDimensionPixelSize(R.styleable.CycleColorImageView_civ_boarder_width, DEFAULT_BOARDER_WIDTH);
        boarderColor = typedArray.getColor(R.styleable.CycleColorImageView_civ_boarder_color, DEFAULT_BOARDER_COLOR);
        circleBgColor = typedArray.getColor(R.styleable.CycleColorImageView_civ_circle_background_color, DEFAULT_CIRCLE_BACKGROUND_COLOR);
        imageColor = typedArray.getColor(R.styleable.CycleColorImageView_civ_circle_image_color, DEFAULT_IMAGE_COLOR);
        typedArray.recycle();
        init();
    }

    private void init() {
        boarderPaint.setStyle(Paint.Style.STROKE);
        boarderPaint.setAntiAlias(true);
        boarderPaint.setColor(boarderColor);
        boarderPaint.setStrokeWidth(boarderWidth);

        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(circleBgColor);

        colorPaint.setAntiAlias(true);
        colorPaint.setDither(true);
        colorPaint.setFilterBitmap(true);
        colorPaint.setAlpha(DEFAULT_IMAGE_ALPHA);
        colorPaint.setColor(imageColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        if (circleBgColor != Color.TRANSPARENT) {
            /* draw background */
            canvas.drawCircle(boarderRect.centerX(), boarderRect.centerY(), boarderRadius, backgroundPaint);
        }

        canvas.drawCircle(drawableRect.centerX(), drawableRect.centerY(), drawableRadius, colorPaint);

        if (boarderWidth > 0 && selected) {
            canvas.drawCircle(boarderRect.centerX(), boarderRect.centerY(), boarderRadius, boarderPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateDimensions();
        invalidate();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        updateDimensions();
        invalidate();
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
        updateDimensions();
        invalidate();
    }

    private void updateDimensions() {
        boarderRect.set(calculateBounds());
        boarderRadius = (Math.min(boarderRect.height(), boarderRect.width()) - boarderWidth) / 2.0f;

        drawableRect.set(boarderRect);
        if (boarderWidth > 0) {
            // TODO: 12/29/2021 this need add an attributed
            drawableRect.inset(boarderWidth + 6.0f, boarderWidth + 6.0f);
        }
        drawableRadius = (Math.min(drawableRect.height(), drawableRect.width())) / 2.0f;
    }

    private RectF calculateBounds() {
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        int diameterLength = Math.max(availableWidth, availableHeight);
        int left = (availableWidth - diameterLength) / 2 + getPaddingLeft();
        int top = (availableHeight - diameterLength) / 2 + getPaddingTop();

        return new RectF(left, top, left + diameterLength, top + diameterLength);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        invalidate();
    }

    public boolean isSelected() {
        return selected;
    }
}
