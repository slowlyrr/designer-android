package com.bruce.designer.activity;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.bruce.designer.R;
import com.bruce.designer.api.ApiManager;
import com.bruce.designer.api.message.MessageListApi;
import com.bruce.designer.constants.Config;
import com.bruce.designer.constants.ConstantDesigner;
import com.bruce.designer.constants.ConstantsKey;
import com.bruce.designer.constants.ConstantsStatEvent;
import com.bruce.designer.handler.DesignerHandler;
import com.bruce.designer.listener.OnSingleClickListener;
import com.bruce.designer.model.Message;
import com.bruce.designer.model.result.ApiResult;
import com.bruce.designer.util.MessageUtil;
import com.bruce.designer.util.TimeUtil;
import com.bruce.designer.util.UiUtil;
import com.bruce.designer.util.UniversalImageUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 普通消息界面
 * 
 * @author liqian
 */
public class Activity_MessageList extends BaseActivity implements OnRefreshListener2<ListView>{

	private static final int HANDLER_FLAG_MESSAGELIST_RESULT = 1;
	
	private View titlebarView;
	private TextView titleView;

	private MessageListAdapter messageListAdapter;
	private PullToRefreshListView pullRefreshView;
	private ListView messageListView;
	
	private int messageType;
	
	private long messageTailId;
	private Handler handler;
	private OnClickListener onClickListener;

	public static void show(Context context, int messageType) {
		Intent intent = new Intent(context, Activity_MessageList.class);
		intent.putExtra("messageType", messageType);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msg_list);
		handler = initHandler();
		onClickListener = initListener();
		

		Intent intent = getIntent();
		// 获取messageType
		messageType = intent.getIntExtra("messageType", 0);

		// init view
		titlebarView = findViewById(R.id.titlebar_return);
		titlebarView.setOnClickListener(onClickListener);
		titleView = (TextView) findViewById(R.id.titlebar_title);
		titleView.setText(MessageUtil.buildMessageTitle(messageType, null));
		
		pullRefreshView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		pullRefreshView.setMode(Mode.PULL_FROM_START);
		pullRefreshView.setOnRefreshListener(this);
		
		messageListView = pullRefreshView.getRefreshableView();
		messageListAdapter = new MessageListAdapter(context, null);
		messageListView.setAdapter(messageListAdapter);

		// 获取消息列表
		getMessageList(0);
	}

	class MessageListAdapter extends BaseAdapter {

		private List<Message> messageList;
		private Context context;

		public MessageListAdapter(Context context, List<Message> messageList) {
			this.context = context;
			this.messageList = messageList;
		}

		public List<Message> getMessageList() {
			return messageList;
		}

		public void setMessageList(List<Message> messageList) {
			this.messageList = messageList;
		}

		@Override
		public int getCount() {
			if (messageList != null) {
				return messageList.size();
			}
			return 0;
		}

		@Override
		public Message getItem(int position) {
			if (messageList != null) {
				return messageList.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Message message = getItem(position);
			//TODO 使用convertView
			MessageViewHandler viewHolder = null;
			if(convertView==null){
				viewHolder = new MessageViewHandler();
				if(message!=null){
					convertView = inflater.inflate(R.layout.item_msgbox_view, null);
					viewHolder.messageItemView = convertView;
					viewHolder.unreadNumContainer = (View) convertView.findViewById(R.id.unreadNumContainer);
					viewHolder.unreadNumText = (TextView) convertView.findViewById(R.id.unreadNum);
					viewHolder.msgTitleView = (TextView) convertView.findViewById(R.id.msgTitle);
					viewHolder.msgContentView = (TextView) convertView.findViewById(R.id.msgContent);
					viewHolder.msgPubTimeView = (TextView) convertView.findViewById(R.id.msgPubTime);
					viewHolder.msgAvatrView = (ImageView) convertView.findViewById(R.id.msgAvatar);
					convertView.setTag(viewHolder);
				}
			}else{
				viewHolder = (MessageViewHandler) convertView.getTag();
			}

			//填充数据
			
			
			//加载头像
			if(MessageUtil.isBroadcastMessage(messageType)){//系统消息使用系统头像
				viewHolder.msgAvatrView.setImageResource(R.drawable.icon_msgbox_sys);
			}else{//其他消息需要使用fromUser的头像
				ImageLoader.getInstance().displayImage(message.getFromUser().getHeadImg(), viewHolder.msgAvatrView, UniversalImageUtil.DEFAULT_AVATAR_DISPLAY_OPTION);
				// 头像点击事件
				viewHolder.msgAvatrView.setOnClickListener(new OnSingleClickListener() {
					@Override
					public void onSingleClick(View v) {
						StatService.onEvent(context, ConstantsStatEvent.EVENT_VIEW_HOME, "消息列表页中查看个人主页");
						// 跳转个人主页
						Activity_UserHome.show(context, message.getFromId(), message.getFromUser().getNickname(), message.getFromUser().getHeadImg(), false, false);
					}
				});
			}
			
			//未读消息数
			int unreadNum = message.getUnread();
			unreadNum = unreadNum>99?99:unreadNum;//最多显示99条
			viewHolder.unreadNumText.setText(String.valueOf(unreadNum));
			if(unreadNum>0){
				viewHolder.unreadNumContainer.setVisibility(View.VISIBLE);
			}else{
				viewHolder.unreadNumContainer.setVisibility(View.GONE);
			}
			//消息内容
			int messageType = message.getMessageType();
			if(MessageUtil.isBroadcastMessage(messageType)){
				viewHolder.msgTitleView.setText("系统消息");
			}else{
				viewHolder.msgTitleView.setText(message.getFromUser().getNickname());
			}
			viewHolder.msgContentView.setText(message.getMessage());
			viewHolder.msgPubTimeView.setText(TimeUtil.displayTime(message.getCreateTime()));
			
			OnClickListener itemClickListener = null;
			if(MessageUtil.isInteractiveMessage(messageType)){//交互类消息，点击后跳到专辑的界面
				itemClickListener = new OnSingleClickListener() {
					@Override
					public void onSingleClick(View v) {
						broadcastMessageStatusChanged();//更新db后发送广播
						Long sourceId =  message.getSourceId();
						//交互消息类型(评论、收藏、赞)跳转到作品页面
						if(sourceId!=null&&sourceId>0){
							Activity_AlbumInfo.show(context, sourceId.intValue());
						}
					}
				};
			}
			
			if(messageType == ConstantDesigner.MESSAGE_TYPE_FOLLOW){//关注消息
				itemClickListener = new OnSingleClickListener() {
					@Override
					public void onSingleClick(View v) {
						Activity_UserHome.show(context, message.getFromId(), message.getFromUser().getNickname(), message.getFromUser().getHeadImg(), false, false);
					}
				};
			}
			
			if(itemClickListener!=null){
				viewHolder.messageItemView.setOnClickListener(itemClickListener);
			}
			
			return convertView;
		}
	}

	/**
	 * 获取消息列表
	 */
	private void getMessageList(final long messageTailId) {
		// 启动线程获取数据
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				android.os.Message message;
				MessageListApi api = new MessageListApi(messageType, messageTailId);
				ApiResult apiResult = ApiManager.invoke(context, api);
				message = handler.obtainMessage(HANDLER_FLAG_MESSAGELIST_RESULT);
				message.obj = apiResult;
				message.sendToTarget();
				
//				if (jsonResult != null && jsonResult.getResult() == 1) {
//					message = handler.obtainMessage(HANDLER_FLAG_MESSAGELIST_RESULT);
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
			public void processHandlerMessage(android.os.Message msg) {
				ApiResult apiResult = (ApiResult) msg.obj;
				boolean successResult = (apiResult!=null&&apiResult.getResult()==1);
				
				switch (msg.what) {
				case HANDLER_FLAG_MESSAGELIST_RESULT:
					pullRefreshView.onRefreshComplete();
					if(successResult){
						Map<String, Object> messagesDataMap = (Map<String, Object>) apiResult.getData();
						if (messagesDataMap != null) {
							//解析响应数据
							Long fromTailId = (Long) messagesDataMap.get("fromTailId");
							Long newTailId = (Long) messagesDataMap.get("newTailId");
							
							List<Message> messageList = (List<Message>)  messagesDataMap.get("messageList");
							if(messageList!=null&&messageList.size()>0){
								if(newTailId!=null&&newTailId>0){//还有可加载的数据
									messageTailId = newTailId;
									pullRefreshView.setMode(Mode.BOTH);
								}else{
									messageTailId = 0;
									pullRefreshView.setMode(Mode.PULL_FROM_START);//禁用下拉刷新查询历史消息 
								}
								List<Message> oldMessageList = messageListAdapter.getMessageList();
								//判断加载位置，以确定是list增量还是覆盖
								boolean fallloadAppend = fromTailId!=null&&fromTailId>0;
								if(fallloadAppend){//加载更多操作，需添加至list的开始
									oldMessageList.addAll(0, messageList);
								}else{//下拉加载，需覆盖原数据
									oldMessageList = null;
									oldMessageList = messageList;
								}
								messageListAdapter.setMessageList(oldMessageList);
								messageListAdapter.notifyDataSetChanged();
							}
						}
					}else{
						//UiUtil.showShortToast(context, "获取我的消息失败，请重试");
						UiUtil.showShortToast(context, Config.RESPONSE_ERROR);
					}
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
		getMessageList(0);
	}
	
	/**
	 * 上拉刷新
	 */
	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		getMessageList(messageTailId);
	}
	
	/**
	 * viewHolder
	 * @author liqian
	 *
	 */
	static class MessageViewHandler {

		public View messageItemView;

		public View unreadNumContainer;
		public TextView unreadNumText;

		public TextView msgTitleView;
		public TextView msgContentView;
		public TextView msgPubTimeView;
		// 头像
		public ImageView msgAvatrView;
	}
	
	/**
	 * 广播消息状态被改变
	 * @param messageId
	 */
	private void broadcastMessageStatusChanged() {
		//发送album被变更的广播
		Intent intent = new Intent(ConstantsKey.BroadcastActionEnum.MESSAGE_STATUS_CHANGED.getAction());
		intent.putExtra(ConstantsKey.BUNDLE_BROADCAST_KEY, ConstantsKey.BROADCAST_MESSAGE_READ_CHANGED);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
}
