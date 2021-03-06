package com.bruce.designer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;

import com.baidu.frontia.FrontiaApplication;
import com.bruce.designer.constants.Config;
import com.bruce.designer.model.User;
import com.bruce.designer.model.UserPassport;
import com.bruce.designer.model.share.GenericSharedInfo;
import com.bruce.designer.model.share.GenericSharedInfo.WxSharedInfo;
import com.bruce.designer.util.ApplicationUtil;
import com.bruce.designer.util.SharedPreferenceUtil;
import com.bruce.designer.util.UniversalImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class AppApplication extends FrontiaApplication {

	private static int versionCode;
	private static String versionName;
	private static String channel;
	
	private static UserPassport userPassport;
	private static User hostUser;
	
	private static float screendensity;
	private static int screenHeight;
	private static int screenWidth;
	/*应用分享时的对象*/
	private static GenericSharedInfo appSharedInfo;
	/*是否显示售价（主要为了应对应用宝审核时关闭）*/
	private static boolean showPrice=false;

	/**
	 * 其他全局量
	 */
	private static AppApplication application;
	private static Handler uiHandler = new Handler();
	private static IWXAPI wxApi; 

	@Override
	public void onCreate() {
		super.onCreate();
		application = this;
		//代码方式init百度application
		FrontiaApplication.initFrontiaApplication(application);
		
		init();
		
		//init weixin share
		wxApi = initWxApi(application);
		
		//Universal ImageLoader init
		ImageLoader.getInstance().init(UniversalImageUtil.buildUniversalImageConfig(application));//全局初始化配置  
	}

	public static void init() {
		//获取packageVersion
		String packageName = application.getPackageName();
		PackageManager pm = application.getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(packageName, 0);
			versionName = info.versionName;
			versionCode = info.versionCode;
		} catch (NameNotFoundException ignored) {
			versionName = "";
			versionCode = 0;
		}
		channel = ApplicationUtil.getMetaValue(application, "BaiduMobAd_CHANNEL");
	}

	public static String getVersionName() {
		return versionName;
	}
	
	public static int getVersionCode() {
		return versionCode;
	}
	
	public static String getChannel() {
		return channel;
	}

	public static AppApplication getApplication() {
		return application;
	}

	public static Handler getUiHandler() {
		return uiHandler;
	}

	public static UserPassport getUserPassport() {
		if(userPassport==null){
			//从sp中读取
			userPassport = SharedPreferenceUtil.readObjectFromSp(UserPassport.class, Config.SP_CONFIG_ACCOUNT, Config.SP_KEY_USERPASSPORT);
		}
		return userPassport;
	}

	public synchronized static void setUserPassport(UserPassport userPassport) {
		if(userPassport!=null){
			//写入sp
			SharedPreferenceUtil.writeObjectToSp(userPassport, Config.SP_CONFIG_ACCOUNT,  Config.SP_KEY_USERPASSPORT);
			AppApplication.userPassport = userPassport;
		}
	}
	
	public synchronized static User getHostUser() {
		if(hostUser==null){
			//从sp中读取
			hostUser = SharedPreferenceUtil.readObjectFromSp(User.class, Config.SP_CONFIG_ACCOUNT, Config.SP_KEY_USERINFO);
		}
		return hostUser;
	}

	public synchronized static void setHostUser(User responseHostUser) {
		if(responseHostUser!=null){
			//写入sp
			SharedPreferenceUtil.writeObjectToSp(responseHostUser, Config.SP_CONFIG_ACCOUNT,  Config.SP_KEY_USERINFO);
			hostUser = responseHostUser;
		}
	}
	
	public synchronized static GenericSharedInfo getAppSharedInfo() {
		if(appSharedInfo==null){
			appSharedInfo = new GenericSharedInfo();
			appSharedInfo.setWxSharedInfo(new WxSharedInfo(Config.SHARED_APP_DEFAULT_TITLE, Config.SHARED_APP_DEFAULT_CONTENT, Config.SHARED_APP_DEFAULT_ICON_URL, Config.SHARED_APP_DEFAULT_LINK));
		}
		return appSharedInfo;
	}

	public synchronized static void setAppSharedInfo(GenericSharedInfo appSharedInfo) {
		AppApplication.appSharedInfo = appSharedInfo;
	}
	
	public static boolean isShowPrice() {
		return showPrice;
	}

	public static void setShowPrice(boolean showPrice) {
		AppApplication.showPrice = showPrice;
	}

	/**
	 * 判断当前用户是否是游客
	 * @return
	 */
	public static boolean isGuest(){
		UserPassport userPassport= getUserPassport();
		if(userPassport!=null&&userPassport.getUserId()>0){
			return isGuest(userPassport.getUserId());
		}
		return true;
	}
	
	/**
	 * 判断指定的用户是否是游客
	 * @param userId
	 * @return
	 */
	public static boolean isGuest(int userId){
		return userId <= Config.GUEST_USER_ID;
	}
	
	/**
	 * 注销时清除sp的账户缓存
	 */
	public static void clearAccount(){
		//清除sp中的缓存
		hostUser = null;
		userPassport = null;
		SharedPreferenceUtil.removeSharePre(application, Config.SP_CONFIG_ACCOUNT,  Config.SP_KEY_USERINFO);
		SharedPreferenceUtil.removeSharePre(application, Config.SP_CONFIG_ACCOUNT,  Config.SP_KEY_USERPASSPORT);
	}
	

	public static float getScreendensity() {
		return screendensity;
	}

	public static int getScreenHeight() {
		return screenHeight;
	}

	public static int getScreenWidth() {
		return screenWidth;
	}
	
	public static IWXAPI getWxApi() {
		return wxApi;
	}

	/**
	 * 判断是否是当前登录用户
	 * @param userId
	 * @return
	 */
	public static boolean isHost(int userId){
		UserPassport userPassport = getUserPassport();
		return (userPassport!=null&&Integer.valueOf(userId).equals(userPassport.getUserId()));
	}

	
	public static IWXAPI initWxApi(Context context){
		String wxAppId = ApplicationUtil.getMetaValue(context, "WeixinOpen_APP_ID");
		IWXAPI api = WXAPIFactory.createWXAPI(context, wxAppId, true);
		api.registerApp(wxAppId);
		return api;
	}

	
	
	
//	public static String getMetaData(Context context, String metaKey){
//		return ApplicationUtil.getMetaValue(context, metaKey);
//	}
	
	

}
