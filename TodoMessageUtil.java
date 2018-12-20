package com.leedarson.email.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 消息推送
 * 
 * @author LZQ 2017年4月18日
 */
public class TodoMessageUtil {

	static Logger log = LogManager.getLogger();
	
	/**
	 * 云之家消息推送 LZQ 2017年5月5日
	 * 
	 * @param massage
	 *            消息内容
	 * @param users
	 *            推送给的用户
	 * @param url
	 *            推送的链接地址
	 * @param title
	 *            消息的提示语
	 * @param type
	 *            发送推送消息的类型 2:纯文本信息 5:文本链接信息 6:图文混排信息
	 * @param code
	 *            推送用户使用的是id或name
	 * @param zip
	 *            内容压缩包二进制字节流，格式为经过BASE64编码的字符串（选填）
	 * @param opentype
	 *            打开方式定义消息的打开方式
	 * @param model 
	 *            图文消息类型 -> 1:单条文本编排模板 2:单条图文混排模板 3:多条图文混排模板 4:应用消息模板
	 *              
	 * @return
	 */
	private static String toMasage(String massage, JSONArray users, String url, String title, String type, String code,
			String zip, String opentype ,Integer model,String imageName) {
		ResourceBundle resource = ResourceBundle.getBundle("mcloud");

		// 读取配置信息
		String eid = resource.getString("eid");
		String pubaccId = resource.getString("pubaccId");
		String pubaccKey = resource.getString("pubaccKey");
		String mserver = resource.getString("mserver");
		String appid = resource.getString("appid");

		JSONObject json = new JSONObject();
		JSONObject from = new JSONObject();
		JSONArray to = new JSONArray();
		JSONObject msg = new JSONObject();

		// from 数据
		from.put("no", eid);
		from.put("pub", pubaccId);
		Long date =  new Date().getTime();
		from.put("time", date);
		Integer random = (int) (Math.random() * 100);
		from.put("nonce", random.toString());
		String pubtoken = sha(new String[] { eid, pubaccId, pubaccKey, random.toString(), date.toString() });
		from.put("pubtoken", pubtoken);
		json.put("from", from);

		// to 数据
		JSONObject toJSon = new JSONObject();
		toJSon.put("no", eid);
		toJSon.put("user", users);// 添加发送用户
		if (StringUtils.isEmpty(code)) {
			toJSon.put("code", "2"); // 使用名称推送 ，不传该参数需要将users改为用户openId
		}
		to.add(toJSon);
		json.put("to", to);

		// 推送 文本
		if (type.equals("2")) {
			// type 数据
			json.put("type", "2");
			// msg 数据
			msg.put("text", massage); // 添加发送消息
			json.put("msg", msg);
		}

		// 推送 文本链接信息
		if (type.equals("5")) {
			// type 数据
			json.put("type", "5");
			// msg 数据
			msg.put("text", massage); // 发送连接消息
			msg.put("url", url);
			msg.put("appid", appid);
			msg.put("todo", 1);
			JSONObject extendFields = new JSONObject();
			extendFields.put("pushTipTitle", title); // 显示标题
			msg.put("sourceid", null);
			msg.put("extendFields", extendFields);
			msg.put("todoPriStatus", "");
			json.put("msg", msg);
		}

		// 推送 图文混排信息
		if (type.equals("6")) {

			// type 数据
			json.put("type", "6");
			msg.put("todo", 0);
			msg.put("appid", "");

			msg.put("model", model);

			JSONObject list = new JSONObject();
			JSONArray listArray = new JSONArray();
			String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

			switch (model) {

			case 1: // 单条文本编排模板

				
				//TODO   未完成
				list.put("date", time);
				list.put("title", title);
				list.put("text", massage);
				list.put("zip", zip); // 内容压缩包二进制字节流，格式为经过BASE64编码的字符串（选填）
				list.put("url", url); // 原文链接，格式为经过URLENCODE编码的字符串,一定要http或者https开头
				list.put("appid", ""); // 如果打开的链接是轻应用,必须传入轻应用号讯通才能传入参数ticket,参考&lt;轻应用框架&gt;开发（选填，这个值和url配合使用，为了拿到ticket）,

				list.put("opentype", opentype); // 打开方式定义消息的打开方式，使用云之家浏览器打开，还是默认浏览器打开。取值为1和0组成的三位数字的字符串，
												// 第一位表示手机端的打开方式，第二位表示桌面端的打开方式，第三位表示web端的打开方式，0：云之家浏览器打开，
												// 1：外部浏览器打开。举例：110
												// 含义：1：手机端外部浏览器打开，1：桌面端外部浏览器打开，0：web端内部浏览器打开。
												// 目前实现了桌面端的打开方式，其他两个开发中，如果该参数不传，使用的是内部的打开方式。（选填）7.0.4+版本支持"
				listArray.add(list);
				msg.put("list", listArray);
				break;

			case 2: // 单条图文混排模板

				list.put("date", time);
				list.put("title", title);
				list.put("text", massage);
//				list.put("zip", zip);
//				list.put("url", url);
				list.put("appid", "");
				list.put("name",imageName); // 图片的文件名(包括后缀)，格式为字符串   "hy.jpg"
				list.put("pic", zip); // 图片的二进制字节流，格式为经过BASE64编码的字符串
				list.put("opentype", opentype);
				listArray.add(list);
				msg.put("list", listArray);
				break;

			case 3:// 多条图文混排模板

				// TODO 需要设计添加多个 listArray.add(list);
				list.put("date", time);
				list.put("title", title);
				list.put("text", massage);
//				list.put("zip", "");
//				list.put("url", url);
				list.put("appid", ""); // 如果打开的链接是轻应用,必须传入轻应用号讯通才能传入参数ticket,参考&lt;轻应用框架&gt;开发（选填，这个值和url配合使用，为了拿到ticket）,
				list.put("name", imageName); // 图片的文件名(包括后缀)，格式为字符串
				list.put("pic", zip); // 图片的二进制字节流，格式为经过BASE64编码的字符串
				list.put("opentype", opentype);
				listArray.add(list);
				
				
				
				list.put("date", "2017-11-24");
				list.put("title", "测试图片2");
				list.put("text", massage);
//				list.put("zip", "");
//				list.put("url", url);
				list.put("appid", ""); // 如果打开的链接是轻应用,必须传入轻应用号讯通才能传入参数ticket,参考&lt;轻应用框架&gt;开发（选填，这个值和url配合使用，为了拿到ticket）,
				list.put("name", "aaaa.jpg"); // 图片的文件名(包括后缀)，格式为字符串
				list.put("pic", zip); // 图片的二进制字节流，格式为经过BASE64编码的字符串
				list.put("opentype", opentype);
				
				
				listArray.add(list);
				
				msg.put("list", listArray);
				break;

			case 4: // 应用消息模板

				// TODO 需要设计添加多个 button.add(buttonList);

				/*
				 * 需要注意的是：button中的event、url两字段是互相对立的，两者不能同时有值；
				 * 当需要点击按钮后直接跳转到某个页面的，则必须保证url有值，而event不能再有任何值！其中，
				 * url必须保持是符合url规范的url！
				 * 当需要点击按钮后只是发送异步请求到指定的接口的，则必须保证event有值，而url不能再有任何值！其中，
				 * event的值的定义遵从以下协议： 首先原值格式为诸如这样
				 * {"appId":"XXX","eventViewId":"XXX","subAppDesc":"XXX","url":
				 * "http://xxx","reqData":{"xxx":"xxx"},"reqType":"XXX",
				 * "contentType":"XXX","autoReply":true,"replyContent":"XXX"}
				 * 格式的json. 在按第一步的格式进行填入相应的值之后把整个json进行utf-8 url编码后对应的值
				 * 
				 * 
				 * 下面对上述提及的event原值json格式中的各项指标进行说明：
				 * 
				 * 项 数据类型 必选 值说明 appId string 是 对应的轻应用appid eventViewId string 是
				 * 该按钮的id subAppDesc string 是 该应用细节的特性，例如签到应用的点赞、点倒等 url string
				 * 是 点击按钮时准备发送请求的接口url reqData json 是
				 * 点击按钮时准备发送请求的接口url所要接收的参数，当接口接收k-v的键值时，便传k-v的json，
				 * 如参数名称为callback，值为test的时候，该值便为{“callback”:“test”}；
				 * 当接口接收body中的一个json时，便把所需入的json直接传入，例如application/json的请求。
				 * reqType string 是
				 * 点击按钮时准备发送请求的接口的请求类型，只限get和post，get和post不限大小写。 contentType
				 * string 是
				 * 点击按钮时准备发送请求的接口的请求的contentType，只限application/x-www-form-
				 * urlencoded和application/json，所写类型值应和请求的contentType保持一致，限小写。
				 * autoReply boolean 否， 默认为false
				 * 点击按钮时发送请求响应后是否需要自动给点击人回复一条消息，默认为不自动回复。 replyContent string 否，
				 * 默认为null 点击按钮时发送请求响应后需要自动给点击人回复一条消息时，必填该值，表示自动回复的文本内容。
				 * 
				 * 温馨提示：请注意！强制要求接收异步请求的接口，即event中url定义的接口，在响应成功的时候返回json格式，并带有”
				 * success”字段，当该字段为true时表明接口处理成功；返回false时接口处理失败，例如：{“success”:
				 * true, …}
				 */

				list.put("date", time);
				list.put("title", title);
				list.put("text", massage);
				JSONArray button = new JSONArray();
				JSONObject buttonList = new JSONObject();
				buttonList.put("id", ""); // 每个button的id，要求该条消息中的每个button都不一样，可以使用UUID
				buttonList.put("title", ""); // 按钮标题，格式为字符串
				buttonList.put("event", ""); // 发送事件的key
				buttonList.put("url", ""); // 发送事件的打开链接，格式为经过URLENCODE编码的字符串
				buttonList.put("appid", ""); // 如果打开的链接是轻应用,必须传入轻应用号讯通才能传入参数ticket,参考<轻应用框架>开发,
				button.add(buttonList);
				listArray.add(list);

				msg.put("list", listArray);
				break;
			}
			json.put("msg", msg);
		}

		String result = sendPost(mserver + "/pubacc/pubsend", json.toJSONString());
		return result;
	}

	/**
	 * 将URL中的参数转成URLEncoder码 LZQ 2017年5月22日
	 * 
	 * @param url
	 * @return
	 */
	public static String UrlDataUtil(String url) {
		int i = StringUtils.indexOf(url, "?");
		if (i == -1) {
			return url;
		} else {
			String data = StringUtils.substring(url, i + 1);
			// if(StringUtils.isEmpty(data)){
			// return url;
			// }
			try {
				data = URLEncoder.encode(data, "utf-8");
			} catch (UnsupportedEncodingException e) {
				log.error("数据编码转换失败" + url);
				e.printStackTrace();
			}
			url = StringUtils.substringBefore(url, "?");
			return url + "?" + data;
		}
	}

	/**
	 * 获取 shaHex 哈希值
	 */
	private static String sha(String... data) {
		Arrays.sort(data);// 在其他语言中按照ASCII顺序排序
		return DigestUtils.shaHex(StringUtils.join(data));// 把数组连接成字符串（无分隔符），并sha1哈希
	}

	/**
	 * 发出POST请求
	 */
	private static String sendPost(String url, String param) {

		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			// out = new PrintWriter(conn.getOutputStream(), "utf-8");
			out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "utf-8"));
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			// in = new BufferedReader(new
			// InputStreamReader(conn.getInputStream()));
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			log.error("发送 POST 请求出现异常！" + e);
			log.error("发送数据" +param);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	
	/**
	 * 发出POST请求
	 */
	// TODO  没写完
	private static String sendPost2(String url, JSONObject param,String encoding) {

		JSONArray data = new JSONArray();
		data.add(param);
		if(StringUtils.isEmpty(encoding)){
			encoding = "UTF_8";
		}
		
		CloseableHttpClient client = HttpClients.createDefault();

		
		HttpPost httpPost = new HttpPost(url);
		
		
		List params=new ArrayList();
		//建立一个NameValuePair数组，用于存储欲传送的参数
		params.addAll(data);
		//添加参数
		
		
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params,encoding));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			
		}
		
		
	



		httpPost.setHeader("Content-type", "application/json");
		httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		
		


//		CloseableHttpResponse response = client.execute(httpPost);
//		HttpEntity stateEntity = response.getEntity();  
//		
//		 if (stateEntity != null) {  
//	            //按指定编码转换结果实体为String类型  
//	          String  body = EntityUtils.toString(stateEntity, encoding);  
//	     }  
//		 EntityUtils.consume(stateEntity); 
//		client.close();
		return "";
	}

	/**
	 * 推送代办消息 通过用户openId LZQ 2017年7月6日
	 * 
	 * @param massage
	 * @param users
	 * @param url
	 * @return
	 */
	public static String pushMassageByOpenid(String massage, JSONArray users, String url) {
		String title = "";
		String type = "5";
		String code = "2";
		String zip = "";
		String opentype = "";
		String blockData = toMasage(massage, users, url, title, type, code, zip, opentype,null,null);
		return blockData;
	}

	/**
	 * 推送文本消息（使用openId） LZQ 2017年7月20日
	 * 
	 * @param massage
	 * @param users
	 * @return
	 */
	public static String pushMassageTextByOpenId(String massage, JSONArray users) {
		String title = "";
		String type = "2";
		String code = "2";
		String url = "";
		String zip = "";
		String opentype = "";
		String blockData = toMasage(massage, users, url, title, type, code, zip, opentype,null,null);
		return blockData;
	}

	/**
	 * 推送文本消息（使用name用户名） LZQ 2017年7月20日
	 * 
	 * @param massage
	 * @param users
	 * @return
	 */
	public static String pushMassageTextByName(String massage, JSONArray users) {
		String title = "";
		String type = "2";
		String url = "";
		String zip = "";
		String opentype = "";

		String blockData = toMasage(massage, users, url, title, type, null, zip, opentype,null,null);
		return blockData;
	}

	/**
	 * 推送连接消息（使用name用户名） LZQ 2017年7月20日
	 * 
	 * @param massage
	 * @param users
	 * @param url
	 * @return
	 */
	public static String pushMassageLinkByName(String massage, JSONArray users, String url) {
		String title = "";
		String type = "5";
		String zip = "";
		String opentype = "";
		String blockData = toMasage(massage, users, url, title, type, null, zip, opentype,null,null);
		return blockData;
	}

	/**
	 * 推送连接消息（使用openId） LZQ 2017年7月20日
	 * 
	 * @param massage
	 * @param users
	 * @param url
	 * @return
	 */
	public static String pushMassageLinkByOpenId(String massage, JSONArray users, String url) {
		String title = "";
		String type = "5";
		String code = "2";
		String zip = "";
		String opentype = "";
		String blockData = toMasage(massage, users, url, title, type, code, zip, opentype,null,null);
		return blockData;
	}

	/**
	 * 推送图片消息 LZQ 2017年11月22日 通过名称
	 * (单条图文混排模板)
	 * @param massage
	 * @param users
	 * @param url
	 * @return
	 */
	public static String pushImageAndTextByName(String massage, String title, JSONArray users, String zip,String imageName) {
		String type = "6";
		String url = null;
		int model = 2 ;  //1:单条文本编排模板 2:单条图文混排模板 3:多条图文混排模板 4:应用消息模板
		String opentype = "010";
		String blockData = toMasage(massage, users, url, title, type, null, zip, opentype,model,imageName);
		return blockData;
	}
	
	
	public static String pushTextByName(String massage, String title, JSONArray users, String zip,String imageName) {
		String type = "6";
		String url = null;
		int model = 1 ;  //1:单条文本编排模板 2:单条图文混排模板 3:多条图文混排模板 4:应用消息模板
		String opentype = "010";
		String blockData = toMasage(massage, users, url, title, type, null, zip, opentype,model,imageName);
		return blockData;
	}
	

	/**
	 * 推送图片消息 LZQ 2017年11月22日 通过名称
	 * (多条图文混排模板)
	 * @param massage
	 * @param users
	 * @param url
	 * @return
	 */
	public static String pushImageAndTextArrayByName(String massage, String title, JSONArray users, String zip,String imageName) {
		
		
		String type = "6";
		String url = null;
		int model = 3 ;  //1:单条文本编排模板 2:单条图文混排模板 3:多条图文混排模板 4:应用消息模板
		String opentype = "010";
		
		//TODO   修改数据格式(需要支持多列)
		JSONArray type6Model3 = new JSONArray() ;
		
		
		
		String blockData = toMasage(massage, users, url, title, type, null, zip, opentype,model,imageName);
		
		return blockData;
	}
	
	
	
	//测试文本信息
//	 public static void main(String[] args) {
//	 String massage = "测试推送连接到行信息(图片)";
//	 JSONArray users = new JSONArray();
//	 String url ="http://172.16.20.96:8020/test/image.html?__hbt=1511333540036";
//	 users.add("wuy"); // 苏宁军：d112fbe5-cbf9-11e6-9462-005056a73c15 吴银：wuy
//	 pushMassageLinkToName(massage, users, url);
//	 // String url = "http://www.kjson.com";
//	 // String title = "测试类型6消息推送";
//	 // String type = "6";
//	
//	 }

	// 测试图片消息
//	public static void main(String[] args) {
//
//		String img = "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" ;
//		String massage = "测试推图片信息";
////		String url = "www.baidu.com";
//		JSONArray users = new JSONArray();
//		users.add("wuy");// TODO
//		String title = "测试发送图片信息!";
//		String imageName = "hk.jpg" ;
//		pushImageAndTextArrayByName(massage, title, users, img, imageName);
//	}
}
