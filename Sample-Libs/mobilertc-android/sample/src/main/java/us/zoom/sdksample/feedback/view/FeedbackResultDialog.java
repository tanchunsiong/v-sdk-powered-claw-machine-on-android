package us.zoom.sdksample.feedback.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import us.zoom.sdksample.R;
import us.zoom.sdksample.feedback.data.FeedbackDataManager;
import us.zoom.sdksample.feedback.model.FeedbackResultData;
import us.zoom.sdksample.feedback.model.FeedbackResultItem;
import us.zoom.sdksample.feedback.util.FeedbackUtils;
import us.zoom.sdksample.util.SharePreferenceUtil;

public class FeedbackResultDialog extends BaseFeedbackDialog {
    private static final int FEEDBACK_ITEM_COUNT = 5;
    private static final int REPUSH_INTERVAL = 60 * 1000;
    private static long lastPushTime;
    private int[] iconIds = {R.drawable.icon_survey_very_satisfied, R.drawable.icon_survey_satisfied,
            R.drawable.icon_survey_neutral, R.drawable.icon_survey_unsatisfied,
            R.drawable.icon_survey_very_unsatisfied};
    private String[] itemTitles;

    private int mFeedbackCount;
    private FeedbackResultItem[] mFeedbackResultList = new FeedbackResultItem[FEEDBACK_ITEM_COUNT];
    private FeedbackResultItemView[] mFeedbackResultItemViews = new FeedbackResultItemView[FEEDBACK_ITEM_COUNT];

    private TextView mTvFeedbackCount;
    private View mTvPublishSurvey;
    private TextView mTvHasPushTip;
    private Context mContext;

    private CountDownTimer mCountDownTimer;

    private FeedbackDataManager.DataChangeListener mDataChangeListener = new FeedbackDataManager.DataChangeListener() {
        @Override
        public void onDataChange(FeedbackResultData feedbackData) {
            updateData();
            updateUI();
        }
    };

    public static void show(FragmentActivity activity) {
        FeedbackResultDialog dialog = new FeedbackResultDialog();
        dialog.show(activity.getSupportFragmentManager(), "FeedbackPublishDialog");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        itemTitles = context.getResources().getStringArray(R.array.feedback_survey);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateData();
        FeedbackDataManager.getInstance().addDataListener(mDataChangeListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FeedbackDataManager.getInstance().removeDataListener(mDataChangeListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_feedback_result, container, false);
        initView(inflater, view);
        return view;
    }

    @Override
    protected void setHeight(BottomSheetDialog bottomSheetDialog, BottomSheetBehavior behavior) {
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
        int windowHeight = getWindowHeight();
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
    }

    private int getWindowHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    private void initView(LayoutInflater inflater, View view) {
        if (getActivity() == null) {
            return;
        }
        LinearLayout linearLayout = view.findViewById(R.id.layout_survey);
        LinearLayout.LayoutParams params;
        int topMargin = getActivity().getResources().getDimensionPixelSize(R.dimen.dp_10);

        for (int i = 0; i < FEEDBACK_ITEM_COUNT; i++) {
            mFeedbackResultItemViews[i] = new FeedbackResultItemView(getActivity());
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i != 0) {
                params.topMargin = topMargin;
            }
            linearLayout.addView(mFeedbackResultItemViews[i], params);
        }

        view.findViewById(R.id.tv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickClose();
            }
        });

        mTvFeedbackCount = view.findViewById(R.id.tv_feedback_count);
        mTvPublishSurvey = view.findViewById(R.id.tv_push_survey);

        mTvPublishSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPush();
            }
        });
        mTvHasPushTip = view.findViewById(R.id.tv_has_push_tip);
        long pushInterval = System.currentTimeMillis() - lastPushTime;

        if (pushInterval <= REPUSH_INTERVAL) {
            mTvPublishSurvey.setEnabled(false);
            mTvHasPushTip.setVisibility(View.VISIBLE);
            startCountDownTime(REPUSH_INTERVAL - pushInterval);
        } else {
            mTvPublishSurvey.setEnabled(true);
            mTvHasPushTip.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateData() {
        FeedbackResultData feedbackData = FeedbackDataManager.getInstance().getFeedbackData();
        mFeedbackCount = feedbackData.feedbackCount;

        for (int i = 0; i < mFeedbackResultList.length; i++) {
            mFeedbackResultList[i] = feedbackData.resultItems.get(i);

            mFeedbackResultList[i].iconResId = iconIds[i];
            if (itemTitles != null && itemTitles[i] != null) {
                mFeedbackResultList[i].title = itemTitles[i];
            }
        }
    }

    private void updateUI() {
        mTvFeedbackCount.setText(String.format(mContext.getResources().getString(R.string.feedback_response_count), mFeedbackCount));
        for (int i = 0; i < mFeedbackResultItemViews.length; i++) {
            FeedbackResultItemView itemView = mFeedbackResultItemViews[i];
            if (itemView != null) {
                itemView.setData(mFeedbackResultList[i]);
            }
        }
    }

    private void onClickPush() {
        if (getActivity() == null) {
            return;
        }

        boolean hasShowed = SharePreferenceUtil.readBoolean(mContext, SharePreferenceUtil.KEY_SP_FEEDBACK_PUSH_CONFIRM_SHOWED, false);
        if (hasShowed) {
            performClickPush();
        } else {
            FeedbackPushConfirmDialog.show(getActivity(), new FeedbackPushConfirmDialog.OnClickPushListener() {
                @Override
                public void onClickPush() {
                    performClickPush();
                }
            });

            SharePreferenceUtil.saveBoolean(mContext, SharePreferenceUtil.KEY_SP_FEEDBACK_PUSH_CONFIRM_SHOWED, true);
        }
    }

    private void performClickPush() {
        mTvHasPushTip.setVisibility(View.VISIBLE);
        mTvPublishSurvey.setEnabled(false);

        lastPushTime = System.currentTimeMillis();
        startCountDownTime(REPUSH_INTERVAL);
        FeedbackUtils.pushFeedback();
    }

    private void startCountDownTime(long startTime) {
        mCountDownTimer = new CountDownTimer(startTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (mContext != null) {
                    mTvHasPushTip.setText(String.format(mContext.getResources().getString(R.string.feedback_repush_tip), millisUntilFinished / 1000));
                }
            }

            @Override
            public void onFinish() {
                mCountDownTimer = null;
                mTvPublishSurvey.setEnabled(true);
                mTvHasPushTip.setVisibility(View.INVISIBLE);
            }
        };
        mCountDownTimer.start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    private void onClickClose() {
        dismiss();
    }
}
