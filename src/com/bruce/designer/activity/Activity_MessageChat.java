package com.bruce.designer.activity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.bruce.designer.AppApplication;
import com.bruce.designer.R;
import com.bruce.designer.api.ApiManager;
import com.bruce.designer.api.message.MessageListApi;
import com.bruce.designer.api.message.PostChatApi;
import com.bruce.designer.broadcast.NotificationBuilder;
import com.bruce.designer.constants.ConstantsKey;
import com.bruce.designer.constants.ConstantsStatEvent;
import com.bruce.designer.handler.DesignerHandler;
import com.bruce.designer.listener.OnSingleClickListener;
import com.bruce.designer.model.Message;
import com.bruce.designer.model.User;
import com.bruce.designer.model.result.ApiResult;
import com.bruce.designer.util.DesignerUtil;
import com.bruce.designer.util.DipUtil;
import com.bruce.designer.util.StringUtils;
import com.bruce.designer.util.TimeUtil;
import com.bruce.designer.util.UiUtil;
import com.bruce.designer.util.UniversalImageUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 私信消息对话页面
 * @author liqian
 *
 */
@SuppressLint("NewApi")
public class Activity_MessageChat extends BaseActivity implements OnRefreshListener2<ListView>{
	
	private static final int HANDLER_FLAG_CHAT_LIST_RESULT = 1;

	protected static final int HANDLER_FLAG_CHAT_POST_RESULT = 11;
	
	private static final int HOST_ID = AppApplication.getUserPassport().getUserId();
	
	
	private View titlebarView;
	private TextView titleView;
	
	private ListView messageListView;
	private MessageListAdapter messageListAdapter;
	private PullToRefreshListView pullRefreshView;

	private int messageType;
	private String nickname;
	private String avatarUrl;

	private EditText messageInput;
	
	private long messageTailId;
	
	private Handler handler;
	private OnClickListener onClickListener;
	
	/**
	 * Chat消息的messageType 实为对方的userId
	 * @param context
	 * @param messageType
	 * @param nickname 
	 */
	public static void show(Context context, int messageType, String nickname, String avatarUrl){
		if(!AppApplication.isGuest()){//游客身份不能发送私信
			if(messageType!=AppApplication.getUserPassport().getUserId()){//不能跟自己进行私信
				Intent intent = new Intent(context, Activity_MessageChat.class);
				intent.putExtra("messageType", messageType);
				intent.putExtra("nickname", nickname);
				intent.putExtra("avatarUrl", avatarUrl);
				context.startActivity(intent);
			}
		}else{
//			UiUtil.showShortToast(context, "游客身份无法发送私信，请先登录");
			DesignerUtil.guideGuestLogin(context, "提示", "游客身份无法发送私信，请先登录");
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msg_chat);
		handler = initHandler();
		onClickListener = initListener();
		
		Intent intent = getIntent();
		//获取messageType
		messageType =  intent.getIntExtra("messageType", 0); 
		nickname =  intent.getStringExtra("nickname");
		avatarUrl =  intent.getStringExtra("avatarUrl");
		
		//进入私信界面后，发送状态变更的广播
		broadcastMessageStatusChanged();//更新db后发送广播
		
		//init view
		titlebarView = findViewById(R.id.titlebar_return);
		titlebarView.setOnClickListener(onClickListener);
		titleView = (TextView) findViewById(R.id.titlebar_title);
		titleView.setText(nickname);
		
		View commentPanel = (View) findViewById(R.id.commentPanel);
		commentPanel.setVisibility(View.VISIBLE);
		
		//评论框
		messageInput = (EditText) findViewById(R.id.commentInput);
		Button btnCommentPost = (Button) findViewById(R.id.btnCommentPost);
		btnCommentPost.setOnClickListener(onClickListener);
		
		pullRefreshView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		pullRefreshView.setMode(Mode.PULL_FROM_END);//在聊天界面，上拉刷新为获取最新数据
		pullRefreshView.setOnRefreshListener(this);
		
		messageListView = pullRefreshView.getRefreshableView();// (ListView)findViewById(R.id.msgDialog);
		messageListAdapter = new MessageListAdapter(context, null);
		messageListView.setAdapter(messageListAdapter);
		
		//获取消息列表
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
			//TODO 暂未使用convertView
			Message message = getItem(position);
			ChatViewHandler viewHolder = null;
			if(convertView==null){
				viewHolder = new ChatViewHandler();
				if(message!=null){
					convertView = inflater.inflate(R.layout.item_msgchat_view, null);
//					RelativeLayout messageContainer = (RelativeLayout) convertView.findViewById(R.id.messageContainer);
//					RelativeLayout myMessageContainer = (RelativeLayout) convertView.findViewById(R.id.myMessageContainer);
					
					viewHolder.messageContainer = (RelativeLayout) convertView.findViewById(R.id.messageContainer);
					viewHolder.myMessageContainer = (RelativeLayout) convertView.findViewById(R.id.myMessageContainer);
					
					viewHolder.chatTimetView = (TextView) convertView.findViewById(R.id.chatTime);
					viewHolder.msgContentView = (TextView) convertView.findViewById(R.id.msgContent);
					viewHolder.msgAvatarView = (ImageView) convertView.findViewById(R.id.msgAvatar);
					//我的
					viewHolder.myChatTimetView = (TextView) convertView.findViewById(R.id.myChatTime);
					viewHolder.myMsgContentView = (TextView) convertView.findViewById(R.id.myMsgContent);
					viewHolder.myMsgAvatarView = (ImageView) convertView.findViewById(R.id.myMsgAvatar);
					convertView.setTag(viewHolder);
				}
			}else{
				viewHolder = (ChatViewHandler) convertView.getTag();
			}
			
			//构造view
			if(message.getFromId()!= HOST_ID){//需要展示对方的对话消息
				viewHolder.messageContainer.setVisibility(View.VISIBLE);
				viewHolder.myMessageContainer.setVisibility(View.GONE);
				
				viewHolder.chatTimetView.setText(TimeUtil.displayTime(message.getCreateTime()));
				
				GradientDrawable timeTextDrawable = new GradientDrawable();
				timeTextDrawable.setColor(getResources().getColor(R.color.grey_button_normal_color));
				float timeRadius = DipUtil.calcFromDip((Activity) context, 3);
				timeTextDrawable.setCornerRadius(timeRadius);
				viewHolder.chatTimetView.setBackground(timeTextDrawable);
				
				viewHolder.msgContentView.setText(message.getMessage());
				
				GradientDrawable chatTextDrawable = new GradientDrawable();
				chatTextDrawable.setColor(getResources().getColor(R.color.grey_button_active_color));
				float radius = DipUtil.calcFromDip((Activity) context, 6);
				chatTextDrawable.setCornerRadius(radius);
				
				viewHolder.msgContentView.setBackground(timeTextDrawable);
				
				//私信消息需要使用fromUser的头像
				ImageLoader.getInstance().displayImage(avatarUrl, viewHolder.msgAvatarView, UniversalImageUtil.DEFAULT_AVATAR_DISPLAY_OPTION);
			}else{//需要展示自己的对话消息
				User hostUser = AppApplication.getHostUser();
				
				viewHolder.messageContainer.setVisibility(View.GONE);
				viewHolder.myMessageContainer.setVisibility(View.VISIBLE);
				
				viewHolder.myChatTimetView.setText(TimeUtil.displayTime(message.getCreateTime()));
				
				GradientDrawable timeTextDrawable = new GradientDrawable();
				timeTextDrawable.setColor(getResources().getColor(R.color.grey_button_normal_color));
				float timeRadius = DipUtil.calcFromDip((Activity) context, 3);
				timeTextDrawable.setCornerRadius(timeRadius);
				viewHolder.myChatTimetView.setBackground(timeTextDrawable);
				
				viewHolder.myMsgContentView.setText(message.getMessage());
				
				GradientDrawable chatTextDrawable = new GradientDrawable();
				chatTextDrawable.setColor(getResources().getColor(R.color.green_button_normal_color));
				float radius = DipUtil.calcFromDip((Activity) context, 6);
				chatTextDrawable.setCornerRadius(radius);
				viewHolder.myMsgContentView.setBackground(chatTextDrawable);
				
				//我的头像
				ImageLoader.getInstance().displayImage(hostUser!=null?hostUser.getHeadImg():"", viewHolder.myMsgAvatarView, UniversalImageUtil.DEFAULT_AVATAR_DISPLAY_OPTION);
			
			}
			return convertView;
		}
	}
	
	/**
	 * 获取关注列表
	 */
	private void getMessageList(final long messageTailId) {
		//启动线程获取数据
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				android.os.Message message;
				MessageListApi api = new MessageListApi(messageType, messageTailId); 
				ApiResult apiResult = ApiManager.invoke(context, api);
				message = handler.obtainMessage(HANDLER_FLAG_CHAT_LIST_RESULT);
				message.obj = apiResult;
				message.sendToTarget();
				
//				if(jsonResult!=null&&jsonResult.getResult()==1){
//					message = handler.obtainMessage(HANDLER_FLAG_CHAT_LIST_RESULT);
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
			
			switch(msg.what){
				case HANDLER_FLAG_CHAT_LIST_RESULT:
					pullRefreshView.onRefreshComplete();
					if(successResult){
						Map<String, Object> messagesDataMap = (Map<String, Object>) apiResult.getData();
						if(messagesDataMap!=null){
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
									pullRefreshView.setMode(Mode.PULL_FROM_END);//禁用下拉刷新查询历史消息 
								}
								
								Collections.reverse(messageList);//聊天界面需要反转list，保证最新消息在最下方
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
								if(!fallloadAppend){
									//非加载更多的场景，需要直接滚动到底部
									messageListView.setSelection(messageListView.getBottom());
								}
							}
						}
					}else{
						UiUtil.showShortToast(context, "获取私信数据失败，请重试");
					}
					break;
				case HANDLER_FLAG_CHAT_POST_RESULT:
					if(successResult){
						NotificationBuilder.createNotification(context, "私信发送成功...");
						messageInput.setText("");//清空评论框内容
						//隐藏软键盘
						InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(messageInput.getWindowToken(), 0);
						//重新加载评论列表
						getMessageList(0);
					}else{
						NotificationBuilder.createNotification(context, "私信发送失败...");
					}
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}
	
	/**
	 * 下拉刷新
	 */
	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		getMessageList(messageTailId);
	}
	
	/**
	 * 上拉刷新
	 */
	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		getMessageList(0);
	}

	private OnClickListener initListener(){
		OnClickListener onclickListener = new OnSingleClickListener() {
			@Override
			public void onSingleClick(View view) {
				switch (view.getId()) {
				case R.id.titlebar_return:
					finish();
					break;
				case R.id.btnCommentPost:
					StatService.onEvent(context, ConstantsStatEvent.EVENT_SEND_CHAT, "私信页中发送私信");
					
					
					String chatContent = messageInput.getText().toString();
					//检查内容不为空
					if(StringUtils.isBlank(chatContent)){
						UiUtil.showShortToast(context, "消息内容不能为空");
						break;
					}
					//启动线程发布回复消息
					postChat(messageType, chatContent);
					break;
				default:
					break;
				}
			}
		};
		return onclickListener;
	}	
	
	
	/**
	 * 回复聊天消息
	 */
	private void postChat(final int toId, final String content) {
		//启动线程获取数据
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				android.os.Message message;
				PostChatApi api = new PostChatApi(toId, content);
				ApiResult apiResult = ApiManager.invoke(context, api);
				message = handler.obtainMessage(HANDLER_FLAG_CHAT_POST_RESULT);
				message.obj = apiResult;
				message.sendToTarget();
				
//				if(jsonResult!=null&&jsonResult.getResult()==1){
//					message = handler.obtainMessage(HANDLER_FLAG_CHAT_POST_RESULT);
//					message.obj = jsonResult.getData();
//					message.sendToTarget();
//				}
			}
		});
		thread.start();
	}
	
	/**
	 * viewHolder
	 * @author liqian
	 *
	 */
	static class ChatViewHandler {

		public View messageContainer;
		public View myMessageContainer;
		//对方的
		public TextView chatTimetView;
		public TextView msgContentView;
		public ImageView msgAvatarView;
		//我的
		public TextView myChatTimetView;
		public TextView myMsgContentView;
		public ImageView myMsgAvatarView;
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
