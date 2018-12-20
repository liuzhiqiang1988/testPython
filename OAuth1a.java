package demo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class OAuth1a {

	public static void main(String[] args) {

		// 正式机
		String url = "http://weibo.leedarson.com/openapi/third/v1/getFile";
		// 测试机
		url = "http://testwb.leedarson.com/openapi/third/v1/getFile";

		// 文件id
		String fileId = "59649517e4b0f9c39754ec37";

		Long timestamp =  System.currentTimeMillis() /1000; // 当前时间戳，时间单位为”秒”，3分钟内有效。
		String appId = "101119"; // 应用appId
		String method = "HMAC-SHA1"; // 即将用于签名的签名算法，目前为HMAC-SHA1。
		String nonce =  UUID.randomUUID().toString().replace("-","").substring(0, 10) ; // 随机字符串
		Float version = 1.0F; // 目前值为1.0。

		
		String signature = ""; // 标准OAuth1.0a签名协议
		String baseUrl = URLEncoder.encode(url) ;

		
		
		
		String params  =  "fileId=" + fileId + 
						  "&oauth_consumer_key=" + appId + 
						  "&oauth_nonce=" + nonce  + 
						  "&oauth_signature_method=" + method + 
						  "&oauth_timestamp=" + timestamp.toString() + 
						  "&oauth_version="	+ version.toString();
		

		
		params = URLEncoder.encode(params);
		String oAuthBaseString = "POST&"+baseUrl+"&"+ params ;
		
		System.out.println(oAuthBaseString);
		
		String AppSecret = "123456";
		String key = URLEncoder.encode(AppSecret) + "&";
		
		signature = genHMAC(oAuthBaseString, key); // 加密后密文
		signature = URLEncoder.encode(signature);
		
		
		
		String authorization = "OAuth oauth_consumer_key=\""+ appId + 
									"\",oauth_signature_method=\"" + method +
									"\",oauth_timestamp=\"" + timestamp.toString() + 
									"\",oauth_nonce=\"" + nonce  + 
									"\",oauth_version=\""+ version.toString() + 
									"\",oauth_signature=\""+ signature + "\"" ;
		System.out.println(authorization);
		
		//请求文件
		String fromData = "fileId="+ fileId ;
		
		
//        get(url+"?"+ fromData,authorization );
        post(url,fromData,authorization);

     //   OAuth oauth_consumer_key="101119",oauth_signature_method="HMAC-SHA1",oauth_timestamp="1528969474",oauth_nonce="2335896239",oauth_version="1.0",oauth_signature="cG8KUQ3fk00006YfW8fAxHMoEW4%3D"

		
		

	}


	

	
	/**
	 * LZQ 2018年6月13日 使用 HMAC-SHA1 签名方法对data进行签名
	 * 
	 * @param data
	 *            被签名的字符串
	 * @param key
	 *            密钥
	 * @return 加密后的字符串
	 */
	public static String genHMAC(String data, String key) {
		String HMAC_SHA1_ALGORITHM = "HmacSHA1";
		byte[] result = null;

		// 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
		SecretKeySpec signinKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		// 生成一个指定 Mac 算法 的 Mac 对象
		Mac mac;
		try {
			mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			// 用给定密钥初始化 Mac 对象
			mac.init(signinKey);
			// 完成 Mac 操作
			byte[] rawHmac = mac.doFinal(data.getBytes());
			result = Base64.encodeBase64(rawHmac);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			System.out.println(e.getMessage());
		}
		if (null != result) {
			return new String(result);
		} else {
			return null;
		}

	}

	
	/**
	 * post 请求
	 * LZQ
	 * 2018年6月14日
	 * @param url
	 * @param fromData
	 * @param head
	 */
	
	public  static void post (String url,String fromData,String head){
		
		HttpClient cli = HttpClients.createDefault();
		HttpPost post = new HttpPost(url);
		post.setHeader("Accept", "application/json");   
		post.setHeader("Content-Type", "application/x-www-form-urlencoded"); 
		post.setHeader("Authorization", head);
		StringEntity entity = null;
		if(null != fromData){
			try {
				
				entity = new StringEntity(fromData);
			} catch (UnsupportedEncodingException e) {
				System.out.println("输入fromData参数异常");
				e.printStackTrace();
			}  
			post.setEntity(entity);
		}
		
		
		try {
			HttpResponse response = cli.execute(post);

			HttpEntity backEntity = response.getEntity();
			String result = EntityUtils.toString(backEntity, "UTF-8");
			System.out.println("POST请求响应类容  -> \n " + result);
			
		} catch (IOException e) {
			System.out.println("请求服务器异常");
			e.printStackTrace();
		}
	}

	/**
	 * get 请求
	 * LZQ
	 * 2018年6月14日
	 * @param url
	 * @param head
	 */
	public static void get (String url,String head){
		HttpGet get = new HttpGet(url);
		HttpClient cli = HttpClients.createDefault();
		get.setHeader("Accept", "application/json");   
		get.setHeader("Content-Type", "application/json"); 
		get.setHeader("Authorization", head);
		try {
			HttpResponse response = cli.execute(get);
			HttpEntity entity = response.getEntity();
			
			
			String result = EntityUtils.toString(entity, "UTF-8");
			System.out.println("GET请求响应类容  ->  " + result);
		} catch (IOException e) {
			System.out.println("请求服务器异常");
			e.printStackTrace();
		}
		
	}
}
