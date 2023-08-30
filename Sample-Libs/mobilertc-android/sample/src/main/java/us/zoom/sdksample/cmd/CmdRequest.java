package us.zoom.sdksample.cmd;

import android.util.Log;

import androidx.annotation.Nullable;

import us.zoom.sdk.ZoomVideoSDKUser;

public abstract class CmdRequest {
    private static final String TAG = "";
    protected CmdType type;
    public ZoomVideoSDKUser user;

    @Nullable
    private static CmdRequest getRequest(CmdType type) {
        switch (type) {
            case Reaction:
                return new CmdReactionRequest();
            case FeedbackPush:
                return new CmdFeedbackPushRequest();
            case FeedbackSubmit:
                return new CmdFeedbackSubmitRequest();
            case LowerThird:
                return new CmdLowerThirdRequest();
            default:
                return null;
        }
    }

    public abstract CmdRequest builderRequest(String[] requestBody);

    @Nullable
    public static CmdRequest getRequest(ZoomVideoSDKUser sender, @Nullable String strCmd) {
        if (strCmd == null) {
            return null;
        }

        String[] strings = strCmd.split("\\|");
        if (strings.length < 1) {
            return null;
        }
        CmdType cmdType = CmdType.None;
        try {
            int type = Integer.parseInt(strings[0]);
            cmdType = CmdType.values()[type];
        } catch (Exception e) {
            Log.e(TAG, "the cmd type can not be Recognized");
            return null;
        }

        CmdRequest cmdRequest = getRequest(cmdType);
        if (cmdRequest != null) {
            cmdRequest.user = sender;
            cmdRequest.builderRequest(strings);
        }

        return cmdRequest;
    }

    @Nullable
    public abstract String generateCmdString();
}
