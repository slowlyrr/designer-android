package com.bruce.designer.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.android.pushservice.PushManager;
import com.bruce.designer.AppApplication;
import com.bruce.designer.AppManager;
import com.bruce.designer.R;
import com.bruce.designer.api.ApiManager;
import com.bruce.designer.api.user.BindPushTokenApi;
import com.bruce.designer.constants.Config;
import com.bruce.designer.listener.OnSingleClickListener;
import com.bruce.designer.model.result.ApiResult;
import com.bruce.designer.util.LogUtil;
import com.bruce.designer.util.SharedPreferenceUtil;
import com.bruce.designer.util.UiUtil;

public class Activity_Settings extends BaseActivity {
	
	protected static final int HANDLER_FLAG_PUSH_UNBIND = 10;
	
	
	private View titlebarView;
	private TextView titleView;
	private View pushSettingsView, aboutUsView, clearCacheView, websiteView;
	private TextView pushStatusView;
//	private SwitcherView pushSwitcher;

	private Button btnLogout;
	
	public static void show(Context context) {
		Intent intent = new Intent(context, Activity_Settings.class);
		context.startActivity(intent);
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_FLAG_PUSH_UNBIND:
				break;
			default:
				break;
			}
		}
	};
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		initView();
	}

	private void initView() {
		// init view
		titlebarView = findViewById(R.id.titlebar_return);
		titlebarView.setOnClickListener(listener);
		titleView = (TextView) findViewById(R.id.titlebar_title);
		titleView.setText("设置");

		pushStatusView = (TextView)findViewById(R.id.pushStatus);
		pushSettingsView = findViewById(R.id.pushSetting);
		pushSettingsView.setOnClickListener(listener);
		
		aboutUsView = findViewById(R.id.aboutUs);
		aboutUsView.setOnClickListener(listener);

		clearCacheView = findViewById(R.id.clearCache);
		clearCacheView.setOnClickListener(listener);
		
		websiteView = findViewById(R.id.websiteView);
		websiteView.setOnClickListener(listener);
		
		btnLogout = (Button) findViewById(R.id.logout);
		if(AppApplication.isGuest()){
			btnLogout.setVisibility(View.GONE);
		}else{
			btnLogout.setVisibility(View.VISIBLE);
			btnLogout.setOnClickListener(listener);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//读取push设置
		long cachedPushMask = SharedPreferenceUtil.getSharePreLong(context, Config.SP_KEY_BAIDU_PUSH_MASK , Long.MAX_VALUE);
		if(cachedPushMask>0){
			pushStatusView.setText("已开启");
		}else{
			pushStatusView.setText("已关闭");
		}
	}
	
	
	private OnClickListener listener = new OnSingleClickListener() {
		@Override
		public void onSingleClick(View view) {

			switch (view.getId()) {
			case R.id.titlebar_return:
				finish();
				break;
			case R.id.btnSettings:
				Activity_Settings.show(context);
				break;
			case R.id.pushSetting:
				if(AppApplication.isGuest()){
					UiUtil.showShortToast(context, "游客无法进行推送设置");
				}else{
					Activity_Settings_Push.show(context);
				}
				break;
			case R.id.aboutUs:
				Activity_AboutUs.show(context);
				break;
			case R.id.clearCache:
				// 弹起popWindow
				// 新建一个poppopWindow，并显示里面的内容
//				PopupWindow popupWindow = makePopupWindow(context);
//				//指定显示位置
//				popupWindow.showAsDropDown(clearCacheView);
				
				AlertDialog clearDialog = UiUtil.showAlertDialog(context, "清除缓存", "根据缓存文件大小，清理时间从几秒到几分钟不等，请耐心等待", "清理", null, "取消", null);
				clearDialog.show();
				break;
			case R.id.websiteView:
				//启动web浏览器访问主页
				Intent intent = new Intent();        
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = Uri.parse(Config.JINWAN_WEB_DOMAIN);
				intent.setData(content_url);  
				startActivity(intent);
				break;
			case R.id.logout:
				//获取push信息
				String pushUserId = SharedPreferenceUtil.getSharePreStr(context, Config.SP_KEY_BAIDU_PUSH_USER_ID, "");
				String pushChannelId = SharedPreferenceUtil.getSharePreStr(context, Config.SP_KEY_BAIDU_PUSH_CHANNEL_ID, "");
				//发起线程解绑用户的pushToken
				unbindPushToken(pushChannelId, pushUserId);
				
				PushManager.stopWork(context);
				
				//清除本机的登录信息
				AppApplication.clearAccount();
				
				AppManager.getInstance().finishAllActivity();
				Activity_Login.show(context);
				UiUtil.showShortToast(context, "注销登录成功");
				break;
			default:
				break;
			}
		}
	};
	
	/**
	 * 解绑push
	 * @param channelId
	 * @param userId
	 */
	public void unbindPushToken(final String channelId, final String userId){
		//发起线程，请求用户绑定pushToken
		new Thread(new Runnable() {
			@Override
			public void run() {
				BindPushTokenApi api = new BindPushTokenApi(channelId, userId, 0);
				ApiResult apiResult = ApiManager.invoke(context, api);
				if (apiResult != null && apiResult.getResult() == 1) {
					LogUtil.d("解绑push的结果： " + apiResult.getResult());
//					Message message = handler.obtainMessage(HANDLER_FLAG_PUSH_UNBIND);
//					message.sendToTarget();
				}
			}
		}).start();
	}
	
	
//	/**
//	 * 创建一个包含自定义view的PopupWindow
//	 * 
//	 * @param context
//	 * @return
//	 */
//	private PopupWindow makePopupWindow(Context context) {
//		PopupWindow popWindow = new PopupWindow(context);
//		View contentView = LayoutInflater.from(this).inflate(R.layout.popup_window, null);
//		popWindow = new PopupWindow(contentView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//		//以下两行实现点击back按钮消失
//		ColorDrawable dw = new ColorDrawable(-00000);
//		popWindow.setBackgroundDrawable(dw);
//		
//		// 设置PopupWindow外部区域是否可触摸
//		popWindow.setFocusable(true); // 设置PopupWindow可获得焦点
//		popWindow.setTouchable(true); // 设置PopupWindow可触摸
//		popWindow.setOutsideTouchable(true);// 设置非PopupWindow区域可触摸
//		return popWindow;
//	}
}

