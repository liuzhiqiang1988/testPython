package com.leedarson.sap.service;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leedarson.sap.sapClient.DestinationProvider;
import com.sap.conn.jco.JCoAbapObject;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoMetaData;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;

/**
 * 调用客户端SAP接口
 * @user LZQ
 * @Date 2018年9月10日
 */
@Service
public class SapClicentExecuteUtil {
	
	/**
	 * 参数传输方式
	 * 表格（Table）  输入接口（Import）
	 */
	public static enum DataType {
		Table,Import
	}
	
	
	
	
	
	
	/**
	 * 调用客户端SAP接口
	 * @user LZQ
	 * @Date 2018年9月10日
	 * @param tableName     输入表格名称
	 * @param data          输入数据  格式 [{"名称":"值"},{"名称":"值"}]
	 * @param funtionName   SAP方法名称
	 * @param type          
	 * @return    {"state":"是否调用成功","data":"数据",message:"异常消息"}
	 */
	public JSONObject executeSap(String tableName ,JSONArray data,String funtionName,DataType type){
		if(data == null){
			data = new JSONArray();
		}
		JCoDestination destination = DestinationProvider.connect();
		JCoFunction function = null;
		JSONObject backData = new JSONObject();
		backData.put("state", true);
		JSONArray outTable = new JSONArray();
		JSONArray outExport = new JSONArray();
		try {
			function = destination.getRepository().getFunction(funtionName);
			if(function == null){
				backData.put("state", false);
				backData.put("error", "SAP接口为空");
				return backData ;
			}
			
			JCoParameterList importParameterList  =  function.getImportParameterList();
			JCoParameterList tablParameterList =  function.getTableParameterList();
			JCoParameterList exportParameterList  = function.getExportParameterList();
			
			if("Import".equals(type.name())){   //使用ImportParameterList   传输数据
				
				if(StringUtils.isEmpty(tableName) ){
					if(data.size()>0){
						JSONObject item = data.getJSONObject(0);
						item.forEach((K,V)->{
							importParameterList.setValue(K, V);
						});
					}
				}else{
					JCoTable table  = importParameterList.getTable(tableName);
					for(int i=0;i<data.size();i++){
						JSONObject item = data.getJSONObject(i);
						table.appendRow();
						item.forEach((K,V)->{
							table.setValue(K,V);
						});
					}
				
				}
				
				
			}
			if("Table".equals(type.name())){     //使用TableParameterList  传输数据
				JCoTable table  =  tablParameterList.getTable(tableName);
				for(int i=0;i<data.size();i++){
					JSONObject item = data.getJSONObject(i);
					table.appendRow();
					item.forEach((K,V)->{
						table.setValue(K,V);
					});
				}
			}
			
			
			System.out.println("接口名称" + funtionName);
			System.out.println(new Date());
			function.execute(destination);
			System.out.println("结束时间");
			System.out.println(new Date());
			
			
			if(exportParameterList != null){   //使用ExportParameterList  接受数据
				JCoMetaData meta = exportParameterList.getMetaData();
				JSONObject d = new JSONObject();
				for(int i=0;i<exportParameterList.getFieldCount();i++){
					d.put(meta.getName(i), exportParameterList.getValue(i));
				}
				outExport.add(d);
			}
			backData.put("export", outExport);
			
			if(tablParameterList != null){   //使用TableParameterList  接受数据
				JSONObject d = new JSONObject();
				for(int i=0;i<tablParameterList.getFieldCount();i++){
					String  name =  tablParameterList.getMetaData().getName(i);
					JCoTable table  = tablParameterList.getTable(name);
					JCoMetaData meta = table.getMetaData();
					
					JSONArray arr = new JSONArray() ;
					for(int n=0;n<table.getNumRows();n++){
						table.setRow(n);
						JSONObject d1 = new JSONObject();
						for(int z=0;z<table.getFieldCount();z++){
							Object value =  table.getValue(z);
							if(value ==null){
								value = "";
							}
							d1.put(meta.getName(z), value);
						}
						arr.add(d1);
					}
				
					d.put(name, arr);
				}
				outTable.add(d);
			}
	        
			backData.put("table", outTable);
			
		} catch (JCoException e) {
			e.printStackTrace();
			backData.put("message", "接口异常");
			backData.put("state", false);
		}
		return backData ;
	}
	
	/**
	 * 设置表格数据
	 * @user LZQ
	 * @Date 2018年10月22日
	 * @param table
	 * @param values
	 */
	public void setTableValue(JCoTable table ,JSONArray values){
		JCoMetaData meta = table.getMetaData();
		//遍历是否有时间字段
		JSONArray dateList = new JSONArray() ;
		
		
		for(int i=0;i<meta.getFieldCount();i++){
		  String  className =	meta.getClassNameOfField(i);
		  System.out.println(className);
		}
		
		
	}

}
