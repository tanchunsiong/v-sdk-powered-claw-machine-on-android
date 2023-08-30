package us.zoom.sdksample.feedback.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import us.zoom.sdksample.R;
import us.zoom.sdksample.feedback.model.FeedbackResultItem;

public class FeedbackResultItemView extends FrameLayout {
    private Context mContext;
    private ImageView mIvIcon;
    private TextView mTvTitle;
    private TextView mTvPercent;
    private TextView mTvCount;
    private ProgressBar mProgressBar;

    public FeedbackResultItemView(Context context) {
        this(context, null);
    }

    public FeedbackResultItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FeedbackResultItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FeedbackResultItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        initView();
    }

    protected void initView() {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.layout_feedback_result_item, this, false);
        addView(rootView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mIvIcon = rootView.findViewById(R.id.iv_icon);
        mTvTitle = rootView.findViewById(R.id.tv_title);
        mTvPercent = rootView.findViewById(R.id.tv_percent);
        mTvCount = rootView.findViewById(R.id.tv_count);
        mProgressBar = rootView.findViewById(R.id.progress_bar);
    }

    public void setData(FeedbackResultItem feedbackResultItem) {
        if (feedbackResultItem == null) {
            return;
        }
        updateUI(feedbackResultItem);
    }

    private void updateUI(FeedbackResultItem feedbackResultItem) {
        mIvIcon.setImageResource(feedbackResultItem.iconResId);
        mTvTitle.setText(feedbackResultItem.title);
        mTvPercent.setText(feedbackResultItem.percent + "%");
        mTvCount.setText(String.format(mContext.getString(R.string.feedback_response_count), feedbackResultItem.responseCount));
        mProgressBar.setProgress((int) Math.ceil(feedbackResultItem.percent));
    }
}
