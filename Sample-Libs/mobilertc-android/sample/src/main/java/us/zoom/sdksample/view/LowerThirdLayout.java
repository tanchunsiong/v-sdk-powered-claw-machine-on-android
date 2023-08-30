package us.zoom.sdksample.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import us.zoom.sdksample.R;
import us.zoom.sdksample.cmd.CmdLowerThirdRequest;
import us.zoom.sdksample.cmd.LowerThirdColorType;
import us.zoom.sdksample.util.DisplayUtil;

public class LowerThirdLayout extends ConstraintLayout {
    private ConstraintLayout lowerThirdLayout;
    private TextView mNameTv;
    private TextView mCompanyTv;
    private View mLowerThirdLine;
    private VectorDrawableCompat vectorDrawableCompat;
    private GradientDrawable lineDrawable;
    private Context context;

    public LowerThirdLayout(@NonNull Context context) {
        this(context, null);
    }

    public LowerThirdLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LowerThirdLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LowerThirdLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater.from(context).inflate(R.layout.layout_lower_third, this);
        this.context = context;
        initView();
    }

    private void initView() {
        lowerThirdLayout = findViewById(R.id.llLowerThird);
        mNameTv = findViewById(R.id.userNameTv);
        mCompanyTv = findViewById(R.id.companyTv);
        mLowerThirdLine = findViewById(R.id.lower_third_line);
        lineDrawable = (GradientDrawable) mLowerThirdLine.getBackground();
    }

    private void updateLowerThirdLayout() {
        ConstraintLayout.LayoutParams nameTvLayoutParams = (ConstraintLayout.LayoutParams) mNameTv.getLayoutParams();
        if (mCompanyTv.getText().toString().isEmpty()) {
            ViewGroup.LayoutParams layoutParams = lowerThirdLayout.getLayoutParams();
            layoutParams.width = (int) DisplayUtil.dp2pix(context, 152);
            layoutParams.height = (int) DisplayUtil.dp2pix(context, 48);
            mCompanyTv.setVisibility(View.GONE);
            nameTvLayoutParams.topMargin = (int) DisplayUtil.dp2pix(context, 14);
            mNameTv.setLayoutParams(nameTvLayoutParams);
            lowerThirdLayout.setLayoutParams(layoutParams);
        } else {
            ViewGroup.LayoutParams layoutParams = lowerThirdLayout.getLayoutParams();
            layoutParams.width = (int) DisplayUtil.dp2pix(context, 198);
            layoutParams.height = (int) DisplayUtil.dp2pix(context, 56);
            mCompanyTv.setVisibility(View.VISIBLE);
            nameTvLayoutParams.topMargin = (int) DisplayUtil.dp2pix(context, 8);
            mNameTv.setLayoutParams(nameTvLayoutParams);
            lowerThirdLayout.setLayoutParams(layoutParams);
        }
    }

    public void updateColor(LowerThirdColorType type) {
        int colorID = CmdLowerThirdRequest.getColorIDFromColorType(type);
        int colorResID = getResources().getColor(colorID);
        lineDrawable.setColor(colorResID);
        vectorDrawableCompat = VectorDrawableCompat.create(getResources(), R.drawable.ic_frame_221, this.context.getTheme());
        if (vectorDrawableCompat != null) {
            vectorDrawableCompat.setTint(colorResID);
            mCompanyTv.setBackground(vectorDrawableCompat);
        }
        if (type == LowerThirdColorType.Green || type == LowerThirdColorType.Yellow || type == LowerThirdColorType.Orange) {
            mCompanyTv.setTextColor(getResources().getColor(R.color.text_black));
        } else {
            mCompanyTv.setTextColor(getResources().getColor(R.color.text_grey));
        }
    }

    public void updateNameTv(String name) {
        if (mNameTv != null) {
            mNameTv.setText(name);
        }
    }

    public void updateCompanyTv(String company) {
        if (mCompanyTv != null) {
            if (company != null) {
                mCompanyTv.setText(company);
            } else {
                mCompanyTv.setText("");
            }
            updateLowerThirdLayout();
        }
    }
}
