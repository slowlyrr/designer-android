package com.bruce.designer.activity.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.bruce.designer.R;
import com.bruce.designer.activity.Activity_Settings;
import com.bruce.designer.adapter.DesignerAlbumsAdapter;
import com.bruce.designer.adapter.ViewPagerAdapter;
import com.bruce.designer.api.AbstractApi;
import com.bruce.designer.api.ApiManager;
import com.bruce.designer.api.album.AlbumListApi;
import com.bruce.designer.api.album.AlbumRecommendApi;
import com.bruce.designer.api.album.FollowAlbumListApi;
import com.bruce.designer.broadcast.NotificationBuilder;
import com.bruce.designer.constants.ConstantsKey;
import com.bruce.designer.db.album.AlbumDB;
import com.bruce.designer.listener.IOnAlbumListener;
import com.bruce.designer.listener.OnAlbumListener;
import com.bruce.designer.listener.OnSingleClickListener;
import com.bruce.designer.model.Album;
import com.bruce.designer.model.result.ApiResult;
import com.bruce.designer.util.SharedPreferenceUtil;
import com.bruce.designer.util.TimeUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class Fragment_Main extends BaseFragment{

	private static final int HANDLER_FLAG_TAB0 = 0;
	private static final int HANDLER_FLAG_TAB1 = 1;
	private static final int HANDLER_FLAG_TAB2 = 2;
	
	private static final int HANDLER_FLAG_ERROR = -1;
//	private static final int HANDLER_FLAG_TAB0_ERROR = 10;
//	private static final int HANDLER_FLAG_TAB1_ERROR = 11;
	
	
	/* tab个数*/
	private static final int TAB_NUM = 3;
	/* 当前tab*/
	private int currentTab = 0;
	/* 各tab的tailId*/
	private int[] tabAlbumTailIds = new int[TAB_NUM];
	
	private View mainView; 
	private ViewPager viewPager;
	private View[] tabViews = new View[TAB_NUM];
	private View[] tabIndicators = new View[TAB_NUM];
	
	/*下拉控件*/
	private PullToRefreshListView[] pullRefreshViews = new PullToRefreshListView[TAB_NUM];
	private ListView[] listViews = new ListView[TAB_NUM];
	private DesignerAlbumsAdapter[] listViewAdapters = new DesignerAlbumsAdapter[TAB_NUM];
	
	private ImageButton btnRefresh;
	
	private Activity activity;
	private LayoutInflater inflater;
	private ImageButton btnSettings;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity = getActivity();
		this.inflater = inflater;
		
		mainView = inflater.inflate(R.layout.fragment_main, null);
		initView(mainView);
		return mainView;
	}
	
	private void initView(View view) {
		tabIndicators[0] = view.findViewById(R.id.tab_recommend_indicator);
		tabIndicators[1] = view.findViewById(R.id.tab_latest_indicator);
		tabIndicators[2] = view.findViewById(R.id.tab_myview_indicator);
		
		tabViews[0] = view.findViewById(R.id.tab_recommend);
		tabViews[1] = view.findViewById(R.id.tab_latest);
		tabViews[2] = view.findViewById(R.id.tab_myview);

		//响应事件
		for(int i=0;i<tabViews.length;i++){
			View tabView = tabViews[i];
			final int tabIndex = i;
			tabView.setOnClickListener(new OnSingleClickListener() {
				@Override
				public void onSingleClick(View view) {
					highLightTab(tabIndex);
				}
			});
		}
		
		//刷新按钮及点击事件
		btnRefresh = (ImageButton) view.findViewById(R.id.btnRefresh);
		btnRefresh.setOnClickListener(listener);
		//setting按钮及点击事件
		btnSettings = (ImageButton) view.findViewById(R.id.btnSettings);
		btnSettings.setOnClickListener(listener);
		
		//构造viewPager
		viewPager = (ViewPager) view.findViewById(R.id.viewPager);
		List<View> pagerViews = new ArrayList<View>();
		//初始化tab页的view
		for(int i=0;i<TAB_NUM; i++){
			View pageView = inflater.inflate(R.layout.albums_listview, null);
			
			pullRefreshViews[i] = (PullToRefreshListView) pageView.findViewById(R.id.pull_refresh_list);
			pullRefreshViews[i].setMode(Mode.PULL_FROM_START);
			pullRefreshViews[i].setOnRefreshListener(new TabedRefreshListener(i));
			listViews[i] = pullRefreshViews[i].getRefreshableView();
			listViewAdapters[i] = new DesignerAlbumsAdapter(activity, null, new OnAlbumListener(activity, tabDataHandler, mainView));
			listViews[i].setAdapter(listViewAdapters[i]);
			
			//将views加入viewPager
			pagerViews.add(pageView);
		}
		//viewPager的适配器
		ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(activity, pagerViews);
		viewPager.setAdapter(viewPagerAdapter);
		viewPager.setOnPageChangeListener(viewPagerListener);
		
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		//根据tabIndex展示相应数据
		highLightTab(currentTab);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	/**
	 * 高亮显示上部的Tab
	 * @param tabIndex
	 */
	private void highLightTab(int tabIndex){
		if(tabIndex>=TAB_NUM){
			tabIndex = 0;
		}
		for(View tabIndicator: tabIndicators){
			tabIndicator.setVisibility(View.GONE);//全部隐藏
		}
		currentTab = tabIndex;
		//显示
		tabIndicators[currentTab].setVisibility(View.VISIBLE);
		viewPager.setCurrentItem(currentTab);
		
		//判断tab的上次刷新时间
		long currentTime = System.currentTimeMillis();
		String tabRefreshKey = getRefreshKey(currentTab);
		long lastRefreshTime = SharedPreferenceUtil.getSharePreLong(activity, tabRefreshKey, 0l);
		long interval = currentTime - lastRefreshTime;
		//相应page上请求数据
		List<Album> albumList = null;
		if(currentTab==1){
			albumList= AlbumDB.queryAllLatest(activity);//请求最新数据
		}else if(currentTab==2){
			albumList= AlbumDB.queryAllFollow(activity);//请求关注数据
		}else{
			albumList= AlbumDB.queryAllRecommend(activity);//请求系统推荐数据
		}
		
		//自动刷新
		listViewAdapters[currentTab].setAlbumList(albumList);
		listViewAdapters[currentTab].notifyDataSetChanged();
		
		if(albumList==null || albumList.size() ==0 || interval > TimeUtil.TIME_UNIT_MINUTE){
			pullRefreshViews[currentTab].setRefreshing(false);
		}
	}
	
	
	/**
	 * 下拉刷新listener
	 * @author liqian
	 *
	 */
	class TabedRefreshListener implements OnRefreshListener2<ListView>{
		
		private int tabIndex;

		public TabedRefreshListener(int tabIndex){
			this.tabIndex = tabIndex;
		}

		@Override
		public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
			//tab下拉请求新数据
			getAlbums(0, tabIndex);
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
			//tab上拉请求增量数据
			getAlbums(tabAlbumTailIds[tabIndex], tabIndex);
		}
	}
	
	
	private void getAlbums(final int albumTailId, final int tabIndex) {
		//启动线程获取数据
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message message;
				AbstractApi api = null;
				if(tabIndex==1){
					api = new AlbumListApi(0, albumTailId);
				}else if(tabIndex==2){
					api = new FollowAlbumListApi(albumTailId);
				}else{
					api = new AlbumRecommendApi();
				}
				ApiResult jsonResult = ApiManager.invoke(activity, api);
				if(jsonResult!=null&&jsonResult.getResult()==1){
					message = tabDataHandler.obtainMessage(tabIndex);
					message.obj = jsonResult.getData();
					message.sendToTarget(); 
				}else{//发送失败消息
					int errorFlag = HANDLER_FLAG_ERROR;
					tabDataHandler.obtainMessage(errorFlag).sendToTarget();
				}
			}
		});
		thread.start();
	}
	
	private Handler tabDataHandler = new Handler(){
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch(what){
				case HANDLER_FLAG_TAB0:
					pullRefreshViews[0].onRefreshComplete();
					Map<String, Object> tab0DataMap = (Map<String, Object>) msg.obj;
					if(tab0DataMap!=null){
						List<Album> albumList = (List<Album>) tab0DataMap.get("albumList");
						if(albumList!=null&&albumList.size()>0){
							AlbumDB.deleteByTab(activity, 0);
							//save to db
							AlbumDB.saveAlbumsByTab(activity, albumList, 0);
							listViewAdapters[0].setAlbumList(albumList);
							listViewAdapters[0].notifyDataSetChanged();
						}
					}
					SharedPreferenceUtil.putSharePre(activity, getRefreshKey(0), System.currentTimeMillis());
					break;
				case HANDLER_FLAG_TAB1:
				case HANDLER_FLAG_TAB2:
					int tabIndex = what;
					//关闭刷新控件
					pullRefreshViews[tabIndex].onRefreshComplete();
					
					Map<String, Object> tabedDataMap = (Map<String, Object>) msg.obj;
					if(tabedDataMap!=null){
						List<Album> albumList = (List<Album>) tabedDataMap.get("albumList");
						Integer fromTailId = (Integer) tabedDataMap.get("fromTailId");
						Integer newTailId = (Integer) tabedDataMap.get("newTailId");
						if(albumList!=null&&albumList.size()>0){
							//缓存本次刷新的时间
							SharedPreferenceUtil.putSharePre(activity, getRefreshKey(tabIndex), System.currentTimeMillis());
							if(newTailId!=null&&newTailId>0){//还有可加载的数据
								tabAlbumTailIds[tabIndex] = newTailId;
								pullRefreshViews[tabIndex].setMode(Mode.BOTH);
							}else{
								tabAlbumTailIds[tabIndex] = 0;
								pullRefreshViews[tabIndex].setMode(Mode.PULL_FROM_START);//禁用上拉刷新
							}
							
							List<Album> oldAlbumList = listViewAdapters[tabIndex].getAlbumList();
							if(oldAlbumList==null){
								oldAlbumList = new ArrayList<Album>();
							}
							//判断加载位置，以确定是list增量还是覆盖
							boolean fallloadAppend = fromTailId!=null&&fromTailId>0;
							if(fallloadAppend){//上拉加载更多，需添加至list的结尾
								oldAlbumList.addAll(albumList);
							}else{//下拉加载，需覆盖原数据
								AlbumDB.deleteByTab(activity, tabIndex);
								AlbumDB.saveAlbumsByTab(activity, albumList, tabIndex);
								oldAlbumList = null;
								oldAlbumList = albumList; 
							}
							listViewAdapters[tabIndex].setAlbumList(oldAlbumList);
							listViewAdapters[tabIndex].notifyDataSetChanged();
						}
					}
					break;
				case IOnAlbumListener.HANDLER_FLAG_LIKE_POST: //赞成功
					int likedAlbumId = (Integer) msg.obj;
					AlbumDB.updateLikeStatus(activity, likedAlbumId, 1, 1);//更新db状态
					//更新ui展示
					List<Album> albumList4Like = listViewAdapters[currentTab].getAlbumList();
					if(albumList4Like!=null&&albumList4Like.size()>0){
						for(Album album: albumList4Like){
							if(album.getId()!=null&&album.getId()==likedAlbumId){
								album.setLike(true);
								long likeCount = album.getLikeCount();
								album.setLikeCount(likeCount+1);
								break;
							}
						}
						listViewAdapters[currentTab].notifyDataSetChanged();
					}
					//发送广播
					NotificationBuilder.createNotification(activity, "赞操作成功...");
					break;
				case IOnAlbumListener.HANDLER_FLAG_FAVORITE_POST: //收藏成功
					int favoritedAlbumId = (Integer) msg.obj;
					AlbumDB.updateFavoriteStatus(activity, favoritedAlbumId, 1, 1);//更新db状态
					//更新ui展示
					List<Album> albumList4Favorite = listViewAdapters[currentTab].getAlbumList();
					if(albumList4Favorite!=null&&albumList4Favorite.size()>0){
						for(Album album: albumList4Favorite){
							if(album.getId()!=null&&album.getId()==favoritedAlbumId){
								long favoriteCount = album.getFavoriteCount();
								album.setFavoriteCount(favoriteCount+1);
								album.setFavorite(true);
								break;
							}
						}
						listViewAdapters[currentTab].notifyDataSetChanged();
					}
					//发送广播
					NotificationBuilder.createNotification(activity, "收藏成功...");
					break;
				case IOnAlbumListener.HANDLER_FLAG_UNFAVORITE_POST: //取消收藏成功
					int unfavoritedAlbumId = (Integer) msg.obj; 
					AlbumDB.updateFavoriteStatus(activity, unfavoritedAlbumId, 0, -1);//更新db状态
					//更新ui展示
					List<Album> albumList = listViewAdapters[currentTab].getAlbumList();
					if(albumList!=null&&albumList.size()>0){
						for(Album album: albumList){
							if(album.getId()!=null&&album.getId()==unfavoritedAlbumId){
								long favoriteCount = album.getFavoriteCount();
								album.setFavoriteCount(favoriteCount-1);
								album.setFavorite(false);
								break;
							}
						}
						listViewAdapters[currentTab].notifyDataSetChanged();
					}
					//发送广播
					NotificationBuilder.createNotification(activity, "取消收藏成功...");
					break;
				default:
					break;
			}
		};
	};
	
	/**
	 * 按钮监听listener
	 */
	private View.OnClickListener listener = new OnSingleClickListener() {
		@Override
		public void onSingleClick(View view) {
			switch (view.getId()){
			case R.id.btnRefresh://刷新按钮
				switch(currentTab){
					case 0:
					case 1:
					case 2:
						pullRefreshViews[currentTab].setRefreshing(false);
						break;
				}
				break;
			case R.id.btnSettings:
				Activity_Settings.show(activity);
				break;
			default:
				break;
			}
		}
	};
	
	/**
	 * 根据tabIndex生成记录其刷新的sp-key
	 * @param tabIndex
	 * @return
	 */
	private static String getRefreshKey(int tabIndex){
		return ConstantsKey.LAST_REFRESH_TIME_MAIN_PREFIX + tabIndex;
	}
	
	/**
	 * viewPager的listener
	 */
	private OnPageChangeListener viewPagerListener = new OnPageChangeListener(){
		// 新的界面被选中时调用
		@Override
		public void onPageSelected(int index) {
			highLightTab(index);
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
	};
	
}
