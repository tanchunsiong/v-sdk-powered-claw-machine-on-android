package us.zoom.sdksample.feedback.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import us.zoom.sdksample.R;
import us.zoom.sdksample.cmd.FeedbackType;
import us.zoom.sdksample.feedback.util.FeedbackUtils;

public class FeedbackSubmitDialog extends BaseFeedbackDialog {
    private static final int FEEDBACK_ITEM_COUNT = 5;
    private static final int AUTO_CLOSE_DELAY = 5000;
    private View mLayoutSubmit;
    private View mLayoutThank;
    private View mtvSubmit;
    private TextView mTvFeedbackContent;
    private String[] feedbackContents;
    private View[] feedbackIcons = new View[FEEDBACK_ITEM_COUNT];
    private View mSelectedIcon;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Context mContext;

    public static void show(FragmentActivity activity) {
        FeedbackSubmitDialog dialog = new FeedbackSubmitDialog();
        dialog.show(activity.getSupportFragmentManager(), "PushFeedbackSubmitDialog");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        feedbackContents = context.getResources().getStringArray(R.array.feedback_survey);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_feedback_submit, container, false);
        initView(inflater, view);
        return view;
    }

    private void initView(LayoutInflater inflater, View view) {
        if (getActivity() == null) {
            return;
        }

        mLayoutSubmit = view.findViewById(R.id.layout_submit);
        mLayoutThank = view.findViewById(R.id.layout_thank);
        mtvSubmit = view.findViewById(R.id.tv_submit);
        mTvFeedbackContent = view.findViewById(R.id.tv_survey_content);
        ViewGroup layoutImageView = view.findViewById(R.id.layout_images);
        int count = layoutImageView.getChildCount();
        for (int i = 0; i < count; i++) {
            View childView = layoutImageView.getChildAt(i);
            childView.setSelected(false);
            int feedbackType = FEEDBACK_ITEM_COUNT - i;
            if (feedbackType >= 0 && feedbackType < FeedbackType.values().length) {
                childView.setTag(FeedbackType.values()[feedbackType]);
            }

            final int index = FEEDBACK_ITEM_COUNT - 1 - i;
            childView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (feedbackContents != null && index >= 0 && index < feedbackContents.length) {
                        mTvFeedbackContent.setText(feedbackContents[index]);
                    }
                    if (mSelectedIcon != null) {
                        mSelectedIcon.setSelected(false);
                    }
                    v.setSelected(true);
                    mtvSubmit.setEnabled(true);
                    mSelectedIcon = v;
                }
            });
        }


        mtvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSubmit();
            }
        });

        view.findViewById(R.id.tv_thank_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        view.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void onClickSubmit() {
        if (mSelectedIcon == null) {
            return;
        }
        mLayoutSubmit.setVisibility(View.INVISIBLE);
        mLayoutThank.setVisibility(View.VISIBLE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, AUTO_CLOSE_DELAY);

        FeedbackUtils.submitFeedback((FeedbackType) mSelectedIcon.getTag());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void setHeight(BottomSheetDialog bottomSheetDialog, BottomSheetBehavior behavior) {
        int padding = mContext.getResources().getDimensionPixelOffset(R.dimen.dp_12);
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        bottomSheet.setPadding(padding, 0, padding, padding);
    }
}
