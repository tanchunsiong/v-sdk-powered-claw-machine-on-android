package us.zoom.sdksample.feedback.data;

import android.text.TextUtils;
import android.util.ArrayMap;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import us.zoom.sdksample.cmd.CmdFeedbackSubmitRequest;
import us.zoom.sdksample.cmd.CmdHandler;
import us.zoom.sdksample.cmd.CmdHelper;
import us.zoom.sdksample.cmd.CmdRequest;
import us.zoom.sdksample.cmd.FeedbackType;
import us.zoom.sdksample.feedback.model.FeedbackResultData;
import us.zoom.sdksample.feedback.model.FeedbackResultItem;

public class FeedbackDataManager {
    private volatile static FeedbackDataManager sInstance;
    private List<DataChangeListener> mListeners = new ArrayList<>();
    private ArrayMap<String, FeedbackType> mFeedbackTypes = new ArrayMap<>();
    private List<FeedbackResultItem> mResultItems = new ArrayList<>();
    private FeedbackResultData mResultData;
    private CmdHandler mCmdHandler = new CmdHandler() {
        @Override
        public void onCmdReceived(CmdRequest request) {
            if (request == null || request.user == null) {
                return;
            }

            String userId = request.user.getUserID();
            if (TextUtils.isEmpty(userId)) {
                return;
            }
            if (request instanceof CmdFeedbackSubmitRequest) {
                FeedbackType submitedFeedbackType = ((CmdFeedbackSubmitRequest) request).feedbackType;
                FeedbackType lastFeedbackType = mFeedbackTypes.get(userId);
                if (lastFeedbackType == null || lastFeedbackType != submitedFeedbackType) {
                    mFeedbackTypes.put(userId, submitedFeedbackType);
                    handleData();
                }
            }
        }
    };

    private void handleData() {
        clearResultData();
        mResultData.feedbackCount = mFeedbackTypes.size();
        FeedbackResultItem resultItem;
        for (FeedbackType feedbackType : mFeedbackTypes.values()) {
            switch (feedbackType) {
                case Very_Satisfied:
                    resultItem = mResultItems.get(0);
                    break;
                case Satisfied:
                    resultItem = mResultItems.get(1);
                    break;
                case Neutral:
                    resultItem = mResultItems.get(2);
                    break;
                case Unsatisfied:
                    resultItem = mResultItems.get(3);
                    break;
                case Very_Unsatisfied:
                    resultItem = mResultItems.get(4);
                    break;
                default:
                    resultItem = mResultItems.get(0);
                    break;
            }
            resultItem.responseCount++;
        }

        int sumPercent = 0;
        for (int i = 0; i < mResultItems.size(); i++) {
            FeedbackResultItem item = mResultItems.get(i);
            if (i == mResultItems.size() - 1) {
                item.percent = 100 - sumPercent;
            } else {
                item.percent = (item.responseCount * 100) / mResultData.feedbackCount;
                sumPercent += item.percent;
            }
        }

        for (DataChangeListener listener : mListeners) {
            listener.onDataChange(mResultData);
        }
    }

    private FeedbackDataManager() {
        mResultData = new FeedbackResultData();
        mResultData.resultItems = mResultItems;
        mResultItems.add(new FeedbackResultItem(FeedbackType.Very_Satisfied));
        mResultItems.add(new FeedbackResultItem(FeedbackType.Satisfied));
        mResultItems.add(new FeedbackResultItem(FeedbackType.Neutral));
        mResultItems.add(new FeedbackResultItem(FeedbackType.Unsatisfied));
        mResultItems.add(new FeedbackResultItem(FeedbackType.Very_Unsatisfied));
    }

    public static FeedbackDataManager getInstance() {
        if (sInstance == null) {
            synchronized (FeedbackDataManager.class) {
                if (sInstance == null) {
                    sInstance = new FeedbackDataManager();
                }
            }
        }

        return sInstance;
    }

    public void startListenerFeedbackData() {
        CmdHelper.getInstance().addListener(mCmdHandler);
    }

    public void stopListenerFeedbackData() {
        CmdHelper.getInstance().removeListener(mCmdHandler);
    }

    @NonNull
    public FeedbackResultData getFeedbackData() {
        return mResultData;
    }

    public int getFeedbackCount() {
        return mResultData.feedbackCount;
    }

    public void addDataListener(DataChangeListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeDataListener(DataChangeListener listener) {
        mListeners.remove(listener);
    }

    public void clear() {
        mFeedbackTypes.clear();
        clearResultData();
    }

    private void clearResultData() {
        mResultData.feedbackCount = 0;
        for (FeedbackResultItem item : mResultItems) {
            item.percent = 0;
            item.responseCount = 0;
        }
    }

    public interface DataChangeListener {
        void onDataChange(FeedbackResultData feedbackData);
    }
}
