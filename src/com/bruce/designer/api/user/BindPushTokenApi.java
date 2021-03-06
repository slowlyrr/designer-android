package com.bruce.designer.api.user;

import java.util.Map;
import java.util.TreeMap;

import com.bruce.designer.api.AbstractApi;
import com.bruce.designer.constants.Config;
import com.bruce.designer.model.result.ApiResult;
import com.bruce.designer.util.ResponseBuilderUtil;

public class BindPushTokenApi extends AbstractApi {

	private Map<String, String> paramMap = null;

	@Override
	protected String getApiMethodName() {
		return "bindPushToken.cmd";
	}
	
	/**
	 * 
	 * @param pushChannelId
	 * @param pushUserId
	 * @param mode  1为绑定，0为解绑
	 */
	public BindPushTokenApi(String pushChannelId, String pushUserId, int mode) {
		paramMap = new TreeMap<String, String>();
		paramMap.put("pushChannelId", pushChannelId);
		paramMap.put("pushUserId", pushUserId);
		paramMap.put("osType", String.valueOf(Config.CLIENT_TYPE));
		mode = mode==1?1:0;
		paramMap.put("mode", String.valueOf(mode));//
	}

	@Override
	protected void fillDataMap(Map<String, String> dataMap) {
		if (paramMap != null) {
			dataMap.putAll(paramMap);
		}
	}

	@Override
	protected ApiResult processApiResult(int result, int errorcode, String message, String dataStr) {
		if (result == 1) {
			return ResponseBuilderUtil.buildSuccessResult();
		} else {
			return ResponseBuilderUtil.buildErrorResult(0);
		}
	}

	/**
	 * 此api是否需要登录用户才能操作
	 * @return
	 */
	protected boolean needAuth(){
		return false;
	}
}
