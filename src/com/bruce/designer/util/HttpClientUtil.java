package com.bruce.designer.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class HttpClientUtil {

	private static final int REQUEST_TIMEOUT = 10 * 1000;// 设置请求超时10秒钟
	private static final int SO_TIMEOUT = 10 * 1000; // 设置等待数据超时时间10秒钟

	/**
	 * http get请求
	 * 
	 * @param url
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public static String httpGet(String url, Map<String, String> paramMap)
			throws Exception {
		String queryString = null;
//		StringBuilder sb = null;
		if (paramMap != null && paramMap.size() > 0) {
//			sb = new StringBuilder();
			List<NameValuePair> paramList = new LinkedList<NameValuePair>();
			for (Entry<String, String> entry : paramMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				paramList.add(new BasicNameValuePair(key, value));
			}
			queryString = URLEncodedUtils.format(paramList, HTTP.UTF_8);
		}
		HttpParams httpParameters = buildHttpParam();
		
		if(queryString!=null){
			String concator = "?";//默认连接符
			if(url.indexOf(concator)>0){//url中不含?号
				concator = "&";//url中含?号，连接符改为&
			}
			url = url +concator+ queryString;
		}

		LogUtil.v("========url======" + url);
		HttpGet httpGet = new HttpGet(url);
		HttpClient client = new DefaultHttpClient(httpParameters);
		HttpResponse response = client.execute(httpGet);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			// 请求成功
			String string = EntityUtils.toString(response.getEntity());
			return string;
		}
		return null;
	}

	/**
	 * httpPost 请求
	 * 
	 * @param url
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public static String httpPost(String url, Map<String, String> paramMap)
			throws Exception {
		HttpParams httpParameters = buildHttpParam();

		HttpPost httpPost = new HttpPost(url);
		HttpClient client = new DefaultHttpClient(httpParameters);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (paramMap != null && paramMap.size() > 0) {
			for (Entry<String, String> set : paramMap.entrySet()) {
				String key = set.getKey();
				String value = paramMap.get(set.getKey());
				params.add(new BasicNameValuePair(key, value));
			}
		}

		httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		HttpResponse response = client.execute(httpPost);

		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			// 请求成功
			String string = EntityUtils.toString(response.getEntity());
			return string;
		}
		return null;
	}

	/**
	 * httpPost
	 * 
	 * @param url
	 * @param dataMap
	 * @return
	 * @throws Exception
	 */
	public static String httpPostJson(String url, String jsonStr)
			throws Exception {
		HttpParams httpParameters = buildHttpParam();

		HttpPost httpPost = new HttpPost(url);
		HttpClient client = new DefaultHttpClient(httpParameters);
		httpPost.addHeader("Content-Type", "application/json");
		StringEntity s = new StringEntity(jsonStr, "UTF-8");
		httpPost.setEntity(s);
		HttpResponse response = client.execute(httpPost);

		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			String string = EntityUtils.toString(response.getEntity());
			return string;
		}
		LogUtil.d("----------" + response.getStatusLine().getStatusCode());

		return null;
	}

	
	/**
	 * httpPost方式上传图片
	 * @param url
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static String httpPostImage(String url, Map<String, String> paramMap, String imagePath, byte[] data) throws Exception {
		HttpParams httpParameters = buildHttpParam();
		// 封装请求参数  
		MultipartEntity mpEntity = new MultipartEntity();
		if (paramMap != null && !paramMap.isEmpty()) {
			for (Map.Entry<String, String> entry : paramMap.entrySet()) {
				StringBody par = new StringBody(entry.getValue());
				mpEntity.addPart(entry.getKey(), par);
			}
		}

		// 图片
		File file = new File(imagePath);
		if (file.exists()) {
			FileBody filebody = new FileBody(new File(imagePath));
			mpEntity.addPart("image", filebody);
			// 使用HttpPost对象设置发送的URL路径

			HttpPost post = new HttpPost(url);
			// 发送请求体
			post.setEntity(mpEntity);
			// 创建一个浏览器对象，以把POST对象向服务器发送，并返回响应消息
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpResponse response = httpClient.execute(post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 请求成功
				String string = EntityUtils.toString(response.getEntity());
				LogUtil.d("=========string=========" + string);
				return string;
			}
		}
		return null;
	}
	
	
	/**
	 * 获取输入流
	 * 
	 * @param resourceUrl
	 * @return
	 */
	public static InputStream getNetworkInputStream(String resourceUrl) {
		try {
			URL url = new URL(resourceUrl);
			URLConnection conn = url.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			resourceUrl = null;
			return is;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] getBytesFromUrl(String imageUrl) {
		byte[] bytes = null;
		try {
			LogUtil.v("shareImageUrl", imageUrl);
			InputStream is = getNetworkInputStream(imageUrl);
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int i = 0;
			while ((i = is.read(buffer)) != -1) {
				bos.write(buffer, 0, i);
			}
			bytes = bos.toByteArray();
			is.close();
			bos.close();
			is = null;
			bos = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return bytes;
	}

	/**
	 * 构造httpClient的参数
	 * 
	 * @return
	 */
	private static HttpParams buildHttpParam() {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				REQUEST_TIMEOUT);// 设置请求超时10秒
		HttpConnectionParams.setSoTimeout(httpParameters, SO_TIMEOUT); // 设置等待数据超时10秒
		HttpConnectionParams.setSocketBufferSize(httpParameters, 8192);
		return httpParameters;
	}

	/**
	 * 获取bitmap
	 * 
	 * @param imageUrl
	 * @return
	 */
	public static Bitmap getNetBitmap(String imageUrl) {
		try {
			URL imgURL = new URL(imageUrl);
			URLConnection conn = imgURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			// 下载图片
			Bitmap bmp = BitmapFactory.decodeStream(bis);
			// 关闭Stream
			bis.close();
			is.close();
			imgURL = null;
			return bmp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
