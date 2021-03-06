package com.bruce.designer.activity;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.bruce.designer.AppApplication;
import com.bruce.designer.R;
import com.bruce.designer.api.ApiManager;
import com.bruce.designer.api.user.PostFollowApi;
import com.bruce.designer.api.user.UserFollowsApi;
import com.bruce.designer.broadcast.NotificationBuilder;
import com.bruce.designer.constants.Config;
import com.bruce.designer.constants.ConstantsKey;
import com.bruce.designer.constants.ConstantsStatEvent;
import com.bruce.designer.handler.DesignerHandler;
import com.bruce.designer.listener.OnSingleClickListener;
import com.bruce.designer.model.UserFollow;
import com.bruce.designer.model.result.ApiResult;
import com.bruce.designer.util.DesignerUtil;
import com.bruce.designer.util.UiUtil;
import com.bruce.designer.util.UniversalImageUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class Activity_UserFollows extends BaseActivity implements OnRefreshListener2<ListView>{

	private static final int HANDLER_FLAG_FOLLOW_RESULT = 100;
	private static final int HANDLER_FLAG_UNFOLLOW_RESULT = 101;

	private static final int HANDLER_FLAG_USERFOLLOWS_RESULT = 1;
	
	
	private View titlebarView;

	private TextView titleView;
	
	private FollowsListAdapter followsListAdapter;

	private int queryUserId;
	private boolean isHost;
	
	private PullToRefreshListView pullRefreshView =null;
	private Handler handler;
	private OnClickListener onClickListener;
	
	
	public static void show(Context context, int queryUserId){
		if(!AppApplication.isGuest(queryUserId)){//游客木有个人主页
			Intent intent = new Intent(context, Activity_UserFollows.class);
			intent.putExtra(ConstantsKey.BUNDLE_USER_INFO_ID, queryUserId);
			context.startActivity(intent);
		}
	}
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_follows);
		handler = initHandler();
		onClickListener = initListener();
		
		
		Intent intent = getIntent();
		//获取userid
		queryUserId = intent.getIntExtra(ConstantsKey.BUNDLE_USER_INFO_ID, 0);
		isHost = AppApplication.isHost(queryUserId);
		
		//init view
		titlebarView = findViewById(R.id.titlebar_return);
		titlebarView.setOnClickListener(onClickListener);
		titleView = (TextView) findViewById(R.id.titlebar_title);
		titleView.setText((isHost?"我":"TA") + "的关注");
		
		pullRefreshView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		ListView followsListView = pullRefreshView.getRefreshableView();
		pullRefreshView.setMode(Mode.PULL_FROM_START);
		pullRefreshView.setOnRefreshListener(this);
		
		followsListAdapter = new FollowsListAdapter(context, null, null);
		followsListView.setAdapter(followsListAdapter);
		
		pullRefreshView.setRefreshing(false);
		//获取关注列表
//		getFollows(queryUserId);
	}
	
	
	class FollowsListAdapter extends BaseAdapter {

		private List<UserFollow> followUserList;
		private Map<Integer, Boolean> followUserMap;
		private Context context;
		
		public FollowsListAdapter(Context context, List<UserFollow> followUserList, Map<Integer, Boolean> followUserMap) {
			this.context = context;
			this.followUserList = followUserList;
			this.followUserMap = followUserMap;
		}
		
		public List<UserFollow> getFollowUserList() {
			return followUserList;
		}

		public void setFollowUserList(List<UserFollow> followUserList) {
			this.followUserList = followUserList;
		}

		public Map<Integer, Boolean> getFollowUserMap() {
			return followUserMap;
		}

		public void setFollowUserMap(Map<Integer, Boolean> followUserMap) {
			this.followUserMap = followUserMap;
		}

		@Override
		public int getCount() {
			if (followUserList != null) {
				return followUserList.size();
			}
			return 0;
		}

		@Override
		public UserFollow getItem(int position) {
			if (followUserList != null) {
				return followUserList.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//使用convertView
			final UserFollow user = getItem(position);
			FollowViewHolder viewHolder = null;
			if(convertView==null){
				viewHolder = new FollowViewHolder();
				if(user!=null){
					convertView = inflater.inflate(R.layout.item_friend_view, null);
					
					viewHolder.friendView = (View) convertView.findViewById(R.id.friendContainer);;
					viewHolder.avatarView = (ImageView) convertView.findViewById(R.id.avatar);
					viewHolder.usernameView = (TextView) convertView.findViewById(R.id.username);
					//构造关注状态
					viewHolder.btnFollow = (Button) convertView.findViewById(R.id.btnFollow);
					viewHolder.btnUnfollow = (Button) convertView.findViewById(R.id.btnUnfollow);
					viewHolder.btnSendMsg = (Button) convertView.findViewById(R.id.btnSendMsg);
				}
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (FollowViewHolder) convertView.getTag();
			}
			
			final FollowViewHolder followViewHolder = viewHolder;
			//填充数据
			final int followUserId = user.getFollowId();
			final String followNickname = user.getFollowUser().getNickname();
			final String followAvatar = user.getFollowUser()!=null?user.getFollowUser().getHeadImg():null;
			
			final boolean isDesigner = true;
			final boolean hasFollowed = true;
			
			viewHolder.usernameView.setText(user.getFollowUser().getNickname());
			
			//显示头像
			ImageLoader.getInstance().displayImage(user.getFollowUser().getHeadImg(), viewHolder.avatarView, UniversalImageUtil.DEFAULT_AVATAR_DISPLAY_OPTION);
			followViewHolder.friendView.setOnClickListener(new OnSingleClickListener() {
				@Override
				public void onSingleClick(View view) {
					StatService.onEvent(context, ConstantsStatEvent.EVENT_VIEW_HOME, "关注页中查看个人主页");
					
					Activity_UserHome.show(context, followUserId, followNickname , followAvatar, isDesigner, hasFollowed);
				}
			});
			
			
			if(AppApplication.isHost(followUserId)){//查看的用户为自己，需要隐藏交互按钮
				
			}else{
				if(followUserMap!=null){
					if(Boolean.TRUE.equals(followUserMap.get(followUserId))){
						followViewHolder.btnFollow.setVisibility(View.GONE);
						followViewHolder.btnUnfollow.setVisibility(View.VISIBLE);
					}else if(Boolean.FALSE.equals(followUserMap.get(followUserId))){
						followViewHolder.btnFollow.setVisibility(View.VISIBLE);
						followViewHolder.btnUnfollow.setVisibility(View.GONE);
					}
				}
				
				//关注事件
				followViewHolder.btnFollow.setOnClickListener(new OnSingleClickListener() {
					@Override
					public void onSingleClick(View v) {
						StatService.onEvent(context, ConstantsStatEvent.EVENT_FOLLOW, "关注页中点击关注");
						if(AppApplication.isGuest()){
							DesignerUtil.guideGuestLogin(context, "提示", "游客身份无法关注，请先登录");
						}else{
							followViewHolder.btnUnfollow.setVisibility(View.VISIBLE);
							followViewHolder.btnFollow.setVisibility(View.GONE);
							new Thread(new Runnable(){
								@Override
								public void run() {
									PostFollowApi api = new PostFollowApi(followUserId, 1);
									ApiResult apiResult = ApiManager.invoke(context, api);
									Message message = handler.obtainMessage(HANDLER_FLAG_FOLLOW_RESULT);
									message.obj = apiResult;
									message.sendToTarget();
	//								if(apiResult!=null&&apiResult.getResult()==1){
	//									handler.obtainMessage(HANDLER_FLAG_FOLLOW_RESULT).sendToTarget();
	//								}
								}
							}).start();
						}
					}
				});
				//取消关注事件
				followViewHolder.btnUnfollow.setOnClickListener(new OnSingleClickListener() {
					@Override
					public void onSingleClick(View v) {
						StatService.onEvent(context, ConstantsStatEvent.EVENT_FOLLOW, "关注页中点击取消关注");
						
						if(AppApplication.isGuest()){
							DesignerUtil.guideGuestLogin(context, "提示", "游客身份无法取消关注，请先登录");
						}else{
							followViewHolder.btnFollow.setVisibility(View.VISIBLE);
							followViewHolder.btnUnfollow.setVisibility(View.GONE);
							new Thread(new Runnable(){
								@Override
								public void run() {
									PostFollowApi api = new PostFollowApi(followUserId, 0);
									ApiResult apiResult = ApiManager.invoke(context, api);
									Message message = handler.obtainMessage(HANDLER_FLAG_UNFOLLOW_RESULT);
									message.obj = apiResult;
									message.sendToTarget();
	//								if(apiResult!=null&&apiResult.getResult()==1){
	//									handler.obtainMessage(HANDLER_FLAG_UNFOLLOW_RESULT).sendToTarget();
	//								}
								}
							}).start();
						}
					}
				});
				
				//私信事件
				followViewHolder.btnSendMsg.setOnClickListener(new OnSingleClickListener() {
					@Override
					public void onSingleClick(View v) {
						StatService.onEvent(context, ConstantsStatEvent.EVENT_VIEW_CHAT, "关注页中打开私信");
						
						Activity_MessageChat.show(context, followUserId, followNickname, user.getFollowUser().getHeadImg());
					}	
				});
			}
			return convertView;
		}
	}
	
	/**
	 * 获取关注列表
	 * @param followsTailId
	 */
	private void getFollows(final int followsTailId) {
		//启动线程获取数据
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message message;
				
				UserFollowsApi api = new UserFollowsApi(queryUserId);
				ApiResult apiResult = ApiManager.invoke(context, api);
				message = handler.obtainMessage(HANDLER_FLAG_USERFOLLOWS_RESULT);
				message.obj = apiResult;
				message.sendToTarget();
//				if(jsonResult!=null&&jsonResult.getResult()==1){
//					message = handler.obtainMessage(0);
//					message.obj = jsonResult.getData();
//					message.sendToTarget();
//				}
			}
		});
		thread.start();
	}
	
	private Handler initHandler(){
		Handler handler = new DesignerHandler(context){
			@SuppressWarnings("unchecked")
			public void processHandlerMessage(Message msg) {
				ApiResult apiResult = (ApiResult) msg.obj;
				boolean successResult = (apiResult!=null&&apiResult.getResult()==1);
				switch(msg.what){
					case HANDLER_FLAG_USERFOLLOWS_RESULT:
						pullRefreshView.onRefreshComplete();
						if(successResult){
							Map<String, Object> userFollowsDataMap = (Map<String, Object>) apiResult.getData();
							if(userFollowsDataMap!=null){
								List<UserFollow> followList = (List<UserFollow>)  userFollowsDataMap.get("followList");
								Map<Integer, Boolean> followMap = (Map<Integer, Boolean>)  userFollowsDataMap.get("followMap");
								if(followList!=null&&followList.size()>0){
									followsListAdapter.setFollowUserList(followList);
									followsListAdapter.setFollowUserMap(followMap);
									followsListAdapter.notifyDataSetChanged();
								}
							}
						}else{
							//UiUtil.showShortToast(context, "获取关注数据失败，请重试");
							UiUtil.showShortToast(context, Config.RESPONSE_ERROR);
						}
						break;
					case HANDLER_FLAG_FOLLOW_RESULT:
						//广播
						NotificationBuilder.createNotification(context, successResult?"您已成功关注":"关注失败");
						break;
					case HANDLER_FLAG_UNFOLLOW_RESULT:
						//广播
						NotificationBuilder.createNotification(context, successResult?"取消关注成功":"取消关注失败");
						break;
					default:
						break;
				}
			}
		};
		return handler;
	}
	
	private OnClickListener initListener(){
		OnClickListener listener = new OnSingleClickListener() {
			@Override
			public void onSingleClick(View view) {
				switch (view.getId()) {
				case R.id.titlebar_return:
					finish();
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
		//获取关注列表
		getFollows(queryUserId);
	}
	
	/**
	 * 上拉刷新
	 */
	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		
	}
	
	/**
	 * viewHolder
	 * @author liqian
	 *
	 */
	static class FollowViewHolder {
		public View friendView;
		public TextView usernameView;
		public ImageView avatarView;

		//关注按钮
		public Button btnFollow;
		public Button btnUnfollow;
		//私信按钮
		public Button btnSendMsg;
	}
	
}
