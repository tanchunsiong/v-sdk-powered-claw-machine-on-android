package us.zoom.sdksample.feedback.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import us.zoom.sdksample.R;

public class FeedbackPushConfirmDialog extends BaseFeedbackDialog {
    private OnClickPushListener mClickPushListener;
    public static void show(FragmentActivity activity, OnClickPushListener listener) {
        FeedbackPushConfirmDialog dialog = new FeedbackPushConfirmDialog();
        dialog.setOnClickPushListener(listener);
        dialog.show(activity.getSupportFragmentManager(), "PushFeedbackResultConfirmDialog");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_feedback_push_confirm, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        if (getActivity() == null) {
            return;
        }

        view.findViewById(R.id.tv_push).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPush();
            }
        });

        view.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCancel();
            }
        });
    }

    public void onClickPush() {
        dismiss();
        if (mClickPushListener != null) {
            mClickPushListener.onClickPush();
        }
    }

    public void onClickCancel() {
        dismiss();
    }

    public void setOnClickPushListener(OnClickPushListener listener) {
        mClickPushListener = listener;
    }

    public interface OnClickPushListener {
        void onClickPush();
    }
}
