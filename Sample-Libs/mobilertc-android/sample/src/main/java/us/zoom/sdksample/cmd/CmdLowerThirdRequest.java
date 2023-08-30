package us.zoom.sdksample.cmd;

import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import us.zoom.sdksample.R;

public class CmdLowerThirdRequest extends CmdRequest {
    private static final String TAG = "CmdLowerThirdRequest";

    public String name = "";
    public String companyName = "";
    public LowerThirdColorType rgb = LowerThirdColorType.Disabled;

    @Override
    public CmdRequest builderRequest(String[] requestBody) {
        if (requestBody.length < 4) {
            return this;
        }
        this.name = requestBody[1];
        this.companyName = requestBody[2];
        this.rgb = getRgb(requestBody[3]);
        return this;
    }

    @Nullable
    @Override
    public String generateCmdString() {
        String rgb = null;
        switch (this.rgb) {
            case Disabled:
                rgb = "#444B53";
                break;
            case Purple:
                rgb = "#493AB7";
                break;
            case Light_purple:
                rgb = "#A477FF";
                break;
            case Green:
                rgb = "#66CC84";
                break;
            case Orange:
                rgb = "#FF8422";
                break;
            case Red:
                rgb = "#FD3D4A";
                break;
            case Yellow:
                rgb = "#FFBF39";
                break;
            case Blue:
                rgb = "#1E71D6";
                break;
        }
        return "4|" + name + "|" + companyName + "|" + rgb;
    }

    private LowerThirdColorType getRgb(String rgb) {
        switch (rgb) {
            case "#1E71D6":
                return LowerThirdColorType.Blue;
            case "#FFBF39":
                return LowerThirdColorType.Yellow;
            case "#FD3D4A":
                return LowerThirdColorType.Red;
            case "#FF8422":
                return LowerThirdColorType.Orange;
            case "#66CC84":
                return LowerThirdColorType.Green;
            case "#A477FF":
                return LowerThirdColorType.Light_purple;
            case "#493AB7":
                return LowerThirdColorType.Purple;
            case "null":
            case "#444B53":
            default:
                return LowerThirdColorType.Disabled;
        }
    }

    public static LowerThirdColorType getColorTypeFromIndex(int index) {
        LowerThirdColorType type = LowerThirdColorType.Disabled;
        try {
            type = LowerThirdColorType.values()[index];
        } catch (Exception e) {
            Log.e(TAG, "wrong color index");
        }
        return type;
    }

    public static int getColorIDFromColorType(LowerThirdColorType type) {
        int colorID = 0;
        switch (type) {
            case Disabled:
                colorID = R.color.lower_third_disable_show;
                break;
            case Purple:
                colorID = R.color.lower_third_purple;
                break;
            case Light_purple:
                colorID = R.color.lower_third_light_purple;
                break;
            case Green:
                colorID = R.color.lower_third_green;
                break;
            case Orange:
                colorID = R.color.lower_third_orange;
                break;
            case Red:
                colorID = R.color.lower_third_red;
                break;
            case Yellow:
                colorID = R.color.lower_third_yellow;
                break;
            case Blue:
                colorID = R.color.lower_third_blue;
        }
        return colorID;
    }
}
