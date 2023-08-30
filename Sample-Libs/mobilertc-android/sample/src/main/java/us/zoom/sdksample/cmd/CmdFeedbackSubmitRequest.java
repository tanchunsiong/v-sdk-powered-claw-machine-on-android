package us.zoom.sdksample.cmd;

import androidx.annotation.Nullable;

public class CmdFeedbackSubmitRequest extends CmdRequest {
    protected CmdType type = CmdType.FeedbackSubmit;
    public FeedbackType feedbackType = FeedbackType.None;

    @Override
    public CmdRequest builderRequest(String[] requestBody) {
        if (requestBody.length < 2) {
            this.feedbackType = FeedbackType.None;
            return this;
        }
        switch (requestBody[1]) {
            case "verySatisfied":
                this.feedbackType = FeedbackType.Very_Satisfied;
                break;
            case "satisfied":
                this.feedbackType = FeedbackType.Satisfied;
                break;
            case "neutral":
                this.feedbackType = FeedbackType.Neutral;
                break;
            case "unsatisfied":
                this.feedbackType = FeedbackType.Unsatisfied;
                break;
            case "veryUnsatisfied":
                this.feedbackType = FeedbackType.Very_Unsatisfied;
                break;
            default:
                this.feedbackType = FeedbackType.None;
                break;
        }
        return this;
    }

    @Nullable
    @Override
    public String generateCmdString() {
        switch (this.feedbackType) {
            case Very_Satisfied:
                return "3|verySatisfied";
            case Satisfied:
                return "3|satisfied";
            case Neutral:
                return "3|neutral";
            case Unsatisfied:
                return "3|unsatisfied";
            case Very_Unsatisfied:
                return "3|veryUnsatisfied";
            default:
                return null;
        }
    }
}
