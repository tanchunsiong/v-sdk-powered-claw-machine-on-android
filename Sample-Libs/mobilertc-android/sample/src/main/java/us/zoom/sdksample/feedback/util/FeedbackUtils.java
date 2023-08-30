package us.zoom.sdksample.feedback.util;

import us.zoom.sdksample.cmd.CmdFeedbackPushRequest;
import us.zoom.sdksample.cmd.CmdFeedbackSubmitRequest;
import us.zoom.sdksample.cmd.CmdHelper;
import us.zoom.sdksample.cmd.FeedbackType;

public class FeedbackUtils {
    public static void submitFeedback(FeedbackType type) {
        CmdFeedbackSubmitRequest request = new CmdFeedbackSubmitRequest();
        request.feedbackType = type;
        CmdHelper.getInstance().sendCommand(request);
    }

    public static void pushFeedback() {
        CmdFeedbackPushRequest request = new CmdFeedbackPushRequest();
        CmdHelper.getInstance().sendCommand(request);
    }
}
