package us.zoom.sdksample.cmd;

import androidx.annotation.Nullable;

import us.zoom.sdksample.R;
import us.zoom.sdksample.cmd.CmdRequest;
import us.zoom.sdksample.cmd.CmdType;
import us.zoom.sdksample.cmd.EmojiReactionType;

public class CmdReactionRequest extends CmdRequest {
    public static final int EMOJI_DISMISS_TIME_OUT = 5;

    protected CmdType type = CmdType.Reaction;
    public EmojiReactionType reactionType;
    public int emojiReactionResID;
    public int dismissTimeLeftInSeconds = EMOJI_DISMISS_TIME_OUT;
    public boolean isHandRaised = false;

    @Override
    public CmdRequest builderRequest(String[] requestBody) {
        if (requestBody.length < 2) {
            this.reactionType = EmojiReactionType.None;
            return this;
        }
        switch (requestBody[1]) {
            case "clap":
                this.reactionType = EmojiReactionType.Clap;
                break;
            case "thumbsup":
                this.reactionType = EmojiReactionType.Thumbsup;
                break;
            case "joy":
                this.reactionType = EmojiReactionType.Joy;
                break;
            case "hushed":
                this.reactionType = EmojiReactionType.Openmouth;
                break;
            case "heart":
                this.reactionType = EmojiReactionType.Heart;
                break;
            case "tada":
                this.reactionType = EmojiReactionType.Tada;
                break;
            case "raisehand":
                this.reactionType = EmojiReactionType.RaisedHand;
                isHandRaised = true;
                break;
            case "lowerhand":
                this.reactionType = EmojiReactionType.LowHand;
                isHandRaised = false;
                break;
            default:
                this.reactionType = EmojiReactionType.None;
                break;
        }
        emojiReactionResID = getReactionResId(this.reactionType);
        return this;
    }

    @Nullable
    @Override
    public String generateCmdString() {
        switch (reactionType) {
            case Clap:
                return "1|clap";
            case Thumbsup:
                return "1|thumbsup";
            case Joy:
                return "1|joy";
            case Openmouth:
                return "1|hushed";
            case Heart:
                return "1|heart";
            case Tada:
                return "1|tada";
            case RaisedHand:
                return "1|raisehand";
            case LowHand:
                return "1|lowerhand";
            default:
                return null;
        }
    }

    public static int getReactionResId(EmojiReactionType type) {
        int resId = 0;
        switch (type) {
            case Clap:
                resId = R.drawable.reaction_1f44f;
                break;
            case Thumbsup:
                resId = R.drawable.reaction_1f44d;
                break;
            case Heart:
                resId = R.drawable.reaction_2764;
                break;
            case Joy:
                resId = R.drawable.reaction_1f602;
                break;
            case Openmouth:
                resId = R.drawable.reaction_1f62e;
                break;
            case Tada:
                resId = R.drawable.reaction_1f389;
                break;
            case RaisedHand:
                resId = R.drawable.reaction_raisehand;
                break;
        }
        return resId;
    }
}
