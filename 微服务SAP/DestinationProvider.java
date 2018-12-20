package com.leedarson.sap.sapClient;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;

public class DestinationProvider {

	private static final String ABAP_AS_POOLED = "ABAP_AS_WITH_POOL";
	private static Logger log = LoggerFactory.getLogger(DestinationProvider.class);
	private static boolean isCreat = false;
	private static final String destName = "SAP_AS";

	static String JCO_MSHOST;
	static String JCO_GROUP;
	static String JCO_R3NAME;
	static String JCO_CLIENT;
	static String JCO_ASHOST;
	static String JCO_SYSNR;
	static String JCO_USER;
	static String JCO_PASSWD;
	static String JCO_LANG;
	static String JCO_POOL_CAPACITY;
	static String JCO_PEAK_LIMIT;
	static boolean GROUP = false;

	public static void setJCO_MSHOST(String jCO_MSHOST) {
		JCO_MSHOST = jCO_MSHOST;
	}

	public static void setJCO_GROUP(String jCO_GROUP) {
		JCO_GROUP = jCO_GROUP;
	}

	public static void setJCO_R3NAME(String jCO_R3NAME) {
		JCO_R3NAME = jCO_R3NAME;
	}

	public static void setJCO_CLIENT(String jCO_CLIENT) {
		JCO_CLIENT = jCO_CLIENT;
	}

	public static void setJCO_ASHOST(String jCO_ASHOST) {
		JCO_ASHOST = jCO_ASHOST;
	}

	public static void setJCO_SYSNR(String jCO_SYSNR) {
		JCO_SYSNR = jCO_SYSNR;
	}

	public static void setJCO_USER(String jCO_USER) {
		JCO_USER = jCO_USER;
	}

	public static void setJCO_PASSWD(String jCO_PASSWD) {
		JCO_PASSWD = jCO_PASSWD;
	}

	public static void setJCO_LANG(String jCO_LANG) {
		JCO_LANG = jCO_LANG;
	}

	public static void setJCO_POOL_CAPACITY(String jCO_POOL_CAPACITY) {
		JCO_POOL_CAPACITY = jCO_POOL_CAPACITY;
	}

	public static void setJCO_PEAK_LIMIT(String jCO_PEAK_LIMIT) {
		JCO_PEAK_LIMIT = jCO_PEAK_LIMIT;
	}

	public static void setGROUP(boolean gROUP) {
		GROUP = gROUP;
	}

	private static Properties setProperties() {
		// logon parameters and other properties
		Properties connectProperties = new Properties();
		if (GROUP) { // 正式机
			connectProperties.setProperty(DestinationDataProvider.JCO_MSHOST, JCO_MSHOST); // 服务器
			connectProperties.setProperty(DestinationDataProvider.JCO_GROUP, JCO_GROUP); // 集群
			connectProperties.setProperty(DestinationDataProvider.JCO_R3NAME, JCO_R3NAME); //
			connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, JCO_CLIENT); // SAP集团
		} else {// 测试机
			connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, JCO_ASHOST); // 服务器
			connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, JCO_SYSNR); // 系统编号
			connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, JCO_CLIENT); // SAP集团
		}
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, JCO_USER); // SAP用户名
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, JCO_PASSWD); // 密码
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, JCO_LANG); // 登录语言
		connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, JCO_POOL_CAPACITY); // 最大连接数
		connectProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, JCO_PEAK_LIMIT); // 最大连接线程
		return connectProperties;
	}

	/**
	 * 获取连接 <br>
	 * 2018年9月12日 <br>
	 * liuzhiqiang
	 * 
	 * @return
	 */
	public static JCoDestination connect() {
		if (!isCreat) {
			setConfig();
		}
		JCoDestination dest = null;
		try {
			dest = JCoDestinationManager.getDestination(destName);
		} catch (JCoException e) {
			log.error("Connect SAP fault, error msg: " + e.toString());
		}
		return dest;
	}

	/**
	 * 初始化SAP连接 <br>
	 * 2018年9月12日 <br>
	 * liuzhiqiang
	 */
	private static void setConfig() {
		Properties props = setProperties();
		DestinationDataProviderImp destDataProvider = new DestinationDataProviderImp();
		destDataProvider.addDestinationProperties(destName, props);
		Environment.registerDestinationDataProvider(destDataProvider);
		isCreat = true;
	}

}
