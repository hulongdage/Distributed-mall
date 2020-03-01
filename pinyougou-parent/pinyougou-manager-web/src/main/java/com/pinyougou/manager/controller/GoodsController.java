package com.pinyougou.manager.controller;
import java.util.Arrays;

import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	//@Reference(timeout=10000)
	//private itemSearchService itemSearchService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}		
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	@Autowired
	private Destination queueSolrDeleteDestination;
	@Autowired
	private Destination topicPageDeleteDestination;
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			//从索引库中删除
			//itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					
					return session.createObjectMessage(ids);
				}
			});
			
			//删除每个服务器上的商品详细页
            jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					
					return session.createObjectMessage(ids);
				}
			});
			
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Destination queueSolrDestination;
	@Autowired
	private Destination topicPageDestination;
	
	/**
	 * 更新商品状态
	 * @param ids
	 * @param status
	 * @return
	 */
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status) {
		try {
			goodsService.updateStatus(ids, status);
			if(status.equals("1")) {
				List<TbItem> list = goodsService.findItemListByGoodsIdandStatus(ids, status);
				if(list.size()>0) {
					//itemSearchService.importList(list);
					String jsonString = JSON.toJSONString(list);//转换为json传输
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {						
						@Override
						public Message createMessage(Session session) throws JMSException {
							
							return session.createTextMessage(jsonString);
						}
					});
				}else {
					System.out.println("没有明细数据");
				}
				System.out.println("333333333333333333333");
				//静态页生成
				for(Long goodsId : ids) {
					System.out.println("11111111111111111111111");
					//itemPageService.genItemHtml(goodsId);
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						
						@Override
						public Message createMessage(Session session) throws JMSException {
							System.out.println("2222222222222222222");
							return session.createTextMessage(goodsId+"");
						}
					});
				}
			}
			return new Result(true, "更新成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(true, "更新失败");
		}	
	}
	
	//@Reference(timeout=50000)
	//private ItemPageService itemPageService;
	@RequestMapping("/genHtml")
	public void genHtml(Long goodsId) {
		//itemPageService.genItemHtml(goodsId);
	}
	
}
