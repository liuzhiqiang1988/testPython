package com.leedarson.sap.sapClient;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.DestinationDataProvider;

/**
 * 与SAP连接配置
 * 
 * @author wy
 */
public class SAPConn {

	// 初始化日志对象
	private static Logger log = LoggerFactory.getLogger(SAPConn.class);

	private static final String ABAP_AS_POOLED = "ABAP_AS_WITH_POOL";

	static {
		Properties connectProperties = new Properties();

		if (false) { // 正式机
			connectProperties.setProperty(DestinationDataProvider.JCO_MSHOST, "192.168.6.79"); // 服务器
			connectProperties.setProperty(DestinationDataProvider.JCO_GROUP, "Leedarson"); // 集群
			connectProperties.setProperty(DestinationDataProvider.JCO_R3NAME, "PRD"); //
			connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "800"); // SAP集团
		} else {// 测试机
			connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "192.168.8.93"); // 服务器
			connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "00"); // 系统编号
			connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "600"); // SAP集团
		}

		connectProperties.setProperty(DestinationDataProvider.JCO_USER, "SYS_RFC"); // SAP用户名
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "localpsd"); // 密码
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "zh"); // 登录语言
		connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, "3"); // 最大连接数
		connectProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, "10"); // 最大连接线程
		createDataFile(ABAP_AS_POOLED, "jcoDestination", connectProperties);
	}

	/**
	 * 创建SAP接口属性文件。
	 * 
	 * @param name
	 *            ABAP管道名称
	 * @param suffix
	 *            属性文件后缀
	 * @param properties
	 *            属性文件内容
	 */
	private static void createDataFile(String name, String suffix, Properties properties) {
		File cfg = new File(name + "." + suffix);
		if (cfg.exists()) {
			cfg.deleteOnExit();
		}
		try {
			FileOutputStream fos = new FileOutputStream(cfg, false);
			properties.store(fos, "for tests only !");
			fos.close();
		} catch (Exception e) {
			log.error("Create Data file fault, error msg: " + e.toString());
			throw new RuntimeException("Unable to create the destination file " + cfg.getName(), e);
		}
	}

	/**
	 * 获取SAP连接
	 * 
	 * @return SAP连接对象
	 */
	public static JCoDestination connect() {
		JCoDestination destination = null;
		try {
			destination = JCoDestinationManager.getDestination(ABAP_AS_POOLED);
		} catch (JCoException e) {
			log.error("Connect SAP fault, error msg: " + e.toString());
		}
		return destination;
	}

}
