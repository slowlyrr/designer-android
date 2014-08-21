package com.bruce.designer.activity.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.bruce.designer.R;
import com.bruce.designer.activity.Activity_Settings;
import com.bruce.designer.adapter.DesignerAlbumsAdapter;
import com.bruce.designer.adapter.ViewPagerAdapter;
import com.bruce.designer.api.AbstractApi;
import com.bruce.designer.api.ApiManager;
import com.bruce.designer.api.hot.HotAlbumListApi;
import com.bruce.designer.constants.ConstantsKey;
import com.bruce.designer.db.album.AlbumDB;
import com.bruce.designer.listener.OnSingleClickListener;
import com.bruce.designer.model.Album;
import com.bruce.designer.model.result.ApiResult;
import com.bruce.designer.util.SharedPreferenceUtil;
import com.bruce.designer.util.TimeUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class Fragment_Hot_Albums extends Fragment{

	private static final int HANDLER_FLAG_TAB0 = 0;
	private static final int HANDLER_FLAG_TAB1 = 1;
	private static final int HANDLER_FLAG_TAB2 = 2;
	
	private static final int HANDLER_FLAG_ERROR = -1;
	
	/* tab个数*/
	private static final int TAB_NUM = 3;
	/* 当前tab*/
	private int currentTab = 0;
	
	private ViewPager viewPager;
	private View[] tabViews = new View[TAB_NUM];
	private View[] tabIndicators = new View[TAB_NUM];
	
	/*下拉控件*/
	private PullToRefreshListView[] pullRefreshViews = new PullToRefreshListView[TAB_NUM];
	private ListView[] listViews = new ListView[TAB_NUM];
	private DesignerAlbumsAdapter[] listViewAdapters = new DesignerAlbumsAdapter[TAB_NUM];
	
	private Activity context;
	private LayoutInflater inflater;
	
	private TextView titleView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = getActivity();
		this.inflater = inflater;
		
		View mainView = inflater.inflate(R.layout.fragment_hot_albums, null);
		initView(mainView);
		return mainView;
	}
	
	private void initView(View mainView) {
		tabIndicators[0] = mainView.findViewById(R.id.tab_weekly_indicator);
		tabIndicators[1] = mainView.findViewById(R.id.tab_monthly_indicator);
		tabIndicators[2] = mainView.findViewById(R.id.tab_yearly_indicator);
		
		tabViews[0] = mainView.findViewById(R.id.tab_weekly_hot);
		tabViews[1] = mainView.findViewById(R.id.tab_monthly_hot);
		tabViews[2] = mainView.findViewById(R.id.tab_yearly_hot);

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
		
		//构造viewPager
		viewPager = (ViewPager) mainView.findViewById(R.id.viewPager);
		List<View> pagerViews = new ArrayList<View>();
		//初始化tab页的view
		for(int i=0;i<TAB_NUM; i++){
			View pageView = inflater.inflate(R.layout.albums_listview, null);
			
			pullRefreshViews[i] = (PullToRefreshListView) pageView.findViewById(R.id.pull_refresh_list);
			pullRefreshViews[i].setMode(Mode.PULL_FROM_START);
			pullRefreshViews[i].setOnRefreshListener(new TabedRefreshListener(i));
			listViews[i] = pullRefreshViews[i].getRefreshableView();
			listViewAdapters[i] = new DesignerAlbumsAdapter(context, null);
			listViews[i].setAdapter(listViewAdapters[i]);
			
			//将views加入viewPager
			pagerViews.add(pageView);
		}
		//viewPager的适配器
		ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(context, pagerViews);
		viewPager.setAdapter(viewPagerAdapter);
		viewPager.setOnPageChangeListener(viewPagerListener);
		
		View titlebarIcon = (View) mainView.findViewById(R.id.titlebar_icon);
		titlebarIcon.setVisibility(View.GONE);
		//init view
		titleView = (TextView) mainView.findViewById(R.id.titlebar_title);
		titleView.setText("热门专辑");
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
		long lastRefreshTime = SharedPreferenceUtil.getSharePreLong(context, tabRefreshKey, 0l);
		long interval = currentTime - lastRefreshTime;
		//相应page上请求数据
		List<Album> albumList = null;
		if(currentTab==1){
			albumList= AlbumDB.queryHotMonthly(context);//月热门
		}else if(currentTab==2){
			albumList= AlbumDB.queryHotYearly(context);//年热门
		}else{
			albumList= AlbumDB.queryHotWeekly(context);//周热门
		}
		if(albumList!=null&&albumList.size()>0){
			listViewAdapters[currentTab].setAlbumList(albumList);
			listViewAdapters[currentTab].notifyDataSetChanged();
			if(interval > TimeUtil.TIME_UNIT_MINUTE){
				pullRefreshViews[currentTab].setRefreshing(false);
			}
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
			getHotAlbums(tabIndex);
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		}
	}
	
	
	private void getHotAlbums(final int tabIndex) {
		//启动线程获取数据
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message message;
				int mode = 1;
				AbstractApi api = new HotAlbumListApi(mode);
				
				ApiResult jsonResult = ApiManager.invoke(context, api);
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
				case HANDLER_FLAG_TAB1:
				case HANDLER_FLAG_TAB2:
					pullRefreshViews[what].onRefreshComplete();
					int tabIndex = what;
					Map<String, Object> tabedDataMap = (Map<String, Object>) msg.obj;
					if(tabedDataMap!=null){
						List<Album> albumList = (List<Album>) tabedDataMap.get("albumList");
						if(albumList!=null&&albumList.size()>0){
							
							AlbumDB.deleteHotByTab(context, tabIndex);
							AlbumDB.saveHotAlbumsByTab(context, albumList, tabIndex);
							
							//缓存本次刷新的时间
							SharedPreferenceUtil.putSharePre(context, getRefreshKey(tabIndex), System.currentTimeMillis());
							listViewAdapters[tabIndex].setAlbumList(albumList);
							listViewAdapters[tabIndex].notifyDataSetChanged();
						}
					}
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
				Activity_Settings.show(context);
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
		return ConstantsKey.LAST_REFRESH_TIME_PREFIX + tabIndex;
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