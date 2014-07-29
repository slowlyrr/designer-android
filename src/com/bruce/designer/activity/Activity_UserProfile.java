package com.bruce.designer.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bruce.designer.R;
import com.bruce.designer.adapter.AlbumSlidesAdapter;
import com.bruce.designer.api.ApiManager;
import com.bruce.designer.api.album.AlbumListApi;
import com.bruce.designer.api.user.UserInfoApi;
import com.bruce.designer.constants.ConstantsKey;
import com.bruce.designer.listener.OnSingleClickListener;
import com.bruce.designer.model.Album;
import com.bruce.designer.model.AlbumAuthorInfo;
import com.bruce.designer.model.AlbumSlide;
import com.bruce.designer.model.User;
import com.bruce.designer.model.result.ApiResult;
import com.bruce.designer.util.TimeUtil;
import com.bruce.designer.util.UniversalImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 任何用户的个人资料页面
 * @author liqian
 *
 */
public class Activity_UserProfile extends BaseActivity {
	
	private static final int HANDLER_FLAG_USERINFO = 1;
	private static final int HANDLER_FLAG_SLIDE = 2;
	
	private View titlebarView;

	private TextView titleView;
	/*设计师头像*/
	private ImageView avatarView;
	/*设计师名称*/
	private TextView designerNameView;
	
	private View followsView;
	private View fansView;
	
	private TextView followsNumView;
	private TextView fansNumView;
	
	private int queryUserId;

	private AlbumListAdapter albumListAdapter;

	public AlbumSlidesAdapter slideAdapter;
	
	
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
//							designerNameView.setText(userinfo.getNickname());
							titleView.setText(userinfo.getNickname());
							fansNumView.setText(String.valueOf(fansCount));
							followsNumView.setText(String.valueOf(followsCount));
						}
					}
					break;
				case HANDLER_FLAG_SLIDE:
					Map<String, Object> albumsDataMap = (Map<String, Object>) msg.obj;
					if(albumsDataMap!=null){
						List<Album> albumList = (List<Album>) albumsDataMap.get("albumList");
						if(albumList!=null&&albumList.size()>0){
							albumListAdapter.setAlbumList(albumList);
							albumListAdapter.notifyDataSetChanged();
						}
					}
					break;
				default:
					break;
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_info);
		
		Intent intent = getIntent();
		//获取userid
		queryUserId =  intent.getIntExtra(ConstantsKey.BUNDLE_USER_INFO_ID, 0);
		AlbumAuthorInfo authorInfo = (AlbumAuthorInfo) intent.getSerializableExtra(ConstantsKey.BUNDLE_ALBUM_AUTHOR_INFO);
		
		//init view
		titlebarView = findViewById(R.id.titlebar_return);
		titlebarView.setOnClickListener(listener);
		
		titleView = (TextView) findViewById(R.id.titlebar_title);
		titleView.setText(authorInfo.getDesignerNickname());
		
		avatarView = (ImageView) findViewById(R.id.avatar);
		//显示头像
		ImageLoader.getInstance().displayImage(authorInfo.getDesignerAvatar(), avatarView, UniversalImageUtil.DEFAULT_AVATAR_DISPLAY_OPTION);
		
		designerNameView = (TextView) findViewById(R.id.txtUsername);
		
		fansView = (View) findViewById(R.id.fansContainer);
		fansNumView = (TextView) findViewById(R.id.txtFansNum);
		fansView.setOnClickListener(listener);
		
		followsView = (View) findViewById(R.id.followsContainer);
		followsNumView = (TextView) findViewById(R.id.txtFollowsNum);
		followsView.setOnClickListener(listener);
		
		ListView albumListView = (ListView)findViewById(R.id.designerAlbums);
		albumListAdapter = new AlbumListAdapter(context, null);
		albumListView.setAdapter(albumListAdapter);
		
		//获取个人资料详情
		getUserinfo(queryUserId);
		//获取个人专辑
		getAlbums(queryUserId, 0);
	}
	
	
	class AlbumListAdapter extends BaseAdapter {

		private List<Album> albumList;
		private Context context;
		
		public AlbumListAdapter(Context context, List<Album> albumList) {
			this.context = context;
			this.albumList = albumList;
		}
		
		public void setAlbumList(List<Album> albumList) {
			this.albumList = albumList;
		}

		public List<Album> getAlbumList() {
			return albumList;
		}

		@Override
		public int getCount() {
			if (albumList != null) {
				return albumList.size();
			}
			return 0;
		}

		@Override
		public Album getItem(int position) {
			if (albumList != null) {
				return albumList.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//TODO 暂未使用convertView
			if(getItem(position)!=null){
				final Album album = getItem(position);
				View albumItemView = LayoutInflater.from(context).inflate(R.layout.item_designer_album_view, null);
				
				/*构造slide数据*/
				GridView gridView = (GridView) albumItemView.findViewById(R.id.albumSlideImages);
				List<AlbumSlide> slideList = album.getSlideList();
				slideAdapter = new AlbumSlidesAdapter(context, slideList);
				gridView.setAdapter(slideAdapter);

				TextView usernameView = (TextView) albumItemView.findViewById(R.id.txtUsername);
				usernameView.setText(album.getTitle());
				
				TextView pubtimeView = (TextView) albumItemView.findViewById(R.id.txtTime);
				pubtimeView.setText(TimeUtil.displayTime(album.getCreateTime()));
				
				return albumItemView;
			}
			return null;
		}
	}
	
//	private List<AlbumSlide> buildSlideList(Album album) {
//		List<AlbumSlide> slideList = album.getSlideList();
//		if(slideList!=null){
//			for(AlbumSlide albumSlide: slideList){
//				albumSlide.setSlideSmallImg(album.getCoverSmallImg());
//			}
//		}
//		return slideList;
//	}
	
	private void getUserinfo(final int userId) {
		//启动线程获取数据
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Message message;
				
				UserInfoApi api = new UserInfoApi(userId);
				ApiResult apiResult = ApiManager.invoke(context, api);
				
				
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
				
				AlbumListApi api = new AlbumListApi(queryUserId, albumTailId);
				ApiResult jsonResult = ApiManager.invoke(context, api);
				
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
			case R.id.titlebar_return:
				finish();
				break;
			case R.id.followsContainer:
				Intent followsIntent = new Intent(context, Activity_UserFollows.class);
				followsIntent.putExtra(ConstantsKey.BUNDLE_USER_INFO_ID, queryUserId);
				context.startActivity(followsIntent);
				break;
			case R.id.fansContainer:
				Intent fansIntent = new Intent(context, Activity_UserFans.class);
				fansIntent.putExtra(ConstantsKey.BUNDLE_USER_INFO_ID, queryUserId);
				context.startActivity(fansIntent);
				break;	
			default:
				break;
			}
		}
	};
	
}
