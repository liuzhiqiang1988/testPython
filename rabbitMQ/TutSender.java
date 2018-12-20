package com.leedarson.monitor.mq;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.leedarson.monitor.entity.WebMonitorEntity;

@Component
public class TutSender {

	@Autowired
	private  RabbitTemplate template;

	@Autowired
	private  FanoutExchange fanout;

	
	
//	int i = 0;
//	@Scheduled(fixedDelay = 5 * 1000, initialDelay = 500)
//	public void send() {
//		String message = "  这是输出数据  " + i;
//		JSONObject d = new JSONObject();
//		d.put("我是KEY", "我是DATA");
//		template.convertAndSend(fanout.getName(),"", d);
//		i++;
//		System.out.println("执行了数据输出");
//	}
	
	
	/**
	 * 发送异常实体对象
	 * @user LZQ
	 * @Date 2018年8月8日
	 * @param entity
	 */
	public void sendErrorEntity(WebMonitorEntity entity){
		template.convertAndSend(fanout.getName(),"", entity);
	}
}