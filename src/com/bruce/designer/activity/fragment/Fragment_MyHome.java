package com.bruce.designer.activity.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bruce.designer.AppApplication;
import com.bruce.designer.R;
import com.bruce.designer.activity.Activity_MyFavorite;
import com.bruce.designer.activity.Activity_Settings;
import com.bruce.designer.activity.Activity_UserFans;
import com.bruce.designer.activity.Activity_UserFollows;
import com.bruce.designer.activity.Activity_UserInfo;
import com.bruce.designer.adapter.DesignerAlbumsAdapter;
import com.bruce.designer.api.ApiManager;
import com.bruce.designer.api.album.AlbumListApi;
import com.bruce.designer.api.user.UserInfoApi;
import com.bruce.designer.listener.OnSingleClickListener;
import com.bruce.designer.model.Album;
import com.bruce.designer.model.User;
import com.bruce.designer.model.result.ApiResult;
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
	
	private static final int HANDLER_FLAG_USERINFO = 1;
	private static final int HANDLER_FLAG_SLIDE = 2;
	
	
	private static final int HOST_ID = AppApplication.getUserPassport().getUserId();
	
	private Activity activity; 
	private LayoutInflater inflater;
	
	private TextView titleView;
	
	/*设计师头像*/
	private ImageView avatarView;
	
	private View followsView;
	private View fansView;
	
	private TextView followsNumView;
	private TextView fansNumView;
	
	private Button btnMyFavorite;
	private Button btnSendMsg;
	private Button btnUserInfo;
//	private Button btnPubAlbum;//发布新作品
	
	private ImageButton btnSettings;
	private PullToRefreshListView pullRefreshView;
	private DesignerAlbumsAdapter albumListAdapter; 
	
	private int albumTailId = 0;
	
	
	private Handler handler = new Handler(){

		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			switch(msg.what){
				case HANDLER_FLAG_USERINFO:
					Map<String, Object> userinfoDataMap = (Map<String, Object>) msg.obj;
					if(userinfoDataMap!=null){
						User userinfo = (User) userinfoDataMap.get("userinfo");
						int fansCount = (Integer) userinfoDataMap.get("fansCount");
						int followsCount = (Integer) userinfoDataMap.get("followsCount");
						if(userinfo!=null&&userinfo.getId()>0){
							if(AppApplication.isHost(userinfo.getId())){
								//缓存到sp
								AppApplication.setHostUser(userinfo);
							}
							
							titleView.setText(userinfo.getNickname());
							fansNumView.setText(String.valueOf(fansCount));
							followsNumView.setText(String.valueOf(followsCount));
							
							//显示头像
							ImageLoader.getInstance().displayImage(userinfo.getHeadImg(), avatarView, UniversalImageUtil.DEFAULT_AVATAR_DISPLAY_OPTION);
						}
					}
					break;
				case HANDLER_FLAG_SLIDE:
					pullRefreshView.onRefreshComplete();
					Map<String, Object> albumsDataMap = (Map<String, Object>) msg.obj;
					if(albumsDataMap!=null){
						List<Album> albumList = (List<Album>) albumsDataMap.get("albumList");
						Integer fromTailId = (Integer) albumsDataMap.get("fromTailId");
						Integer newTailId = (Integer) albumsDataMap.get("newTailId");
						if(albumList!=null&&albumList.size()>0){
							
							if(newTailId!=null&&newTailId>0){//还有可加载的数据
								albumTailId = newTailId;
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
						}else{
							pullRefreshView.setMode(Mode.PULL_FROM_START);//禁用下拉刷新
						}
					}
					break;
				default:
					break;
			}
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity = getActivity();
		this.inflater = inflater;
		
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
		btnSettings.setOnClickListener(listener);
		btnSettings.setVisibility(View.VISIBLE);

		pullRefreshView = (PullToRefreshListView) mainView.findViewById(R.id.pull_refresh_list);
		pullRefreshView.setMode(Mode.PULL_FROM_END);
		pullRefreshView.setOnRefreshListener(this);
		ListView albumListView = pullRefreshView.getRefreshableView();
		albumListAdapter = new DesignerAlbumsAdapter(activity, null);
		albumListView.setAdapter(albumListAdapter);
		
		//把个人资料的layout作为listview的header
		View headerView = inflater.inflate(R.layout.user_home_head, null);
		albumListView.addHeaderView(headerView);
		
		avatarView = (ImageView) headerView.findViewById(R.id.avatar);
		
		fansView = (View) headerView.findViewById(R.id.fansContainer);
		fansNumView = (TextView) headerView.findViewById(R.id.txtFansNum);
		fansView.setOnClickListener(listener);
		
		followsView = (View) headerView.findViewById(R.id.followsContainer);
		followsNumView = (TextView) headerView.findViewById(R.id.txtFollowsNum);
		followsView.setOnClickListener(listener);
		
		
		btnSendMsg = (Button)headerView.findViewById(R.id.btnSendMsg);
		btnSendMsg.setVisibility(View.GONE);
		
		btnMyFavorite = (Button)headerView.findViewById(R.id.btnMyFavorite);
		btnMyFavorite.setOnClickListener(listener);
		btnMyFavorite.setVisibility(View.VISIBLE);

//		btnPubAlbum = (Button)headerView.findViewById(R.id.btnPubAlbum);
//		btnPubAlbum.setVisibility(View.VISIBLE);
//		btnPubAlbum.setOnClickListener(listener);
		
		btnUserInfo = (Button)headerView.findViewById(R.id.btnUserInfo);
		btnUserInfo.setOnClickListener(listener);
		
//		//启动获取个人资料详情
//		getUserinfo(hostId);
//		//获取个人专辑
//		getAlbums(hostId, 0);
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		pullRefreshView.setRefreshing(false);
	}
	
	private void getUserinfo(final int userId) {
		//启动线程获取数据
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message message;
				
				UserInfoApi api = new UserInfoApi(userId);
				ApiResult apiResult = ApiManager.invoke(activity, api);
				if(apiResult!=null&&apiResult.getResult()==1){
					message = handler.obtainMessage(HANDLER_FLAG_USERINFO);
					message.obj = apiResult.getData();
					message.sendToTarget();
				}
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
				
				if(jsonResult!=null&&jsonResult.getResult()==1){
					message = handler.obtainMessage(HANDLER_FLAG_SLIDE);
					message.obj = jsonResult.getData();
					message.sendToTarget();
				}
			}
		});
		thread.start();
	}
	
	
	private OnClickListener listener = new OnSingleClickListener() {
		@Override
		public void onSingleClick(View view) {

			switch (view.getId()) {
			case R.id.btnSettings:
				Activity_Settings.show(activity);
				break;
			case R.id.followsContainer:
				Activity_UserFollows.show(activity, HOST_ID);
				break;
			case R.id.fansContainer:
				Activity_UserFans.show(activity, HOST_ID);
				break;
			case R.id.btnMyFavorite:
				Activity_MyFavorite.show(activity);
				break;
//			case R.id.btnPubAlbum:
//				UiUtil.showLongToast(activity, "抱歉，客户端暂不支持发布专辑\r\n请前往【金玩儿网】网站发布您的专辑作品");
//				break;
			case R.id.btnUserInfo:
				Activity_UserInfo.show(activity, HOST_ID);
				break;
			default:
				break;
			}
		}
	};
	
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
	
}
