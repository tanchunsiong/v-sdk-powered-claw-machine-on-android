package us.zoom.sdksample.share;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

import us.zoom.sdk.ZoomVideoSDK;
import us.zoom.sdk.ZoomVideoSDKAnnotationClearType;
import us.zoom.sdk.ZoomVideoSDKAnnotationToolType;
import us.zoom.sdk.ZoomVideoSDKAnnotationHelper;
import us.zoom.sdk.ZoomVideoSDKAudioHelper;
import us.zoom.sdk.ZoomVideoSDKAudioRawData;
import us.zoom.sdk.ZoomVideoSDKCRCCallStatus;
import us.zoom.sdk.ZoomVideoSDKChatHelper;
import us.zoom.sdk.ZoomVideoSDKChatMessage;
import us.zoom.sdk.ZoomVideoSDKChatMessageDeleteType;
import us.zoom.sdk.ZoomVideoSDKChatPrivilegeType;
import us.zoom.sdk.ZoomVideoSDKDelegate;
import us.zoom.sdk.ZoomVideoSDKLiveStreamHelper;
import us.zoom.sdk.ZoomVideoSDKLiveStreamStatus;
import us.zoom.sdk.ZoomVideoSDKLiveTranscriptionHelper;
import us.zoom.sdk.ZoomVideoSDKMultiCameraStreamStatus;
import us.zoom.sdk.ZoomVideoSDKNetworkStatus;
import us.zoom.sdk.ZoomVideoSDKPasswordHandler;
import us.zoom.sdk.ZoomVideoSDKPhoneFailedReason;
import us.zoom.sdk.ZoomVideoSDKPhoneStatus;
import us.zoom.sdk.ZoomVideoSDKProxySettingHandler;
import us.zoom.sdk.ZoomVideoSDKRawDataPipe;
import us.zoom.sdk.ZoomVideoSDKRecordingConsentHandler;
import us.zoom.sdk.ZoomVideoSDKRecordingStatus;
import us.zoom.sdk.ZoomVideoSDKSSLCertificateInfo;
import us.zoom.sdk.ZoomVideoSDKShareHelper;
import us.zoom.sdk.ZoomVideoSDKShareStatus;
import us.zoom.sdk.ZoomVideoSDKUser;
import us.zoom.sdk.ZoomVideoSDKUserHelper;
import us.zoom.sdk.ZoomVideoSDKVideoCanvas;
import us.zoom.sdk.ZoomVideoSDKVideoHelper;
import us.zoom.sdk.ZoomVideoSDKVideoSubscribeFailReason;
import us.zoom.sdk.ZoomVideoSDKVideoView;
import us.zoom.sdksample.R;

public class AnnotateToolbar extends FrameLayout implements IColorChangedListener, View.OnClickListener, ZoomVideoSDKDelegate {
	private ToolbarDragView mView;
	private ImageView mCloseBtn;
	private ImageView mAnnotateBtn;
	private View mToolbars;

	private int mLineWidth = 2; //dp

	private ColorTable mColorTable;
	private TextView txtLineWidth;
	private PopupWindow mColorTableView;
	private PopupWindow mSaveTableView;
	private SeekBar mLineWidthSeekBar;

	private ImageView mSpotlight;
	private ImageView mHighlight;
	private ImageView mPen;
	private ImageView mErase;
	private ImageView mArrow;
	private ImageView mClear;

	private View mColorIndicator;
	private ColorSelectedImage mColorImage;
	private ZoomVideoSDKVideoView videoSDKVideoView;
	private ZoomVideoSDKShareHelper shareHelper;
	private ZoomVideoSDKAnnotationHelper mAnnotationController;

	private final static int COLOR_SIZE_DEFAULT = 25;
	private final static int COLOR_SIZE_PRESSED = 33;
	private final static int DEFAULT_FONT_SIZE = 48;

	private final static String TAG = AnnotateToolbar.class.getSimpleName();

	public AnnotateToolbar(Context context) {
		super(context);
		init(context);
	}

	public AnnotateToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AnnotateToolbar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context){
		mView = (ToolbarDragView)LayoutInflater.from(context).inflate(R.layout.annotatebar, null, false);
		mView.getLayoutParams();
		mView.setGestureDetectorListener( new GuestureListener());
		initAnnotateView(context);
		this.addView(mView);
		ZoomVideoSDK.getInstance().addListener(this);
	}

	private void initAnnotateView(Context context)
	{
		videoSDKVideoView = null;
		shareHelper = ZoomVideoSDK.getInstance().getShareHelper();
		mAnnotateBtn = (ImageView) mView.findViewById(R.id.btnAnnotate);
		mAnnotateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showAnnoToolbar();
			}
		});
		mCloseBtn = (ImageView) mView.findViewById(R.id.shareEditBtn);
		mCloseBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				closeAnnoToolbar();
			}
		});

		mSpotlight = (ImageView) mView.findViewById(R.id.btnSpotlight);
		mHighlight = (ImageView) mView.findViewById(R.id.btnHighlight);
		mPen = (ImageView) mView.findViewById(R.id.btnPen);
		mErase = (ImageView) mView.findViewById(R.id.btnErase);
		mColorIndicator = mView.findViewById(R.id.btnColorIndicator);

		mColorImage = (ColorSelectedImage)mColorIndicator.findViewById(R.id.colorImage);
		mArrow = (ImageView) mView.findViewById(R.id.btnArrow);
		mClear = (ImageView) mView.findViewById(R.id.btnClear);



		mClear.setVisibility(VISIBLE);

		mArrow.setVisibility(GONE);
		mSpotlight.setVisibility(GONE);

		mSpotlight.setOnClickListener(this);
		mHighlight.setOnClickListener(this);
		mPen.setOnClickListener(this);
		mErase.setOnClickListener(this);
		mColorIndicator.setOnClickListener(this);
		mArrow.setOnClickListener(this);
		mClear.setOnClickListener(this);
		mView.findViewById(R.id.btnRedo).setOnClickListener(this);
		mView.findViewById(R.id.btnUndo).setOnClickListener(this);

		mToolbars = mView.findViewById(R.id.drawingtools);
		mToolbars.setVisibility(GONE);
		
		View contentView = inflate(getContext(), R.layout.annocolorlayout, null);
		mColorTableView = new PopupWindow(contentView, LayoutParams.MATCH_PARENT, AppUtil.dip2px(context, 100));
		mColorTable = (ColorTable) contentView.findViewById(R.id.colorTable);
		txtLineWidth = (TextView)contentView.findViewById(R.id.txtLineWidth);
		// TODO: 4/25/2023  
		//mColorTableView.setBackgroundDrawable(getResources().getDrawable(R.drawable.zm_transparent));
		mColorTableView.setContentView(contentView);
		mColorTableView.setFocusable(true);
		mColorTableView.setOutsideTouchable(true);
		mColorTable.setOnColorChangedListener(this);

		mLineWidthSeekBar = (SeekBar) contentView.findViewById(R.id.seekbar);
		mLineWidthSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				mAnnotationController.setToolWidth(mLineWidth);
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {

			}

			@Override
			public void onProgressChanged(SeekBar arg0, int value, boolean arg2) {
				mLineWidth = value > 0 ? value : 1;
				updateLineWidthPromt();
			}
		});

		updateSelection(mPen);
	}

	private ZoomVideoSDKAnnotationHelper getAnnotationHelper() {
		if (mAnnotationController == null) {
			shareHelper = getShareHelper();
			mAnnotationController = shareHelper == null ? null : shareHelper.createAnnotationHelper(videoSDKVideoView);
		}
		return mAnnotationController;
	}

	private ZoomVideoSDKShareHelper getShareHelper() {
		if (shareHelper == null) {
			shareHelper = ZoomVideoSDK.getInstance().getShareHelper();
		}
		return shareHelper;
	}

	public void setRenderView(ZoomVideoSDKVideoView view) {
		videoSDKVideoView = view;
		if (getShareHelper().isSharingOut()) {
			videoSDKVideoView = null;
		}
		mAnnotationController = null;
	}

	public void updateAnnotationController(ZoomVideoSDKAnnotationHelper helper) {
		this.mAnnotationController = helper;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		ZoomVideoSDK.getInstance().removeListener(this);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	public boolean isAnnotationStarted()
	{
		return  getVisibility()==VISIBLE&& mToolbars.getVisibility()==VISIBLE;
	}


	private void startAnnotation()
	{

		if (getAnnotationHelper() == null) {
			return;
		}
		mAnnotationController.startAnnotation();

		mLineWidth = 2;

		setRandomColor();
		mAnnotationController.setToolType(ZoomVideoSDKAnnotationToolType.ZoomVideoSDKAnnotationToolType_Pen);
		updateSelection(mPen);
		shareHelper = getShareHelper();
		if (shareHelper == null) {
			return;
		}
		mArrow.setVisibility(shareHelper.isSharingOut() ? GONE : VISIBLE);
		mSpotlight.setVisibility(VISIBLE);
	}

	public void stopAnnotation() {

		if(getAnnotationHelper() == null) return;

		mAnnotationController.stopAnnotation();

		mAnnotationController.setToolType(ZoomVideoSDKAnnotationToolType.ZoomVideoSDKAnnotationToolType_Pen);
		updateSelection(mPen);
		if(null != mColorTableView && mColorTableView.isShowing())
			mColorTableView.dismiss();

		if(null != mSaveTableView && mSaveTableView.isShowing())
			mSaveTableView.dismiss();
	}
	
	private void updateSelection(View v) {
		if(null == v)
			return;

		mSpotlight.setSelected(false);
		mHighlight.setSelected(false);
		mPen.setSelected(false);
		mErase.setSelected(false);
		mArrow.setSelected(false);
		v.setSelected(true);
	}

	private int annoToolsIndex = 0;
	private final ZoomVideoSDKAnnotationToolType[] annoTools = ZoomVideoSDKAnnotationToolType.values();
	private int annoCleanIndex = 0;
	private final ZoomVideoSDKAnnotationClearType[] annoCleanTypes = ZoomVideoSDKAnnotationClearType.values();

	//set annoTool
	@Override  
	public void onClick(View v) 
	{
		if(getAnnotationHelper() == null) return;

		if (v == mSpotlight)
		{
			annoToolsIndex++;
			annoToolsIndex = annoToolsIndex % annoTools.length;
			int error = mAnnotationController.setToolType(annoTools[annoToolsIndex]);
			Log.e(TAG, "setToolType: " + annoTools[annoToolsIndex] + " error: " + error + ", getToolType: " + mAnnotationController.getToolType());
		} 
		else if (v == mPen) 
		{
			mAnnotationController.setToolType(ZoomVideoSDKAnnotationToolType.ZoomVideoSDKAnnotationToolType_Pen);
		} 
		else if (v == mHighlight)
		{
			mAnnotationController.setToolType(ZoomVideoSDKAnnotationToolType.ZoomVideoSDKAnnotationToolType_HighLighter);
		} 
		else if (v == mErase) 
		{
			mAnnotationController.setToolType(ZoomVideoSDKAnnotationToolType.ZoomVideoSDKAnnotationToolType_ERASER);
		} else if(v == mArrow)
		{
			mAnnotationController.setToolType(ZoomVideoSDKAnnotationToolType.ZoomVideoSDKAnnotationToolType_AutoDoubleArrow);
		}
		else if (v == mColorIndicator) 
		{
			if (mColorTableView.isShowing())
			{
				mColorTableView.dismiss();
			}
			else 
			{
				mColorTableView.showAsDropDown(mToolbars);
				updateLineWidthPromt();
			}
			return;
		}else if(v.getId()==R.id.btnUndo){
			mAnnotationController.undo();
		}else if(v.getId()==R.id.btnRedo){
			mAnnotationController.redo();
		}

		else if(v == mClear)
		{
			annoCleanIndex++;
			annoToolsIndex = annoCleanIndex % annoCleanTypes.length;
			int error = mAnnotationController.clear(annoCleanTypes[annoToolsIndex]);
			Log.e(TAG, "clean: " + annoCleanTypes[annoToolsIndex] + " error: " + error);
			return;
		}

		updateSelection(v);
	}
	
	private void updateLineWidthPromt()
	{
		if (mColorTableView.isShowing())
		{
			mLineWidthSeekBar.setProgress(mLineWidth);
			txtLineWidth.setText(String.valueOf(mLineWidth));
		}
	}

	@Override
	public void onColorChanged(View view, int newColor) {
	}

	@Override
	public void onColorPicked(View view, int newColor) {
		if (getAnnotationHelper() == null) {
			return;
		}
		mAnnotationController.setToolColor(newColor);
		mColorImage.setColor(newColor);
	}

	private void closeAnnoToolbar()
	{
		if(null != mToolbars) {
			mToolbars.setVisibility(GONE);
			mAnnotateBtn.setVisibility(VISIBLE);
			stopAnnotation();
		}
	}

	private void showAnnoToolbar()
	{
		if(null != mToolbars) {
			mToolbars.setVisibility(VISIBLE);
			mAnnotateBtn.setVisibility(GONE);
			startAnnotation();
		}
	}

    private void setRandomColor()
	{
		if (getAnnotationHelper() == null) {
			return;
		}
		Random rand = new Random();
		int index = rand.nextInt(9);
		int color = ColorTable.COLOR_RED;
		if(null != mColorTable){
			color = mColorTable.getColorByIndex(index);
		}
		mAnnotationController.setToolColor(color);
		mColorImage.setColor(color);
	}

	@Override
	protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
		if(visibility != VISIBLE) {
			closeAnnoToolbar();
		}
		super.onVisibilityChanged(changedView, visibility);
	}

	private class GuestureListener extends ToolbarDragView.ToolbarScrollListener {

		float mLastRawX = -1f;
		float mLastRawY = -1f;

		public GuestureListener(){
		}

		@Override
		public void onTouchEventUp() {
			//if a click event happen, the event is handle by child view, the method is not called,
			//so dragFinish should be also called in click listener
			mLastRawX = -1f;
			mLastRawY = -1f;
		}

		@SuppressWarnings("deprecation")
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
								float distanceX, float distanceY) {
			if(mView == null || mView.getParent() == null ){
				return true;
			}

			//hide color Table
			if(mColorTableView==null)
					return true;
			if(mColorTableView.isShowing())
			{
				mColorTableView.dismiss();
			}

			int dx , dy ;
			if((int)mLastRawX == -1 || (int) mLastRawY == -1 ){
				dx = (int) (e2.getRawX() - e1.getRawX());
				dy = (int) (e2.getRawY() - e1.getRawY());
			}else{
				dx = (int) (e2.getRawX() - mLastRawX);
				dy = (int) (e2.getRawY() - mLastRawY);
			}

			mLastRawX = e2.getRawX();
			mLastRawY = e2.getRawY();
			int width = AnnotateToolbar.this.getWidth();
			int height = AnnotateToolbar.this.getHeight();
			int top = AnnotateToolbar.this.getTop() + dy;
			int left = AnnotateToolbar.this.getLeft() + dx;
			//screen maybe rotate any time

			if(left < 0){
				left = 0;
			}

			if(left + width > AppUtil.getDisplayWidth(getContext())){
				left = AppUtil.getDisplayWidth(getContext()) - width;
			}

			if(top < 0){
				top = 0;
			}
			if(top + height > AppUtil.getDisplayHeight(getContext())){
				//out of top
				top = AppUtil.getDisplayHeight(getContext()) - height;
			}
			AnnotateToolbar.this.layout(left, top, left+width, top+height);
			return true;
		}
	}

	@Override
	public void onSessionJoin() {

	}

	@Override
	public void onSessionLeave() {

	}

	@Override
	public void onError(int errorCode) {

	}

	@Override
	public void onUserJoin(ZoomVideoSDKUserHelper userHelper, List<ZoomVideoSDKUser> userList) {

	}

	@Override
	public void onUserLeave(ZoomVideoSDKUserHelper userHelper, List<ZoomVideoSDKUser> userList) {

	}

	@Override
	public void onUserVideoStatusChanged(ZoomVideoSDKVideoHelper videoHelper, List<ZoomVideoSDKUser> userList) {

	}

	@Override
	public void onUserAudioStatusChanged(ZoomVideoSDKAudioHelper audioHelper, List<ZoomVideoSDKUser> userList) {

	}

	@Override
	public void onUserShareStatusChanged(ZoomVideoSDKShareHelper shareHelper, ZoomVideoSDKUser userInfo, ZoomVideoSDKShareStatus status) {

	}

	@Override
	public void onLiveStreamStatusChanged(ZoomVideoSDKLiveStreamHelper liveStreamHelper, ZoomVideoSDKLiveStreamStatus status) {

	}

	@Override
	public void onChatNewMessageNotify(ZoomVideoSDKChatHelper chatHelper, ZoomVideoSDKChatMessage messageItem) {

	}

	@Override
	public void onChatDeleteMessageNotify(ZoomVideoSDKChatHelper chatHelper, String msgID, ZoomVideoSDKChatMessageDeleteType deleteBy) {

	}

	@Override
	public void onChatPrivilegeChanged(ZoomVideoSDKChatHelper chatHelper, ZoomVideoSDKChatPrivilegeType currentPrivilege) {

	}

	@Override
	public void onUserHostChanged(ZoomVideoSDKUserHelper userHelper, ZoomVideoSDKUser userInfo) {

	}

	@Override
	public void onUserManagerChanged(ZoomVideoSDKUser user) {

	}

	@Override
	public void onUserNameChanged(ZoomVideoSDKUser user) {

	}

	@Override
	public void onUserActiveAudioChanged(ZoomVideoSDKAudioHelper audioHelper, List<ZoomVideoSDKUser> list) {

	}

	@Override
	public void onSessionNeedPassword(ZoomVideoSDKPasswordHandler handler) {

	}

	@Override
	public void onSessionPasswordWrong(ZoomVideoSDKPasswordHandler handler) {

	}

	@Override
	public void onMixedAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData) {

	}

	@Override
	public void onOneWayAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData, ZoomVideoSDKUser user) {

	}

	@Override
	public void onShareAudioRawDataReceived(ZoomVideoSDKAudioRawData rawData) {

	}

	@Override
	public void onCommandReceived(ZoomVideoSDKUser sender, String strCmd) {

	}

	@Override
	public void onCommandChannelConnectResult(boolean isSuccess) {

	}

	@Override
	public void onCloudRecordingStatus(ZoomVideoSDKRecordingStatus status, ZoomVideoSDKRecordingConsentHandler handler) {

	}

	@Override
	public void onHostAskUnmute() {

	}

	@Override
	public void onInviteByPhoneStatus(ZoomVideoSDKPhoneStatus status, ZoomVideoSDKPhoneFailedReason reason) {

	}

	@Override
	public void onMultiCameraStreamStatusChanged(ZoomVideoSDKMultiCameraStreamStatus status, ZoomVideoSDKUser user, ZoomVideoSDKRawDataPipe videoPipe) {

	}

	@Override
	public void onMultiCameraStreamStatusChanged(ZoomVideoSDKMultiCameraStreamStatus status, ZoomVideoSDKUser user, ZoomVideoSDKVideoCanvas canvas) {

	}

	@Override
	public void onLiveTranscriptionStatus(ZoomVideoSDKLiveTranscriptionHelper.ZoomVideoSDKLiveTranscriptionStatus status) {

	}

	@Override
	public void onLiveTranscriptionMsgReceived(String ltMsg, ZoomVideoSDKUser pUser, ZoomVideoSDKLiveTranscriptionHelper.ZoomVideoSDKLiveTranscriptionOperationType type) {

	}

	@Override
	public void onOriginalLanguageMsgReceived(ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionMessageInfo messageInfo) {

	}

	@Override
	public void onLiveTranscriptionMsgInfoReceived(ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionMessageInfo messageInfo) {

	}

	@Override
	public void onLiveTranscriptionMsgError(ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage spokenLanguage, ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage transcriptLanguage) {

	}

	@Override
	public void onProxySettingNotification(ZoomVideoSDKProxySettingHandler handler) {

	}

	@Override
	public void onSSLCertVerifiedFailNotification(ZoomVideoSDKSSLCertificateInfo info) {

	}

	@Override
	public void onCameraControlRequestResult(ZoomVideoSDKUser user, boolean isApproved) {

	}

	@Override
	public void onUserVideoNetworkStatusChanged(ZoomVideoSDKNetworkStatus status, ZoomVideoSDKUser user) {

	}

	@Override
	public void onUserRecordingConsent(ZoomVideoSDKUser user) {

	}

	@Override
	public void onCallCRCDeviceStatusChanged(ZoomVideoSDKCRCCallStatus status) {

	}

	@Override
	public void onVideoCanvasSubscribeFail(ZoomVideoSDKVideoSubscribeFailReason fail_reason, ZoomVideoSDKUser pUser, ZoomVideoSDKVideoView view) {

	}

	@Override
	public void onShareCanvasSubscribeFail(ZoomVideoSDKVideoSubscribeFailReason fail_reason, ZoomVideoSDKUser pUser, ZoomVideoSDKVideoView view) {

	}

	@Override
	public void onAnnotationHelperCleanUp(ZoomVideoSDKAnnotationHelper helper) {
		if (helper == mAnnotationController) {
			mAnnotationController = null;
		}
	}

	@Override
	public void onAnnotationPrivilegeChange(boolean enable, ZoomVideoSDKUser user) {
		// TODO: 5/11/2023
	}
}
