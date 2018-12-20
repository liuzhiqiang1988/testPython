package com.leedarson.email.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.soap.Addressing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.leedarson.email.service.WOPIService;


@RestController
@RequestMapping("")
public class WOPIController {
	
	@Autowired
	private  WOPIService   wopi ;
	
	
	//http://localhost/ldsEmail/wopi*/files/
	@RequestMapping("/wopi/files/{id}")	
	public  JSONObject checkFileInfo(@PathVariable("id") String id){
		return wopi.checkFileInfo(id);
	}
	
	
	@RequestMapping("/wopi/files/{id}/contents")
	public void  getFile( HttpServletResponse response,@PathVariable("id") String id){
		wopi.getFile(id,response);
	}
	
	
}
