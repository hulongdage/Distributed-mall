package com.pinyougou.page.service.impl;

import javax.jms.JMSException;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;
@Component
public class PageListener implements MessageListener {
    
	@Autowired
	private ItemPageService itemPageService;
	@Override
	public void onMessage(Message message) {
		TextMessage textMessage=(TextMessage)message;
		System.out.println("=========我是页面生成监听");
		try {
			String goodsId = textMessage.getText();
			System.out.println("接收到消息:"+goodsId);
			boolean b = itemPageService.genItemHtml(Long.parseLong(goodsId));
			System.out.println("网页生成结果:"+b);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
