package com.leedarson.monitor.mq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.leedarson.monitor.entity.WebMonitorEntity;
import com.leedarson.monitor.service.MonitorServicre;


@Component
public class TutReceiver {

	

//    @RabbitListener(queues = "#{autoDeleteQueue.name}")
//    public void receive1(JSONObject in) {
//    	System.out.println("这是接收的数据");
//    	System.out.println(in);
//    }
    
    
	/**
	 * 接受异常实体数据
	 * @user LZQ
	 * @Date 2018年8月8日
	 * @param in
	 */
    @RabbitListener(queues = "#{autoDeleteQueue.name}")
    public void receive1(WebMonitorEntity entity) {
    	MonitorServicre.addMonitor(entity);
    }

}