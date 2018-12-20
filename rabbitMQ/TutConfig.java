package com.leedarson.monitor.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class TutConfig {

	@Bean
	public FanoutExchange fanout() {
		return new FanoutExchange("monitorErrorInfo");
	}

	@Bean
	public Queue autoDeleteQueue() {
		return new AnonymousQueue();
	}

	@Bean
	public Binding binding(FanoutExchange fanout, Queue autoDeleteQueue) {
		return BindingBuilder.bind(autoDeleteQueue).to(fanout);
	}

	@Bean
	public TutReceiver receiver() {
		return new TutReceiver();
	}

	@Profile("sender")
	@Bean
	public TutSender sender() {
		return new TutSender();
	}

}
