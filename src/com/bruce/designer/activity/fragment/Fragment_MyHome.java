package com.bruce.designer.activity.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.bruce.designer.AppApplication;
import com.bruce.designer.AppManager;
import com.bruce.designer.R;
import com.bruce.designer.activity.Activity_Login;
import com.bruce.designer.activity.Activity_MyFavorite;
import com.bruce.designer.activity.Activity_Settings;
import com.bruce.designer.activity.Activity_UserFans;
import com.bruce.designer.activity.Activity_UserFollows;
import com.bruce.designer.activity.Activity_UserInfo;
import com.bruce.designer.adapter.DesignerAlbumsAdapter;
import com.bruce.designer.api.ApiManager;
import com.bruce.designer.api.album.AlbumListApi;
import com.bruce.designer.api.user.MyInfoApi;
import com.bruce.designer.broadcast.NotificationBuilder;
import com.bruce.designer.constants.ConstantsKey;
import com.bruce.designer.constants.ConstantsStatEvent;
import com.bruce.designer.db.album.AlbumDB;
import com.bruce.designer.handler.DesignerHandler;
import com.bruce.designer.listener.IOnAlbumListener;
import com.bruce.designer.listener.OnAlbumListener;
import com.bruce.designer.listener.OnSingleClickListener;
import com.bruce.designer.model.Album;
import com.bruce.designer.model.User;
import com.bruce.designer.model.result.ApiResult;
import com.bruce.designer.util.DesignerUtil;
import com.bruce.designer.util.ImageUtil;
import com.bruce.designer.util.SharedPreferenceUtil;
import com.bruce.designer.util.TimeUtil;
import com.bruce.designer.util.UiUtil;
import com.bruce.designer.util.UniversalImageUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 我的个人资料的Fragment
 * @author liqian
 *
 */
public class Fragment_MyHome extends BaseFragment implements OnRefreshListener2<ListView> {
	
	private static final int HANDLER_FLAG_USERINFO_RESULT = 1;
	private static final int HANDLER_FLAG_SLIDE_RESULT = 2;
	
	public static final int REQUEST_CODE_USERINFO = 100;
	public static final int RESULT_CODE_AVATAR_CHANGED = 200;
	
	private int HOST_ID = AppApplication.getUserPassport().getUserId();
	
	private Activity activity; 
	private LayoutInflater inflater;
	
	private TextView titleView;
	/*用户昵称*/
	private TextView nicknameView;
	/*设计师头像*/
	private ImageView avatarView;
	
	private View albumsView, followsView, fansView;
	
	private TextView albumsNumView, followsNumView, fansNumView;
	
	private Button btnLogin, btnApplyDesigner, btnNewAlbum, btnMyFavorite, btnSendMsg, btnUserInfo;
//	private Button btnPubAlbum;//发布新作品
	
	private ImageButton btnSettings;
	private PullToRefreshListView pullRefreshView;
	private DesignerAlbumsAdapter albumListAdapter; 
	
	private int albumTailId = 0;
	
	private Handler handler;
	private OnClickListener onClickListener;
	
	private Handler initHandler(){ 
		Handler handler = new DesignerHandler(activity){
		@SuppressWarnings("unchecked")
		public void processHandlerMessage(Message msg) {
			ApiResult apiResult = (ApiResult) msg.obj;
			boolean successResult = (apiResult!=null&&apiResult.getResult()==1);
			
			switch(msg.what){
				case HANDLER_FLAG_USERINFO_RESULT:
					if(successResult){
						SharedPreferenceUtil.putSharePre(activity, getRefreshKey(), System.currentTimeMillis());
						Map<String, Object> userinfoDataMap = (Map<String, Object>) apiResult.getData();
						if(userinfoDataMap!=null){
							User userinfo = (User) userinfoDataMap.get("userinfo");
							int fansCount = (Integer) userinfoDataMap.get("fansCount");
							int followsCount = (Integer) userinfoDataMap.get("followsCount");
							int albumsCount = (Integer) userinfoDataMap.get("albumsCount");
							
							if(userinfo!=null&&userinfo.getId()>0){
								if(AppApplication.isHost(userinfo.getId())){
									//缓存到sp
									AppApplication.setHostUser(userinfo);
								}
								//显示头像
								ImageLoader.getInstance().displayImage(userinfo.getHeadImg(), avatarView, UniversalImageUtil.DEFAULT_AVATAR_DISPLAY_OPTION);
								
								titleView.setText(userinfo.getNickname());
								nicknameView.setText(userinfo.getNickname());
								
								albumsNumView.setText(String.valueOf(albumsCount));
								fansNumView.setText(String.valueOf(fansCount));
								followsNumView.setText(String.valueOf(followsCount));
							}
						}
					}else{
						UiUtil.showShortToast(activity, "获取个人资料失败，请重试");
					}
					break;
				case HANDLER_FLAG_SLIDE_RESULT:
					pullRefreshView.onRefreshComplete();
					if(apiResult!=null&&apiResult.getResult()==1){
						Map<String, Object> albumsDataMap = (Map<String, Object>) apiResult.getData();
						if(albumsDataMap!=null){
							List<Album> albumList = (List<Album>) albumsDataMap.get("albumList");
							Integer fromTailId = (Integer) albumsDataMap.get("fromTailId");
							Integer newTailId = (Integer) albumsDataMap.get("newTailId");
							if(albumList!=null&&albumList.size()>0){
								
								if(newTailId!=null&&newTailId>0){//还有可加载的数据
									albumTailId = newTailId;
									pullRefreshView.setMode(Mode.BOTH);
								}else{
									albumTailId = 0;
									pullRefreshView.setMode(Mode.PULL_FROM_START);//禁用上拉刷新
								}
								List<Album> oldAlbumList = albumListAdapter.getAlbumList();
								if(oldAlbumList==null){
									oldAlbumList = new ArrayList<Album>();
								}
								//判断加载位置，以确定是list增量还是覆盖
								boolean fallloadAppend = fromTailId!=null&&fromTailId>0;
								if(fallloadAppend){//上拉加载更多，需添加至list的结尾
									oldAlbumList.addAll(albumList);
								}else{//下拉加载，需覆盖原数据
									oldAlbumList = null;
									oldAlbumList = albumList;
								}
								albumListAdapter.setAlbumList(oldAlbumList);
								albumListAdapter.notifyDataSetChanged();
							}
						}
					}else{
						UiUtil.showShortToast(activity, "获取专辑数据失败，请重试");
					}
					break;
				case IOnAlbumListener.HANDLER_FLAG_LIKE_POST_RESULT: //赞成功
					if(successResult){
						int likedAlbumId = (Integer) apiResult.getData();
						AlbumDB.updateLikeStatus(activity, likedAlbumId, 1, 1);//更新db状态
						broadcastAlbumOperated(likedAlbumId);//更新db后发送广播
						//更新ui展示
						List<Album> albumList4Like = albumListAdapter.getAlbumList();
						if(albumList4Like!=null&&albumList4Like.size()>0){
							for(Album album: albumList4Like){
								if(album.getId()!=null&&album.getId()==likedAlbumId){
									album.setLike(true);
									long likeCount = album.getLikeCount();
									album.setLikeCount(likeCount+1);
									break;
								}
							}
							albumListAdapter.notifyDataSetChanged();
						}
					}
					//发送广播
					NotificationBuilder.createNotification(activity, successResult?"赞操作成功...":"赞操作失败...");
					break;
				case IOnAlbumListener.HANDLER_FLAG_FAVORITE_POST_RESULT: //收藏成功
					if(successResult){
						int favoritedAlbumId = (Integer) apiResult.getData();
						AlbumDB.updateFavoriteStatus(activity, favoritedAlbumId, 1, 1);//更新db状态
						broadcastAlbumOperated(favoritedAlbumId);//更新db后发送广播
						//更新ui展示
						List<Album> albumList4Favorite = albumListAdapter.getAlbumList();
						if(albumList4Favorite!=null&&albumList4Favorite.size()>0){
							for(Album album: albumList4Favorite){
								if(album.getId()!=null&&album.getId()==favoritedAlbumId){
									long favoriteCount = album.getFavoriteCount();
									album.setFavoriteCount(favoriteCount+1);
									album.setFavorite(true);
									break;
								}
							}
							albumListAdapter.notifyDataSetChanged();
						}
					}
					//发送广播
					NotificationBuilder.createNotification(activity, successResult?"收藏成功...":"收藏失败...");
					break;
				case IOnAlbumListener.HANDLER_FLAG_UNFAVORITE_POST_RESULT: //取消收藏成功
					if(successResult){
						int unfavoritedAlbumId = (Integer) apiResult.getData(); 
						AlbumDB.updateFavoriteStatus(activity, unfavoritedAlbumId, 0, -1);//更新db状态
						broadcastAlbumOperated(unfavoritedAlbumId);//更新db后发送广播
						//更新ui展示
						List<Album> albumList = albumListAdapter.getAlbumList();
						if(albumList!=null&&albumList.size()>0){
							for(Album album: albumList){
								if(album.getId()!=null&&album.getId()==unfavoritedAlbumId){
									long favoriteCount = album.getFavoriteCount();
									album.setFavoriteCount(favoriteCount-1);
									album.setFavorite(false);
									break;
								}
							}
							albumListAdapter.notifyDataSetChanged();
						}
					}
					//发送广播
					NotificationBuilder.createNotification(activity, successResult?"取消收藏成功...":"取消收藏失败...");
					break;
				default:
					break;
			}
		}
	};
	return handler;
}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity = getActivity();
		this.inflater = inflater;
		
		handler = initHandler();
		onClickListener = initListener();
		
		View mainView = inflater.inflate(R.layout.activity_user_home, null);
		initView(mainView);
		
		return mainView;
	}

	private void initView(View mainView) {

		View titlebarIcon = (View) mainView.findViewById(R.id.titlebar_icon);
		titlebarIcon.setVisibility(View.GONE);
		
		titleView = (TextView) mainView.findViewById(R.id.titlebar_title);
		titleView.setText("我");
		
		//setting按钮及点击事件
		btnSettings = (ImageButton) mainView.findViewById(R.id.btnSettings);
		btnSettings.setOnClickListener(onClickListener);
		btnSettings.setVisibility(View.VISIBLE);
		
		pullRefreshView = (PullToRefreshListView) mainView.findViewById(R.id.pull_refresh_list);
		if(AppApplication.isGuest()){
			pullRefreshView.setMode(Mode.DISABLED);//游客无操作
		}else{
			pullRefreshView.setMode(Mode.PULL_FROM_START);
		}
		pullRefreshView.setOnRefreshListener(this);
		ListView albumListView = pullRefreshView.getRefreshableView();
		albumListAdapter = new DesignerAlbumsAdapter(activity, null, new OnAlbumListener(activity, handler, mainView));
		albumListView.setAdapter(albumListAdapter);
		
		//把个人资料的layout作为listview的header
		View headerView = inflater.inflate(R.layout.user_home_head, null);
		albumListView.addHeaderView(headerView);
		
		avatarView = (ImageView) headerView.findViewById(R.id.avatar);
		//显示昵称
		nicknameView = (TextView) headerView.findViewById(R.id.txtNickname);
		if(AppApplication.isGuest()){
			nicknameView.setText("游客");
		}else{
			User user = AppApplication.getHostUser();
			nicknameView.setText(user.getNickname());
		}
		
		albumsView = (View) headerView.findViewById(R.id.albumsContainer);
		albumsNumView = (TextView) headerView.findViewById(R.id.txtAlbumsNum);
		albumsView.setOnClickListener(onClickListener);
		
		fansView = (View) headerView.findViewById(R.id.fansContainer);
		fansNumView = (TextView) headerView.findViewById(R.id.txtFansNum);
		fansView.setOnClickListener(onClickListener);//默认情况下不能查看粉丝列表（只有设计师才能查看）
		
		followsView = (View) headerView.findViewById(R.id.followsContainer);
		followsNumView = (TextView) headerView.findViewById(R.id.txtFollowsNum);
		followsView.setOnClickListener(onClickListener);
		
		btnSendMsg = (Button)headerView.findViewById(R.id.btnSendMsg);
		btnSendMsg.setVisibility(View.GONE);
		
		btnMyFavorite = (Button)headerView.findViewById(R.id.btnMyFavorite);
		btnMyFavorite.setOnClickListener(onClickListener);
		btnMyFavorite.setVisibility(View.VISIBLE);
		
		btnLogin = (Button)headerView.findViewById(R.id.btnLogin);
		btnApplyDesigner = (Button)headerView.findViewById(R.id.btnApplyDesigner);
		btnNewAlbum = (Button)headerView.findViewById(R.id.btnCreateAlbum);
		
		if(AppApplication.isGuest()){
			btnLogin.setOnClickListener(onClickListener);
			btnLogin.setVisibility(View.VISIBLE);
		}else{
			User hostUser = AppApplication.getHostUser();
			if(hostUser!=null&&hostUser.getDesignerStatus()!=null&&hostUser.getDesignerStatus()==2){//设计师通过
				//显示发布按钮
				btnNewAlbum.setVisibility(View.VISIBLE);
				btnNewAlbum.setOnClickListener(onClickListener);
				fansView.setOnClickListener(onClickListener);//设计师才能点击查看粉丝列表
			}else{
				//显示申请按钮
				btnApplyDesigner.setVisibility(View.VISIBLE);
				btnApplyDesigner.setOnClickListener(onClickListener);
				
			}
		}
		
		//普通用户展示申请设计师，设计师展示发布新作品
//		if(!AppApplication.isGuest()){
//			btnNewAlbum = (Button)headerView.findViewById(R.id.btnNewAlbum);
//			btnNewAlbum.setVisibility(View.VISIBLE);
//			btnNewAlbum.setOnClickListener(onClickListener);
//		}
		
		btnUserInfo = (Button)headerView.findViewById(R.id.btnUserInfo);
		btnUserInfo.setOnClickListener(onClickListener);
		
//		//启动获取个人资料详情
//		getUserinfo(HOST_ID);
//		//获取个人专辑
//		getAlbums(HOST_ID, 0);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(!AppApplication.isGuest()){//登录用户进入时，才需要重刷数据
			//判断缓存时间
			long currentTime = System.currentTimeMillis();
			String tabRefreshKey = getRefreshKey();
			long lastRefreshTime = SharedPreferenceUtil.getSharePreLong(activity, tabRefreshKey, 0l);
			long interval = currentTime - lastRefreshTime;
			if(interval > (TimeUtil.TIME_UNIT_MINUTE*2)){//距离上次请求时间>2分钟，才再次刷新（减少请求次数）
				pullRefreshView.setRefreshing(false);
			}
		}
	}
	
	private void getUserinfo(final int userId) {
		//启动线程获取数据
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message message;
				
				MyInfoApi api = new MyInfoApi();
				ApiResult apiResult = ApiManager.invoke(activity, api);
				message = handler.obtainMessage(HANDLER_FLAG_USERINFO_RESULT);
				message.obj = apiResult;
				message.sendToTarget();
//				if(apiResult!=null&&apiResult.getResult()==1){
//					message = handler.obtainMessage(HANDLER_FLAG_USERINFO_RESULT);
//					message.obj = apiResult.getData();
//					message.sendToTarget();
//				}
			}
		});
		thread.start();
	}
	
	
	private void getAlbums(final int queryUserId, final int albumTailId) {
		//启动线程获取数据
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message message;
				
				AlbumListApi api = new AlbumListApi(HOST_ID, albumTailId);
				ApiResult jsonResult = ApiManager.invoke(activity, api);
				message = handler.obtainMessage(HANDLER_FLAG_SLIDE_RESULT);
				message.obj = jsonResult;
				message.sendToTarget();
//				if(jsonResult!=null&&jsonResult.getResult()==1){
//					message = handler.obtainMessage(HANDLER_FLAG_SLIDE_RESULT);
//					message.obj = jsonResult.getData();
//					message.sendToTarget();
//				}
			}
		});
		thread.start();
	}
	
	private OnClickListener initListener(){
		OnClickListener listener = new OnSingleClickListener() {
			@Override
			public void onSingleClick(View view) {
	
				switch (view.getId()) {
				
				case R.id.btnSettings:
					StatService.onEvent(activity, ConstantsStatEvent.EVENT_VIEW_SETTINGS, "我的Fragment中查看设置");
					
					Activity_Settings.show(activity);
					break;
				case R.id.followsContainer:
					StatService.onEvent(activity, ConstantsStatEvent.EVENT_VIEW_FOLLOWS, "我的Fragment中查看关注页");
					
					if(AppApplication.isGuest()){
						DesignerUtil.guideGuestLogin(activity, "提示", "游客身份无法查看关注，请先登录");
					}else{
						Activity_UserFollows.show(activity, HOST_ID);
					}
					break;
				case R.id.fansContainer:
					StatService.onEvent(activity, ConstantsStatEvent.EVENT_VIEW_FANS, "我的Fragment中查看粉丝页");
					
					if(AppApplication.isGuest()){
						DesignerUtil.guideGuestLogin(activity, "提示", "游客身份无法查看粉丝，请先登录");
					}else{
						User hostUser = AppApplication.getHostUser();
						if(hostUser!=null&&hostUser.getDesignerStatus()!=null&&hostUser.getDesignerStatus()==2){//设计师身份
							//可以查看粉丝列表的功能
							Activity_UserFans.show(activity, HOST_ID);
						}else{
							DialogInterface.OnClickListener onclickListener = new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							};
							UiUtil.showAlertDialog(activity, true, null, "提示", "非设计师无法查看粉丝", "我知道了", onclickListener, null,null).show();
						}
					}
					break;
				case R.id.btnMyFavorite:
					StatService.onEvent(activity, ConstantsStatEvent.EVENT_VIEW_FAVORITES, "我的Fragment中查看收藏");
					
					Activity_MyFavorite.show(activity);
					break;
				case R.id.btnLogin:
					StatService.onEvent(activity, ConstantsStatEvent.EVENT_LOGIN, "我的Fragment中点击登录");
					//跳转到登录界面
					AppManager.getInstance().finishAllActivity();
					Intent loginIntent = new Intent(activity, Activity_Login.class);
					activity.startActivity(loginIntent);
					break;
				case R.id.btnCreateAlbum:
					StatService.onEvent(activity, ConstantsStatEvent.EVENT_CREATE_ALBUM, "我的Fragment中发布作品");
					
					UiUtil.showAlertDialog(activity, true, null, "提示", "客户端操作受限，请前往网站【www.jinwanr.com】进行发布", "我知道了", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							 dialog.dismiss(); //销毁窗口
						}
					}, null,null).show();
					break;
				case R.id.btnApplyDesigner:
					StatService.onEvent(activity, ConstantsStatEvent.EVENT_APPLY_DESIGNER, "我的Fragment中申请");
					
					UiUtil.showAlertDialog(activity, true, null, "提示", "客户端操作受限，请前往网站【www.jinwanr.com】进行申请", "我知道了", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							 dialog.dismiss(); //销毁窗口
						}
					}, null,null).show();
					
					break;
	//			case R.id.btnPubAlbum:
	//				UiUtil.showLongToast(activity, "抱歉，客户端暂不支持发布专辑\r\n请前往【金玩儿网】网站发布您的专辑作品");
	//				break;
				case R.id.btnUserInfo:
					StatService.onEvent(activity, ConstantsStatEvent.EVENT_VIEW_PROFILE, "我的Fragment中点击个人资料");
					
					if(AppApplication.isGuest()){
						Activity_UserInfo.show(activity, HOST_ID);
					}else{
						Intent intent = new Intent(activity, Activity_UserInfo.class);
						intent.putExtra(ConstantsKey.BUNDLE_USER_INFO_ID, HOST_ID);
						startActivityForResult(intent, REQUEST_CODE_USERINFO);
					}
					break;
				default:
					break;
				}
			}
		};
		return listener;
	}
	
	/**
	 * 下拉刷新
	 */
	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		//获取个人资料详情
		getUserinfo(HOST_ID);
		//获取个人专辑
		getAlbums(HOST_ID, 0);
	}
	
	/**
	 * 上拉刷新
	 */
	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		//加载更多专辑信息
		getAlbums(HOST_ID, albumTailId);
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if(requestCode==REQUEST_CODE_USERINFO){//如果是来自userInfo的回调
			//检查是否有头像变更
			if(resultCode==RESULT_CODE_AVATAR_CHANGED){
				byte[] avatarBytes = intent.getByteArrayExtra("avatarData");
				if(avatarBytes!=null&&avatarBytes.length>0){
					avatarView.setImageBitmap(ImageUtil.bytes2Bimap(avatarBytes));
				}
			}
		}
	}
	
	private void broadcastAlbumOperated(int albumId) {
		//发送album被变更的广播
		Intent intent = new Intent(ConstantsKey.BroadcastActionEnum.ALBUM_OPERATED.getAction());
		intent.putExtra(ConstantsKey.BUNDLE_BROADCAST_KEY, ConstantsKey.BROADCAST_ALBUM_OPERATED);
		intent.putExtra(ConstantsKey.BUNDLE_BROADCAST_KEY_OPERATED_ALBUMID, albumId);
		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
	}
	
	/**
	 * 我的主页中刷新的sp-key
	 * @param tabIndex
	 * @return
	 */
	private static String getRefreshKey(){
		return ConstantsKey.LAST_REFRESH_TIME_MYHOME_PREFIX;
	}
}
