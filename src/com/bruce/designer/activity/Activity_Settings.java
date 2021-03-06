package com.bruce.designer.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.bruce.designer.AppApplication;
import com.bruce.designer.AppManager;
import com.bruce.designer.R;
import com.bruce.designer.api.ApiManager;
import com.bruce.designer.api.user.BindPushTokenApi;
import com.bruce.designer.constants.Config;
import com.bruce.designer.constants.ConstantsStatEvent;
import com.bruce.designer.db.album.AlbumDB;
import com.bruce.designer.listener.OnSingleClickListener;
import com.bruce.designer.model.User;
import com.bruce.designer.model.result.ApiResult;
import com.bruce.designer.model.share.GenericSharedInfo;
import com.bruce.designer.util.LogUtil;
import com.bruce.designer.util.SharedPreferenceUtil;
import com.bruce.designer.util.StringUtils;
import com.bruce.designer.util.UiUtil;
import com.bruce.designer.util.UrlUtil;
import com.bruce.designer.view.SharePanelView;

public class Activity_Settings extends BaseActivity {
	
	private View titlebarView;
	private TextView titleView;
	
	/*账户绑定的container*/
	private View accountBindContainer;
	
	private View pushSettingsView, clearCacheView, wxmpView, websiteView, inviteFriendsView;
	private TextView pushStatusView;

	private Button btnLogout, btnLogin;
	private ImageView iconWeibo, iconWeixin;
	
	public static void show(Context context) {
		Intent intent = new Intent(context, Activity_Settings.class);
		context.startActivity(intent);
	}
	

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
		
		accountBindContainer = (View)findViewById(R.id.account_bind_container);
		
		pushStatusView = (TextView)findViewById(R.id.pushStatus);
		pushSettingsView = findViewById(R.id.pushSetting);
		pushSettingsView.setOnClickListener(listener);
		
		inviteFriendsView = findViewById(R.id.inviteFriends);
		inviteFriendsView.setOnClickListener(listener);

		clearCacheView = findViewById(R.id.clearCache);
		clearCacheView.setOnClickListener(listener);
		
		wxmpView = findViewById(R.id.wxmpView);
		wxmpView.setOnClickListener(listener);
		
		websiteView = findViewById(R.id.websiteView);
		websiteView.setOnClickListener(listener);
		
		btnLogout = (Button) findViewById(R.id.logout);
		btnLogout.setOnClickListener(listener);
		btnLogin = (Button) findViewById(R.id.login);
		btnLogin.setOnClickListener(listener);
		
		iconWeibo = (ImageView) findViewById(R.id.weiboIcon);
		iconWeixin = (ImageView) findViewById(R.id.weixinIcon);
		
		if(AppApplication.isGuest()){//游客需要展示登录按钮
			accountBindContainer.setVisibility(View.GONE);
			btnLogin.setVisibility(View.VISIBLE);
		}else{//登录用户展示注销按钮
			btnLogout.setVisibility(View.VISIBLE);
			accountBindContainer.setVisibility(View.VISIBLE);
			//展示绑定账户
			User hostUser = AppApplication.getHostUser();
			if(hostUser!=null&&hostUser.getAccessTokenMap()!=null&&hostUser.getAccessTokenMap().size()>0){
				boolean bindWeibo = hostUser.getAccessTokenMap().get((short)1)!=null;
				boolean bindWeixin = hostUser.getAccessTokenMap().get((short)3)!=null;
				if(bindWeibo){//已绑定微博
//					UiUtil.showShortToast(context, "已绑定Sina微博");
					iconWeibo.setImageDrawable(getResources().getDrawable(R.drawable.icon_weibo_bund));
				}else{
//					UiUtil.showShortToast(context, "未绑定Sina微博");
					iconWeibo.setImageDrawable(getResources().getDrawable(R.drawable.icon_weibo_unbind));
				}
				if(bindWeixin){//已绑定微信
//					UiUtil.showShortToast(context, "已绑定微信");
					iconWeixin.setImageDrawable(getResources().getDrawable(R.drawable.icon_weixin_bund));
				}else{
//					UiUtil.showShortToast(context, "未绑定微信");
					iconWeixin.setImageDrawable(getResources().getDrawable(R.drawable.icon_weixin_unbind));
				}
			}
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
				StatService.onEvent(context, ConstantsStatEvent.EVENT_SETTINGS_OPTION, "进入Push设置");
				if(AppApplication.isGuest()){
					UiUtil.showShortToast(context, Config.GUEST_ACCESS_DENIED_TEXT);
				}else{
					Activity_Settings_Push.show(context);
				}
				break;
			case R.id.inviteFriends:
				StatService.onEvent(context, ConstantsStatEvent.EVENT_SETTINGS_OPTION, "邀请好友");
				
				GenericSharedInfo generalSharedInfo = AppApplication.getAppSharedInfo();
//				UiUtil.showLongToast(context, JsonUtil.gson.toJson(generalSharedInfo)); 
				SharePanelView sharePanel = new SharePanelView(context, generalSharedInfo);
				sharePanel.show(findViewById(R.id.settingsScrollView));
				break;
			case R.id.wxmpView:
				StatService.onEvent(context, ConstantsStatEvent.EVENT_SETTINGS_OPTION, "微信公众帐号");
				Activity_AboutUs.show(context);
				break;
			case R.id.clearCache:
				StatService.onEvent(context, ConstantsStatEvent.EVENT_SETTINGS_OPTION, "清除缓存");
				// 弹起popWindow
				// 新建一个poppopWindow，并显示里面的内容
//				PopupWindow popupWindow = makePopupWindow(context);
//				//指定显示位置
//				popupWindow.showAsDropDown(clearCacheView);
				
				AlertDialog clearDialog = UiUtil.showAlertDialog(context, "清除缓存", "根据缓存文件大小，清理时间从几秒到几分钟不等，请耐心等待", "清理", null, "取消", null);
				clearDialog.show();
				break;
			case R.id.websiteView:
				StatService.onEvent(context, ConstantsStatEvent.EVENT_SETTINGS_OPTION, "打开网站");
				
				//启动web浏览器访问主页
				Intent intent = new Intent();        
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = Uri.parse(UrlUtil.addParameter(Config.JINWAN_WEB_DOMAIN, Config.CHANNEL_FLAG, AppApplication.getChannel()));
				intent.setData(content_url);  
				startActivity(intent);
				break;
			case R.id.login:
				StatService.onEvent(context, ConstantsStatEvent.EVENT_SETTINGS_OPTION, "立即登录");
				
				//清除本机的登录信息
				AppApplication.clearAccount();
				//TODO 清除DB缓存的数据
				
				AppManager.getInstance().finishAllActivity();
				Activity_Login.show(context);
				break;
			case R.id.logout:
				StatService.onEvent(context, ConstantsStatEvent.EVENT_SETTINGS_OPTION, "用户注销");
				if(!AppApplication.isGuest()){//登录用户需要解绑pushToken
					//获取push信息
					String pushUserId = SharedPreferenceUtil.getSharePreStr(context, Config.SP_KEY_BAIDU_PUSH_USER_ID, "");
					String pushChannelId = SharedPreferenceUtil.getSharePreStr(context, Config.SP_KEY_BAIDU_PUSH_CHANNEL_ID, "");
					//发起线程解绑用户的pushToken
					unbindPushToken(pushChannelId, pushUserId);
				}
//				PushManager.stopWork(context);//只退出，不结束push
				//清除本机的登录信息
				AppApplication.clearAccount();
				//清除所有DB数据
				AlbumDB.clearAll(context);
				
				AppManager.getInstance().finishAllActivity();
				UiUtil.showShortToast(context, "注销成功");
				Activity_Login.show(context);
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
				if(!StringUtils.isBlank(channelId)&&!StringUtils.isBlank(userId)){
					BindPushTokenApi api = new BindPushTokenApi(channelId, userId, 0);
					ApiResult apiResult = ApiManager.invoke(context, api);
					if (apiResult != null && apiResult.getResult() == 1) {
						LogUtil.d("解绑push的结果： " + apiResult.getResult());
	//					Message message = handler.obtainMessage(HANDLER_FLAG_PUSH_UNBIND);
	//					message.sendToTarget();
				}
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

