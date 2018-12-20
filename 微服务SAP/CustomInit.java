package com.leedarson.sap.sapClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 有一些工作必须项目启动之后自动运行，这里是示例代码<br>
 * 
 * @author howard
 * @Create_Date: 2018年6月26日上午8:51:19
 * @Modified_By: howard
 * @Modified_Date: 2018年6月26日上午8:51:19
 * @Why_and_What_is_modified: <br>
 * @Modified_By: howard
 * @Modified_Date: 2018年6月26日上午8:52:02
 * @Why_and_What_is_modified: 调整了实现借口<br>
 */
@Component
public class CustomInit implements CommandLineRunner {

	@Value("${sap.JCO_MSHOST}") 
	String jCO_MSHOST;
	
	@Value("${sap.JCO_GROUP}") 
	boolean GROUP;
	
	@Value("${sap.JCO_GROUP}") 
	String jCO_GROUP;
	
	@Value("${sap.JCO_R3NAME}") 
	String jCO_R3NAME;

	@Value("${sap.JCO_CLIENT}") 
	String jCO_CLIENT;

	@Value("${sap.JCO_ASHOST}") 
	String jCO_ASHOST;
	
	@Value("${sap.JCO_SYSNR}") 
	String jCO_SYSNR;
	
	@Value("${sap.JCO_USER}") 
	String jCO_USER;

	@Value("${sap.JCO_PASSWD}") 
	String jCO_PASSWD;
	
	@Value("${sap.JCO_LANG}") 
	String jCO_LANG;
	
	@Value("${sap.JCO_POOL_CAPACITY}") 
	String jCO_POOL_CAPACITY ;
	
	@Value("${sap.JCO_PEAK_LIMIT}") 
	String jCO_PEAK_LIMIT ;
	
	
	@Override
	/**
	 * 初始化SAP参数
	 */
	public void run(String... args) throws Exception {

		DestinationProvider.setJCO_USER(jCO_USER);
		DestinationProvider.setJCO_SYSNR(jCO_SYSNR);
		DestinationProvider.setJCO_R3NAME(jCO_R3NAME);
		DestinationProvider.setJCO_POOL_CAPACITY(jCO_POOL_CAPACITY);
		DestinationProvider.setJCO_PEAK_LIMIT(jCO_PEAK_LIMIT);
		DestinationProvider.setJCO_PASSWD(jCO_PASSWD);
		DestinationProvider.setJCO_MSHOST(jCO_MSHOST);
		DestinationProvider.setJCO_LANG(jCO_LANG);
		DestinationProvider.setJCO_GROUP(jCO_GROUP);
		DestinationProvider.setJCO_CLIENT(jCO_CLIENT);
		DestinationProvider.setJCO_ASHOST(jCO_ASHOST);
		DestinationProvider.setGROUP(GROUP);
	}

}
