package com.pinyougou.solrUtil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

@Component
public class SolrData {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;
	public void importItemData() {
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//已审核
		List<TbItem> list = itemMapper.selectByExample(example);
		System.out.println("==============商品列表==============");
		for(TbItem item:list) {
			System.out.println(item.getTitle());
			//将spec字段中json字符串转换为map
			Map map = JSON.parseObject(item.getSpec(), Map.class);
			//给带注解的字段赋值
			item.setSpecMap(map);
		}
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		System.out.println("=========结束=============");
	}
	
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
		SolrData solrData = (SolrData) context.getBean("solrData");
		solrData.importItemData();
	}
}
