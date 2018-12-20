package com.leedarson.email.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.IOUtils;
import com.leedarson.mobile.mcloud.service.McloudCacheService;

/**
 * 文件预览 服务器处理 LZQ 2018年6月4日
 */
@Service
public class WOPIService {

	@Autowired
	private  McloudCacheService mcloudcacheservice;
	@Autowired
	private  LookEmailService look;
	private static final String KEY = "email:attachment:";

	static Logger log = LogManager.getLogger();
	
	
	
	/**
	 *	通过id获得服务器缓存 redis中的文件信息
	 *
	 * @auhor wangzhenhong
	 * @Create_Date: 2018年6月8日下午3:08:43
	 * @last_modify by wangzhenhong at 2018年6月8日下午3:08:43 <br>
	 * @Why_and_What_is_modified:
	 * @param uid
	 * @return
	 */
	public String wopiEmailGet(String uid){
		String base64Str = mcloudcacheservice.useJedisClient(j -> {
			String res = j.get(KEY + uid);
			return res;
		});
		return base64Str;
	}
	
	/**
	 * 获取文件信息 LZQ 2018年6月4日
	 */
	public JSONObject checkFileInfo(String id) {
		
		 String mailJson=wopiEmailGet(id);
		 JSONObject mail =JSONObject.parseObject(mailJson);
		 JSONObject mails=look.getMailJson(
				 mail.getString("userName"), 
				 mail.getString("password") ,
				 mail.getString("folderName"),
				 Long.valueOf(mail.getString("id")),
				 mail.getString("partName"));
		 JSONObject  fileInfo = new JSONObject();
		 String fileName = mails.getString("name");
		 String OwnerId = "jinijin";
		 int size =Integer.parseInt(mails.getString("size"));
		 fileInfo.put("BaseFileName", fileName);
		 fileInfo.put("Version", "1.12");
		 fileInfo.put("OwnerId", "lzq");
		 fileInfo.put("Size", size);
		return fileInfo;
	}

	/**
	 * 获取文件 LZQ 2018年6月4日
	 * 
	 * @throws IOException
	 */
	public void getFile(String id,  HttpServletResponse response) {

		 String mailJson=wopiEmailGet(id);
		 JSONObject mail =JSONObject.parseObject(mailJson);
		
		 String userName= mail.getString("userName");
	
		 String password=mail.getString("password");
		 String folderName= mail.getString("folderName");
		 Long ids=Long.valueOf(mail.getString("id"));
		 String partName=mail.getString("partName");
		 byte[] mails=look.getMailInput(userName, password ,folderName,ids,partName);
		 response.setCharacterEncoding("utf-8");
		try { 
			BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());

			bos.write(mails);
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
			log.error("预览获取文件异常");
		}

	}

	/**
	 * 利用java原生的摘要实现SHA256加密
	 * 
	 * @param str
	 *            加密后的报文
	 * @return
	 */
	public static String getSHA256StrJava(String str) {
		MessageDigest messageDigest;
		String encodeStr = "";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(str.getBytes("UTF-8"));
			encodeStr = byte2Hex(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return encodeStr;
	}

	/**
	 * 将byte转为16进制
	 * 
	 * @param bytes
	 * @return
	 */
	private static String byte2Hex(byte[] bytes) {
		StringBuffer stringBuffer = new StringBuffer();
		String temp = null;
		for (int i = 0; i < bytes.length; i++) {
			temp = Integer.toHexString(bytes[i] & 0xFF);
			if (temp.length() == 1) {
				// 1得到一位的进行补0操作
				stringBuffer.append("0");
			}
			stringBuffer.append(temp);
		}
		return stringBuffer.toString();
	}

	
	
}
