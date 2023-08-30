package us.zoom.sdksample;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import us.zoom.sdksample.cmd.CmdHelper;
import us.zoom.sdksample.cmd.CmdLowerThirdRequest;
import us.zoom.sdksample.util.DisplayUtil;
import us.zoom.sdksample.util.SharePreferenceUtil;

public class LowerThirdSettingBottomFragment extends LowerThirdSettingFragment {

    public static LowerThirdSettingBottomFragment newInstance() {
        final LowerThirdSettingBottomFragment bottomDialogFragment = new LowerThirdSettingBottomFragment();
        bottomDialogFragment.setFinishListener(new OnFinishListener() {
            @Override
            public void onFinishClicked(boolean save) {
                if (save) {
                    /* the switch only control the lower third show, the cmd always send */
                    String name = SharePreferenceUtil.readString(bottomDialogFragment.context, LowerThirdSettingFragment.NAME_KEY, "");
                    String company = SharePreferenceUtil.readString(bottomDialogFragment.context, LowerThirdSettingFragment.COMPANY_KEY, "");
                    int rgbIndex = SharePreferenceUtil.readInt(bottomDialogFragment.context, LowerThirdSettingFragment.RGB_KEY, 0);

                    final CmdLowerThirdRequest request = new CmdLowerThirdRequest();
                    request.user = null;
                    request.name = name;
                    request.companyName = company;
                    request.rgb = CmdLowerThirdRequest.getColorTypeFromIndex(rgbIndex);

                    CmdHelper.getInstance().sendCommand(request);
                }

                boolean lowerThirdEnable = SharePreferenceUtil.readBoolean(bottomDialogFragment.context, LowerThirdSettingFragment.LOWER_THIRD_KEY, false);
                if (lowerThirdEnable) {
                    if (bottomDialogFragment.lowerThirdDisableListener != null) {
                        bottomDialogFragment.lowerThirdDisableListener.onLowerThirdEnabled();
                    }
                } else {
                    if (bottomDialogFragment.lowerThirdDisableListener != null) {
                        bottomDialogFragment.lowerThirdDisableListener.onLowerThirdDisabled();
                    }
                }

                bottomDialogFragment.dismiss();
                if (bottomDialogFragment.context != null) {
                    bottomDialogFragment.context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
        });
        return bottomDialogFragment;
    }

    public LowerThirdDisableListener lowerThirdDisableListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (this.context != null) {
            this.context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.setBackground(getResources().getDrawable(R.drawable.lower_third_fragment_bg));
            mCancelTv.setText("Close");
            TextView titleTv = view.findViewById(R.id.titleTv);
            if (titleTv != null) {
                titleTv.setText("Preview");
            }
        }
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                setupFullHeight((BottomSheetDialog) dialog);
            }
        });
        return dialog;
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
            ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

            int windowHeight = getWindowHeight();
            if (layoutParams != null) {
                layoutParams.height = windowHeight;
            }
            bottomSheet.setLayoutParams(layoutParams);
            bottomSheet.setBackgroundColor(getResources().getColor(R.color.transparent));
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels - getStatusBarHeight();
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        } else {
            result = (int) DisplayUtil.dp2pix(context, 25);
        }
        return result;
    }

    /**
     * interface
     */
    public interface LowerThirdDisableListener {
        void onLowerThirdDisabled();
        void onLowerThirdEnabled();
    }
}
