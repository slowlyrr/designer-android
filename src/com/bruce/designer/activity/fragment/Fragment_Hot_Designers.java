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
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.bruce.designer.R;
import com.bruce.designer.activity.Activity_Settings;
import com.bruce.designer.adapter.HotDesignerAdapter;
import com.bruce.designer.adapter.ViewPagerAdapter;
import com.bruce.designer.api.AbstractApi;
import com.bruce.designer.api.ApiManager;
import com.bruce.designer.api.hot.HotDesignerListApi;
import com.bruce.designer.constants.ConstantsKey;
import com.bruce.designer.listener.OnSingleClickListener;
import com.bruce.designer.model.User;
import com.bruce.designer.model.result.ApiResult;
import com.bruce.designer.util.SharedPreferenceUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;

public class Fragment_Hot_Designers extends BaseFragment{

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
	
	private GridView[] gridViews = new GridView[TAB_NUM];
	private HotDesignerAdapter[] gridViewAdapters = new HotDesignerAdapter[TAB_NUM];
	
	private Activity activity;
	private LayoutInflater inflater;
	
	private TextView titleView;
	private ImageButton btnRefresh; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity = getActivity();
		this.inflater = inflater;
		
		View mainView = inflater.inflate(R.layout.fragment_hot_designers, null);
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
			View pageView = inflater.inflate(R.layout.designer_view, null);
			gridViews[i] = (GridView) pageView.findViewById(R.id.designerGridView);
			gridViewAdapters[i] = new HotDesignerAdapter(activity, null);
			gridViews[i].setAdapter(gridViewAdapters[i]);
			
			//将views加入viewPager
			pagerViews.add(pageView);
		}
		//viewPager的适配器
		ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(activity, pagerViews);
		viewPager.setAdapter(viewPagerAdapter);
		viewPager.setOnPageChangeListener(viewPagerListener);
		
		//init view
		View titlebarIcon = (View) mainView.findViewById(R.id.titlebar_icon);
		titlebarIcon.setVisibility(View.GONE);
		titleView = (TextView) mainView.findViewById(R.id.titlebar_title);
		titleView.setText("热门设计师");
		
		btnRefresh = (ImageButton) mainView.findViewById(R.id.btnRefresh);
		btnRefresh.setVisibility(View.VISIBLE);
		btnRefresh.setOnClickListener(listener);
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
//		//相应page上请求数据
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
			getHotDesigner(tabIndex);
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		}
	}
	
	
	private void getHotDesigner(final int tabIndex) {
		//启动线程获取数据
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message message;
				int mode = 1;
				AbstractApi api = new HotDesignerListApi(mode);
				
				ApiResult apiResult = ApiManager.invoke(activity, api);
				if(apiResult!=null&&apiResult.getResult()==1){
					message = tabDataHandler.obtainMessage(tabIndex);
					message.obj = apiResult.getData();
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
//					pullRefreshViews[what].onRefreshComplete();
					int tabIndex = what;
					Map<String, Object> tabedDataMap = (Map<String, Object>) msg.obj;
					if(tabedDataMap!=null){
						List<User> designerList = (List<User>) tabedDataMap.get("designerList");
						if(designerList!=null&&designerList.size()>0){
							
							//缓存本次刷新的时间
							SharedPreferenceUtil.putSharePre(activity, getRefreshKey(tabIndex), System.currentTimeMillis());
							gridViewAdapters[tabIndex].setUserList(designerList);
							gridViewAdapters[tabIndex].notifyDataSetChanged();
						}
					}
					break;
				default:
					break;
			}
		};
	};
	
	/**
	 * 根据tabIndex生成记录其刷新的sp-key
	 * @param tabIndex
	 * @return
	 */
	private static String getRefreshKey(int tabIndex){
		return ConstantsKey.LAST_REFRESH_TIME_HOTALBUM_PREFIX+ tabIndex;
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
						getHotDesigner(currentTab);
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
}