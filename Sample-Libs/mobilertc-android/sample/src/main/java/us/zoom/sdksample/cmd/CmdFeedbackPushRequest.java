package us.zoom.sdksample.cmd;

import androidx.annotation.Nullable;

public class CmdFeedbackPushRequest extends CmdRequest {
    private static final String TAG = "CmdFeedbackPushRequest";

    protected CmdType type = CmdType.FeedbackPush;

    @Override
    public CmdRequest builderRequest(String[] requestBody) {
        return this;
    }

    @Nullable
    @Override
    public String generateCmdString() {
        return "2";
    }
}
