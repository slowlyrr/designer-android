package com.bruce.designer.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.bruce.designer.R;
import com.bruce.designer.adapter.ViewPagerAdapter;
import com.bruce.designer.listener.OnSingleClickListener;
import com.bruce.designer.util.UniversalImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 浏览图片的activity
 * @author liqian
 *
 */
public class Activity_ImageBrowser extends BaseActivity implements OnPageChangeListener{
	
	private ViewPager viewPager;
	
	private View titlebarView;
	private TextView titleView;
	
	private TextView albumSlideIndicatorView, albumSlideTitleView, albumSlideDescView;
	
	private int currentIndex;
	private int totalSize;

	private List<String> slideTitleList;
	private List<String> slideDescList;
	
	private static final String KEY_BROWSE_INDEX = "index";
	private static final String kEY_BROWSE_SLIDE_URL_LIST = "slideUrl";
	private static final String kEY_BROWSE_SLIDE_TITLE_LIST = "slideTitle";
	private static final String kEY_BROWSE_SLIDE_DESC_LIST = "slideDesc";
	
	public static void show(Context context, int index, ArrayList<String> imageUrlList, ArrayList<String> titleList, ArrayList<String> descList){
		Intent intent = new Intent(context, Activity_ImageBrowser.class);
		intent.putExtra(KEY_BROWSE_INDEX, index);
		intent.putStringArrayListExtra(kEY_BROWSE_SLIDE_URL_LIST, imageUrlList);
		intent.putStringArrayListExtra(kEY_BROWSE_SLIDE_TITLE_LIST, titleList);
		intent.putStringArrayListExtra(kEY_BROWSE_SLIDE_DESC_LIST, descList);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_browser);
		
		Intent intent = getIntent();
		currentIndex = intent.getIntExtra(KEY_BROWSE_INDEX, 0);
		List<String> imageUrlList = intent.getStringArrayListExtra(kEY_BROWSE_SLIDE_URL_LIST);
		slideTitleList = intent.getStringArrayListExtra(kEY_BROWSE_SLIDE_TITLE_LIST);
		slideDescList = intent.getStringArrayListExtra(kEY_BROWSE_SLIDE_DESC_LIST);
		
		
		// init view
		titlebarView = findViewById(R.id.titlebar_return);
		titlebarView.setOnClickListener(listener);
		titleView = (TextView) findViewById(R.id.titlebar_title);
		titleView.setText("详情展示");
		
		albumSlideIndicatorView = (TextView) findViewById(R.id.albumSlideIndicator);
		albumSlideIndicatorView.setText((currentIndex+1) + "/" + totalSize);
		
		albumSlideTitleView = (TextView) findViewById(R.id.albumSlideTitle);
		albumSlideTitleView.setText(slideTitleList.get(currentIndex));
		
		albumSlideDescView = (TextView) findViewById(R.id.albumSlideDesc);
		albumSlideDescView.setText(slideDescList.get(currentIndex));
		
		
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		if(imageUrlList!=null&&imageUrlList.size()>0){
			totalSize = imageUrlList.size();
			//非法index视为0
			if(currentIndex<0 || currentIndex>=imageUrlList.size()){
				currentIndex = 0;
			}
			
			List<View> views = new ArrayList<View>();
			for(String imageUrl: imageUrlList){
				View imageViewItem = inflater.inflate(R.layout.pager_image_view, null);
				views.add(imageViewItem);
				ImageView imageView = (ImageView) imageViewItem.findViewById(R.id.pager_imgview);
				ImageLoader.getInstance().displayImage(imageUrl, imageView, UniversalImageUtil.DEFAULT_DISPLAY_OPTION);
			}
			ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this, views);
			viewPager.setAdapter(pagerAdapter);
			viewPager.setOnPageChangeListener(this);
			viewPager.setCurrentItem(currentIndex);
		}
	}

	
	
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		currentIndex = arg0;
		albumSlideIndicatorView.setText((currentIndex+1) + "/" + totalSize);
		if(slideDescList!=null&&slideDescList.size()>currentIndex){
			albumSlideDescView.setText(slideDescList.get(currentIndex));
		}
		if(slideTitleList!=null&&slideTitleList.size()>currentIndex){
			albumSlideTitleView.setText(slideTitleList.get(currentIndex));
		}
	}
	
	
	private OnClickListener listener = new OnSingleClickListener() {
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

	
}
