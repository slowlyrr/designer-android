package com.bruce.designer.api.album;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.bruce.designer.api.AbstractApi;
import com.bruce.designer.model.Comment;
import com.bruce.designer.model.result.ApiResult;
import com.bruce.designer.util.JsonUtil;
import com.bruce.designer.util.ResponseBuilderUtil;
import com.google.gson.reflect.TypeToken;

public class AlbumCommentsApi extends AbstractApi { 

	private Map<String, String> paramMap = null;

	public AlbumCommentsApi(int albumId, long commentsTailId) {
		paramMap = new TreeMap<String, String>();
		paramMap.put("albumId", String.valueOf(albumId));
		paramMap.put("commentsTailId", String.valueOf(commentsTailId));
	}

//	@Override
//	public JsonResultBean processResponse(String response) {
//		JsonResultBean jsonResult = null;
//		if (response != null) {
//			try {
//				JSONObject jsonObject = new JSONObject(response);
//				int result = jsonObject.getInt("result");
//				if (result == 1) {// 成功响应
//					JSONObject jsonData = jsonObject.getJSONObject("data");
//					int resTailId = jsonData.getInt("tailId");
//					String commentListStr = jsonData.getString("commentList");
//					List<Comment> commentList = JsonUtil.gson.fromJson(
//							commentListStr, new TypeToken<List<Comment>>() {
//							}.getType());
//					Map<String, Object> map = new HashMap<String, Object>();
//					map.put("commentTailId", resTailId);
//					map.put("commentList", commentList);
//					jsonResult = new JsonResultBean(result, map, 0, null);
//				} else {// 错误响应
//					int errorcode = jsonObject.getInt("errorcode");
//					String message = jsonObject.getString("message");
//					jsonResult = new JsonResultBean(result, null, errorcode,
//							message);
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//		return jsonResult;
//	}

	@Override
	protected ApiResult processApiResult(int result, int errorcode, String message, String dataStr) {
		if(result==1){
			JSONObject jsonData;
			Map<String, Object> dataMap = new HashMap<String, Object>();
			try {
				jsonData = new JSONObject(dataStr);
				long fromTailId = jsonData.optLong("fromTailId");
				long newTailId = jsonData.optLong("newTailId");
				
				String commentListStr = jsonData.getString("commentList");
				if(commentListStr!=null){
					List<Comment> commentList = JsonUtil.gson.fromJson(commentListStr, new TypeToken<List<Comment>>() {}.getType());
					dataMap.put("commentList", commentList);
					dataMap.put("fromTailId", fromTailId);
					dataMap.put("newTailId", newTailId);
					return ResponseBuilderUtil.buildSuccessResult(dataMap);
				} 
			}catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return ResponseBuilderUtil.buildErrorResult(0);
	}

	@Override
	protected void fillDataMap(Map<String, String> dataMap) {
		if(paramMap!=null){
			dataMap.putAll(paramMap);
		}
	}

	@Override
	protected String getApiMethodName() {
		return "albumComments.cmd";
	}

	
	/**
	 * 此api是否需要登录用户才能操作
	 * @return
	 */
	protected boolean needAuth(){
		return false;
	}
}
