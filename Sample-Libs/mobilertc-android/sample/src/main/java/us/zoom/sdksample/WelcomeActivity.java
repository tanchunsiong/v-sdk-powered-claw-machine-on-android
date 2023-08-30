package us.zoom.sdksample;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKErrors;
import us.zoom.sdk.ZoomVideoSDKExtendParams;
import us.zoom.sdk.ZoomVideoSDKInitParams;
import us.zoom.sdk.ZoomVideoSDKRawDataMemoryMode;
import us.zoom.sdksample.util.ErrorMsgUtil;
import us.zoom.sdksample.view.TopCropImageView;

public class WelcomeActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private DrawerLayout mDrawerLayout;
    private LinearLayout mPanelDots;
    private ViewPagerAdapter mAdapter;
    private List<View> pages;

    private int currentIndex = 0;
    private final int ImageIds[] = new int[]{R.drawable.page_one, R.drawable.page_two, R.drawable.page_three, R.drawable.page_four, R.drawable.page_five, R.drawable.page_six};

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        initView();
        initPages();
        initDots();
        initSDK();
    }

    private void initPages() {
        pages = new ArrayList<>();
        TopCropImageView view = null;
        for (int i = 0; i < ImageIds.length; i++) {
            view = new TopCropImageView(this);
            view.setImageResource(ImageIds[i]);
            pages.add(view);
        }
        mAdapter = new ViewPagerAdapter(pages);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setCurDot(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initDots() {
        mPanelDots.removeAllViews();
        currentIndex = 0;
        ImageView dot;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.dp_7), getResources().getDimensionPixelSize(R.dimen.dp_7));
        int margin = getResources().getDimensionPixelSize(R.dimen.dp_5);
        layoutParams.setMargins(margin, 0, margin, 0);
        for (int i = 0; i < ImageIds.length; i++) {
            dot = new ImageView(this);

            dot.setLayoutParams(layoutParams);
            dot.setImageResource(R.drawable.dot_selector);
            dot.setEnabled(currentIndex == i);
            dot.setTag(i);
            mPanelDots.addView(dot, i);
        }
    }

    private void initView() {
        mViewPager = findViewById(R.id.viewPager);
        mPanelDots = findViewById(R.id.llDots);
        mDrawerLayout = findViewById(R.id.drawerLayout);
    }

    public void showDrawer(View view) {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    public void gotoSetting(View view) {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        Intent intent = new Intent(this, SettingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
    }

    public void gotoDocument(View view) {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        Uri uri = Uri.parse("https://marketplace.zoom.us");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void gotoCreate(View view) {
        Intent intent = new Intent(this, CreateSessionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void gotoJoin(View view) {
        Intent intent = new Intent(this, JoinSessionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void setCurDot(int position) {
        if (position < 0 || position > ImageIds.length || currentIndex == position) {
            return;
        }
        mPanelDots.getChildAt(position).setEnabled(true);
        mPanelDots.getChildAt(currentIndex).setEnabled(false);
        currentIndex = position;
    }

    class ViewPagerAdapter extends PagerAdapter {
        private List<View> views;

        public ViewPagerAdapter(List<View> views) {
            super();
            this.views = views;
        }

        @Override
        public int getCount() {
            if (views != null) {
                return views.size();
            }
            return 0;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(views.get(position), 0);
            return views.get(position);
        }

    }

    protected void initSDK() {
        ZoomVideoSDKInitParams params = new ZoomVideoSDKInitParams();
        params.domain = Constants.WEB_DOMAIN;
        params.enableLog = true;
        params.videoRawDataMemoryMode = ZoomVideoSDKRawDataMemoryMode.ZoomVideoSDKRawDataMemoryModeHeap;
        params.audioRawDataMemoryMode = ZoomVideoSDKRawDataMemoryMode.ZoomVideoSDKRawDataMemoryModeHeap;
        params.shareRawDataMemoryMode = ZoomVideoSDKRawDataMemoryMode.ZoomVideoSDKRawDataMemoryModeHeap;

        params.extendParam=new ZoomVideoSDKExtendParams();
        params.extendParam.speakerTestFilePath="/sdcard/Android/data/us.zoom.VideoSDKPlaygroud/files/test.mp3";

        int ret = ZoomVideoSDK.getInstance().initialize(this.getApplicationContext(), params);
        if (ret != ZoomVideoSDKErrors.Errors_Success) {
            Toast.makeText(this, ErrorMsgUtil.getMsgByErrorCode(ret), Toast.LENGTH_LONG).show();
        }else {
            ((TextView) findViewById(R.id.text_version)).setText(getString(R.string.launch_setting_version, ZoomVideoSDK.getInstance().getSDKVersion()));
        }
    }
}
