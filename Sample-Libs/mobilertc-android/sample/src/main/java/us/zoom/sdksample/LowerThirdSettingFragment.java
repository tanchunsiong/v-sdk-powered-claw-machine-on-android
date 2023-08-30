package us.zoom.sdksample;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKErrors;
import us.zoom.sdk.ZoomVideoSDKSession;
import us.zoom.sdk.ZoomVideoSDKUser;
import us.zoom.sdk.ZoomVideoSDKVideoAspect;
import us.zoom.sdk.ZoomVideoSDKVideoHelper;
import us.zoom.sdk.ZoomVideoSDKVideoResolution;
import us.zoom.sdk.ZoomVideoSDKVideoView;
import us.zoom.sdksample.cmd.CmdLowerThirdRequest;
import us.zoom.sdksample.cmd.LowerThirdColorType;
import us.zoom.sdksample.rawdata.RawDataRenderer;
import us.zoom.sdksample.util.DisplayUtil;
import us.zoom.sdksample.util.SharePreferenceUtil;
import us.zoom.sdksample.view.CycleColorImageView;
import us.zoom.sdksample.view.LowerThirdLayout;

public class LowerThirdSettingFragment extends BottomSheetDialogFragment {

    public static final String TAG = "LowerThirdActivity";
    public static final String NAME_KEY = "name_key";
    public static final String COMPANY_KEY = "company_key";
    public static final String LOWER_THIRD_KEY = "LOWER_THIRD_KEY";
    public static final String RGB_KEY = "RGB_KEY";

    protected Activity context;
    private LowerThirdLayout lowerThirdLayout;
    protected TextView mSaveTv;
    protected TextView mCancelTv;

    private View mNameLayout;
    private EditText mNameEdt;

    private View mCompanyLayout;
    private EditText mCompanyEdt;

    private TextView mColorTipTv;
    private ImageView mDisableLockIv;
    private CycleColorImageView mDisableIv;
    private CycleColorImageView mPurpleIv;
    private CycleColorImageView mLightPurpleIv;
    private CycleColorImageView mGreenIv;
    private CycleColorImageView mOrangeIv;
    private CycleColorImageView mRedIv;
    private CycleColorImageView mYellowIv;
    private CycleColorImageView mBlueIv;

    private SwitchCompat mLowerConfigSwitch;
    private String originalName;
    private String originalCompany;
    private boolean originalSwitch;
    private LowerThirdColorType originalType;
    private LowerThirdColorType newColorType;
    ZoomVideoSDKVideoView zoomCanvas;

    private boolean isUseRawDataRender = true;
    private RawDataRenderer rawDataRenderer;

    private OnFinishListener listener;
    private ZoomVideoSDKVideoHelper videoHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_lower_third, container, false);
        initData();
        initView(view);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.context = (Activity) context;
        }
    }

    private void initData() {
        originalName = SharePreferenceUtil.readString(this.context, NAME_KEY, null);
        if (originalName == null || originalName.isEmpty()) {
            ZoomVideoSDKSession session = ZoomVideoSDK.getInstance().getSession();
            if (session != null) {
                originalName = session.getMySelf().getUserName();
            }
        }
        originalCompany = SharePreferenceUtil.readString(this.context, COMPANY_KEY, null);
        originalSwitch = SharePreferenceUtil.readBoolean(this.context, LOWER_THIRD_KEY, false);
        int rgbIndex = SharePreferenceUtil.readInt(this.context, RGB_KEY, 0);
        originalType = CmdLowerThirdRequest.getColorTypeFromIndex(rgbIndex);
        videoHelper = ZoomVideoSDK.getInstance().getVideoHelper();
    }

    private void initView(View rootView) {
        lowerThirdLayout = rootView.findViewById(R.id.layout_lower_third);
        mSaveTv = rootView.findViewById(R.id.saveTv);
        mCancelTv = rootView.findViewById(R.id.cancelTv);
        mLowerConfigSwitch = rootView.findViewById(R.id.lowerThirdSwitch);

        mNameLayout = rootView.findViewById(R.id.nameLayout);
        mNameEdt = rootView.findViewById(R.id.name_input);

        mCompanyLayout = rootView.findViewById(R.id.companyLayout);
        mCompanyEdt = rootView.findViewById(R.id.company_input);

        mColorTipTv = rootView.findViewById(R.id.colorTipTv);
        mDisableIv = rootView.findViewById(R.id.disableIv);
        mDisableLockIv = rootView.findViewById(R.id.disableLockIv);
        mPurpleIv = rootView.findViewById(R.id.purpleIv);
        mLightPurpleIv = rootView.findViewById(R.id.lightPurpleIv);
        mGreenIv = rootView.findViewById(R.id.greenIv);
        mOrangeIv = rootView.findViewById(R.id.orangeIv);
        mRedIv = rootView.findViewById(R.id.redIv);
        mYellowIv = rootView.findViewById(R.id.yellowIv);
        mBlueIv = rootView.findViewById(R.id.blueIv);

        if (originalName != null) {
            mNameEdt.setText(originalName);
            lowerThirdLayout.updateNameTv(originalName);
        }

        if (originalCompany != null) {
            mCompanyEdt.setText(originalCompany);
            lowerThirdLayout.updateCompanyTv(originalCompany);
        }

        if (originalCompany == null || originalCompany.isEmpty()) {
            lowerThirdLayout.updateCompanyTv(originalCompany);
        }

        mNameEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null) {
                    lowerThirdLayout.updateNameTv(s.toString());
                    setSaveEnable();
                    updateLowerThirdLayoutVisible(!s.toString().isEmpty());
                } else {
                    updateLowerThirdLayoutVisible(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mCompanyEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null) {
                    lowerThirdLayout.updateCompanyTv(s.toString());
                    setSaveEnable();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mCancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentFinish(false);
            }
        });

        mLowerConfigSwitch.setChecked(originalSwitch);

        mLowerConfigSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateLowerThirdLayoutVisible(isChecked);
                setConfigViewShow(isChecked);
                SharePreferenceUtil.saveBoolean(LowerThirdSettingFragment.this.context, LOWER_THIRD_KEY, isChecked);
            }
        });

        mSaveTv.setEnabled(false);
        mSaveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNameEdt != null) {
                    SharePreferenceUtil.saveString(LowerThirdSettingFragment.this.context, NAME_KEY, mNameEdt.getText().toString());
                }
                if (mCompanyEdt != null) {
                    SharePreferenceUtil.saveString(LowerThirdSettingFragment.this.context, COMPANY_KEY, mCompanyEdt.getText().toString());
                }
                SharePreferenceUtil.saveInt(LowerThirdSettingFragment.this.context, RGB_KEY, newColorType.ordinal());
                onFragmentFinish(true);
            }
        });
        initSelectedColor();

        mDisableIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSelectedColor(mDisableIv);
                updateSelectedColor(LowerThirdColorType.Disabled);
            }
        });

        mPurpleIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSelectedColor(mPurpleIv);
                updateSelectedColor(LowerThirdColorType.Purple);
            }
        });

        mLightPurpleIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSelectedColor(mLightPurpleIv);
                updateSelectedColor(LowerThirdColorType.Light_purple);
            }
        });

        mGreenIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSelectedColor(mGreenIv);
                updateSelectedColor(LowerThirdColorType.Green);
            }
        });

        mOrangeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSelectedColor(mOrangeIv);
                updateSelectedColor(LowerThirdColorType.Orange);
            }
        });

        mRedIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSelectedColor(mRedIv);
                updateSelectedColor(LowerThirdColorType.Red);
            }
        });

        mYellowIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSelectedColor(mYellowIv);
                updateSelectedColor(LowerThirdColorType.Yellow);
            }
        });

        mBlueIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSelectedColor(mBlueIv);
                updateSelectedColor(LowerThirdColorType.Blue);
            }
        });
        setConfigViewShow(originalSwitch);
        updateLowerThirdLayoutVisible(originalSwitch && (originalName != null && !originalName.isEmpty()));

        if (ZoomVideoSDK.getInstance().getSession() != null) {
            ZoomVideoSDKUser mySelf = ZoomVideoSDK.getInstance().getSession().getMySelf();
            Log.e(TAG, "is Video On : getVideoStatus: " + mySelf.getVideoStatus().isOn());
            Log.e(TAG, "is Video On : getVideoCanvas.getVideoStatus: " + mySelf.getVideoCanvas().getVideoStatus().isOn());
            Log.e(TAG, "is Video On : getVideoPipe.getVideoStatus: " + mySelf.getVideoPipe().getVideoStatus().isOn());
            if (mySelf.getVideoStatus().isOn()) {
                FrameLayout frameLayout = rootView.findViewById(R.id.previewLayout);
                if (!isUseRawDataRender) {
                    zoomCanvas = new ZoomVideoSDKVideoView(context, false);
                    zoomCanvas.setOutlineProvider(new ViewOutlineProvider() {
                        @Override
                        public void getOutline(View view, Outline outline) {
                            Rect rect = new Rect();
                            view.getGlobalVisibleRect(rect);
                            Rect selfRect = new Rect(0, 0, rect.right - rect.left, rect.bottom - rect.top);
                            outline.setRoundRect(selfRect, DisplayUtil.dp2pix(LowerThirdSettingFragment.this.context, 16));
                        }
                    });
                    zoomCanvas.setClipToOutline(true);
                    zoomCanvas.setZOrderMediaOverlay(true);
                }
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                if (!isUseRawDataRender) {
                    frameLayout.addView(zoomCanvas, 0, params);
                } else {
                    rawDataRenderer = new RawDataRenderer(context);
                    frameLayout.addView(rawDataRenderer, 0, params);
                }
                subscribeVideoByUser(mySelf);
                rootView.findViewById(R.id.previewIv).setVisibility(View.GONE);
            } else {
                rootView.findViewById(R.id.previewIv).setVisibility(View.VISIBLE);
            }
        } else {
            rootView.findViewById(R.id.previewIv).setVisibility(View.VISIBLE);
        }
    }

    private void updateSelectedColor(CycleColorImageView view) {
        if (view.isSelected()) {
            return;
        }
        CycleColorImageView[] views = new CycleColorImageView[]{mDisableIv, mPurpleIv, mLightPurpleIv, mGreenIv, mOrangeIv, mRedIv, mYellowIv, mBlueIv};
        for (CycleColorImageView itemView: views) {
            if (itemView.isSelected()) {
                itemView.setSelected(false);
            } else {
                if (view == itemView) {
                    view.setSelected(true);
                }
            }
        }
    }

    private void updateSelectedColor(LowerThirdColorType type) {
        newColorType = type;
        lowerThirdLayout.updateColor(type);
        setSaveEnable();
    }

    private void initSelectedColor() {
        LowerThirdColorType type = originalType;
        CycleColorImageView view = null;
        switch (type) {
            case Disabled:
                view = mDisableIv;
                break;
            case Purple:
                view = mPurpleIv;
                break;
            case Light_purple:
                view = mLightPurpleIv;
                break;
            case Green:
                view = mGreenIv;
                break;
            case Orange:
                view = mOrangeIv;
                break;
            case Red:
                view = mRedIv;
                break;
            case Yellow:
                view = mYellowIv;
                break;
            case Blue:
                view = mBlueIv;
                break;
        }
        updateSelectedColor(view);
        updateSelectedColor(type);
    }

    private void setSaveEnable() {
        String nameString = mNameEdt.getText().toString();
        String companyString = mCompanyEdt.getText().toString();
        if (nameString.isEmpty()) {
            /* save state must set name */
            mSaveTv.setEnabled(false);
            return;
        }
        mSaveTv.setEnabled(!nameString.equals(originalName) || !companyString.equals(originalCompany) || (newColorType != originalType));
    }

    private void setConfigViewShow(boolean show) {
        int visibility = show ? View.VISIBLE : View.INVISIBLE;
        mNameLayout.setVisibility(visibility);
        mNameEdt.setVisibility(visibility);

        mCompanyLayout.setVisibility(visibility);
        mCompanyEdt.setVisibility(visibility);

        mColorTipTv.setVisibility(visibility);
        mDisableIv.setVisibility(visibility);
        mDisableLockIv.setVisibility(visibility);
        mPurpleIv.setVisibility(visibility);
        mLightPurpleIv.setVisibility(visibility);
        mGreenIv.setVisibility(visibility);
        mOrangeIv.setVisibility(visibility);
        mRedIv.setVisibility(visibility);
        mYellowIv.setVisibility(visibility);
        mBlueIv.setVisibility(visibility);
    }

    private void subscribeVideoByUser(ZoomVideoSDKUser user) {
        ZoomVideoSDKVideoAspect aspect = ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_PanAndScan;
        int ret= ZoomVideoSDKErrors.Errors_Success;
        if (!isUseRawDataRender) {
            videoHelper.startVideoCanvasPreview(zoomCanvas, aspect);
        } else {
            videoHelper.startVideoPreview(rawDataRenderer);
        }
        if(ret!= ZoomVideoSDKErrors.Errors_Success)

        {
            Toast.makeText(this.context,"subscribe error:"+ret,Toast.LENGTH_LONG).show();
        }
    }

    private void updateLowerThirdLayoutVisible(boolean isShow) {
        isShow = isShow && mLowerConfigSwitch.isChecked();
        String name = mNameEdt.getText().toString();
        isShow = (name != null && !name.isEmpty()) && isShow;
        lowerThirdLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    /**
     * interface
     */
    public interface OnFinishListener {
        void onFinishClicked(boolean save);
    }

    public void setFinishListener(OnFinishListener listener) {
        this.listener = listener;
    }

    private void onFragmentFinish(boolean save) {
        if (listener != null) {
            listener.onFinishClicked(save);
        }
        if (!isUseRawDataRender) {
            if (ZoomVideoSDK.getInstance().getSession() != null && zoomCanvas != null) {
                videoHelper.stopVideoCanvasPreview(zoomCanvas);
            }
        } else {
            if (ZoomVideoSDK.getInstance().getSession() != null && rawDataRenderer != null) {
                videoHelper.stopVideoPreview(rawDataRenderer);
            }
        }
    }
}
