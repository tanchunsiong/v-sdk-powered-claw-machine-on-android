package us.zoom.sdksample.view;

import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdksample.R;

public class KeyBoardLayout extends LinearLayout {

    private final static double KEYBOARD_MIN_HEIGHT_RATIO = 0.15;

    public interface KeyBoardListener {
        void onKeyBoardChange(boolean isShow, int height, int inputHeight);
    }

    private KeyBoardListener listener;

    public EditText inputText;

    private int mHeight;

    private int keyboardHeight;

    private boolean isKeyBoardShow;

    float scale;

    private View chatInputGroup;

    private View btnSend;

    public KeyBoardLayout(@NonNull Context context) {
        super(context);
    }

    public KeyBoardLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyBoardLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mHeight = bottom - top;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dismissChat(true);
        updateLayout();
    }

    void init() {
        inputText = findViewById(R.id.chat_input);
        chatInputGroup = findViewById(R.id.chat_input_group);
        btnSend = findViewById(R.id.btn_send);

        Display display = ((WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        scale = metrics.density;

        getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    if (btnSend.getVisibility() != VISIBLE) {
                        btnSend.setVisibility(VISIBLE);
                    }
                } else {
                    if (btnSend.getVisibility() == VISIBLE) {
                        btnSend.setVisibility(GONE);
                    }
                }
            }
        });

        btnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = inputText.getText().toString().trim();
                ZoomVideoSDK.getInstance().getChatHelper().sendChatToAll(content);
                inputText.setText("");
            }
        });
    }

    ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        private boolean wasOpened = false;

        @Override
        public void onGlobalLayout() {
            int height = getHeight();
            Rect r = new Rect();
            getWindowVisibleDisplayFrame(r);
            int heightDiff = height - r.height();
            boolean isOpen = heightDiff > height * KEYBOARD_MIN_HEIGHT_RATIO;
            if (isOpen == wasOpened) {
                // keyboard state has not changed
                return;
            }
            wasOpened = isOpen;
            if (isOpen) {
                keyboardHeight = heightDiff;
                onKeyboardShow(heightDiff);
            } else {
                onKeyboardHidden();
            }
//            Log.w("KeyBoardLayout", String.format("keyboard height: %d  height:%d heightDiff=%d", keyboardHeight, height, heightDiff));
        }
    };

    public void setKeyBoardListener(KeyBoardListener listener) {
        this.listener = listener;
    }

    private void onKeyboardShow(int height) {
        if (height > 0) {
            keyboardHeight = height;
            updateLayout();
        }
        keyboardHeight = height;

        if (null != listener) {
            listener.onKeyBoardChange(true, keyboardHeight, inputText.getHeight());
        }
        isKeyBoardShow = true;
        Log.w("KeyBoardLayout", "onKeyboardShow:" + height + ":" + keyboardHeight);
    }

    private void onKeyboardHidden() {
        setVisibility(INVISIBLE);
        if (null != listener) {
            listener.onKeyBoardChange(false, keyboardHeight, inputText.getHeight());
        }
        isKeyBoardShow = false;
//        getViewTreeObserver().removeGlobalOnLayoutListener(layoutListener);
        Log.w("KeyBoardLayout", "onKeyboardHidden:" + keyboardHeight);
    }

    private void updateLayout() {
        int orientation = getContext().getResources().getConfiguration().orientation;
        LayoutParams params;
        int inputHeight = inputText.getHeight();
        if (inputHeight <= 0) {
            inputHeight = (int) (36 * scale);
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params = (LayoutParams) chatInputGroup.getLayoutParams();
            int height = getHeight() > getWidth() ? getWidth() : getHeight();
            params.topMargin = (height - inputHeight - (int) (12 * scale));
        } else {
            params = (LayoutParams) chatInputGroup.getLayoutParams();
            int height = getHeight() > getWidth() ? getHeight() : getWidth();
            if (keyboardHeight > 0) {
                params.topMargin = (height - keyboardHeight - inputHeight - (int) (12 * scale));
            } else {
                params.topMargin = (int) (12 * scale);
            }
        }
        chatInputGroup.setLayoutParams(params);
        Log.w("KeyBoardLayout", String.format("updateLayout params.topMargin : %d ", params.topMargin) + " orientation:" + orientation
                + "  height:" + getHeight() + " width：" + getWidth());


    }

    public void showChat() {

        inputText.setAlpha(0.0F);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                inputText.setAlpha(1.0F);
            }
        }, 380);
        getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        setVisibility(VISIBLE);
        inputText.requestFocus();

        InputMethodManager inputMethod = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethod.showSoftInput(inputText, InputMethodManager.SHOW_FORCED);


    }

    public boolean isKeyBoardShow() {
        return getVisibility() == VISIBLE;
    }

    public void dismissChat(boolean forceHidden) {
        InputMethodManager inputMethod = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethod.hideSoftInputFromWindow(inputText.getWindowToken(), 0);
        if (forceHidden) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    setVisibility(INVISIBLE);
                }
            }, 380);
        }
    }


}
