 package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.itemSearchService;
@Service(timeout=5000)
public class itemSearchServiceImpl implements itemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Override
	public Map search(Map searchMap) {
		Map map = new HashMap();
		String keywords = (String) searchMap.get("keywords");
		map.put("keywords", keywords.replace(" ", ""));
/*		Query query=new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria );
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		map.put("rows", page.getContent());*/
		//1.按关键字查询（高亮显示）
		map.putAll(searchList(searchMap));
		//2.根据关键字查询商品分类
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList",categoryList);	
		//3.查询品牌和规格列表
		String category = (String) searchMap.get("category");
		if(!"".equals(category)) {
			map.putAll(searchBrandAndSpec(category));
		}else {
			if(categoryList.size()>0) {
				map.putAll(searchBrandAndSpec(categoryList.get(0)));
			}			
		}	
		return map;
	}
	
	private Map searchList(Map searchMap) {
		        Map map = new HashMap();
		        //高亮显示
				HighlightQuery query = new SimpleHighlightQuery();
				
				//构建高亮选项选项
				HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//高亮域
				highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
				highlightOptions.setSimplePostfix("</em>");//高亮后缀
				query.setHighlightOptions(highlightOptions );//为查询对象设置高亮选项
				
				//关键字查询
				Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
				query.addCriteria(criteria);
				
				//按商品分类过滤
				if(!"".equals(searchMap.get("category"))) {//如果用户选择了分类
					FilterQuery filterQuery = new SimpleFilterQuery();
					Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
					filterQuery.addCriteria(filterCriteria );
					query.addFilterQuery(filterQuery);
				}
				//按品牌过滤
				if(!"".equals(searchMap.get("brand"))){			
					Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
					FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
					query.addFilterQuery(filterQuery);
				}
				//按规格过滤
				if(searchMap.get("spec")!=null) {
					Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
					for(String key:specMap.keySet()) {
						Criteria filterCriteria=new Criteria("item_spec_"+key).is(specMap.get(key));
						FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
						query.addFilterQuery(filterQuery);
					}
				}
				//按价格过滤
				if(!"".equals(searchMap.get("price"))) {
					String[] price = ((String)searchMap.get("price")).split("-");
					if(!price[0].equals("0")) {//如果最低价格不等于0
						Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
						FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
						query.addFilterQuery(filterQuery);
					}
					if(!price[1].equals("*")) {//如果最高价格不等于*
						Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
						FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
						query.addFilterQuery(filterQuery);
					}
				}
				//分页查询
				Integer pageNo =  (Integer) searchMap.get("pageNo");//提取页码
				if(pageNo==null) {
					pageNo=1;
				}
				Integer pageSize =  (Integer) searchMap.get("pageSize");//每页记录数
				if(pageSize==null) {
					pageNo=20;
				}
				query.setOffset((pageNo-1)*pageSize);//从第几条记录开始查询
				query.setRows(pageSize);
				
				//排序
				String sortValue = (String) searchMap.get("sort");//ASC DESC
				String sortField = (String) searchMap.get("sortField");//排序字段
				if(sortValue!=null && !"".equals(sortValue)) {
					if(sortValue.equals("ASC")) {
						Sort sort = new Sort(Sort.Direction.ASC, "item_"+sortField);
						query.addSort(sort);
					}
					if(sortValue.equals("DESC")) {
						Sort sort = new Sort(Sort.Direction.DESC, "item_"+sortField);
						query.addSort(sort);
					}
				}
				
				//高亮页对象
				HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
				//高亮入口集合(每条记录的高亮入口)
				List<HighlightEntry<TbItem>> entryList = highlightPage.getHighlighted();
				for(HighlightEntry<TbItem> entry : entryList) {
					//获取高亮列表(高亮域的个数)
					List<Highlight> highlights = entry.getHighlights();
					/*for(Highlight highLight : highlights) {
						List<String> snipplets = highLight.getSnipplets();//每个域可能存储多值
						System.out.println(snipplets);
					}*/
					if(highlights.size()>0 && highlights.get(0).getSnipplets().size()>0) {
						TbItem item = entry.getEntity();
						item.setTitle(highlights.get(0).getSnipplets().get(0));
					}
					
				}
				map.put("rows", highlightPage.getContent());
				map.put("totalPages", highlightPage.getTotalPages());//返回总页数
				map.put("total", highlightPage.getTotalElements());//返回总记录数
				return map;			
	}
	
	//查询分类列表
	private List<String> searchCategoryList(Map searchMap) {
		List<String> list=new ArrayList();
		Query query = new SimpleQuery("*:*");
		//按照关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria );
		//设置分组选项
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		//得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query , TbItem.class);
		//根据列得到分组结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//得到分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//得到分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		for(GroupEntry<TbItem> entry : content) {
			list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
		}
		return list;
	}
	
	private Map searchBrandAndSpec(String category) {
		Map map =new HashMap();
		Long categoryId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if(categoryId != null) {
			//根据模板ID查询品牌列表 
			List brandList = (List) redisTemplate.boundHashOps("brandIds").get(categoryId);
			map.put("brandList", brandList);
			//根据模板ID查询规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(categoryId);
			map.put("specList", specList);
		}
		return map;
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria );
		solrTemplate.delete(query);
		solrTemplate.commit();	
	}

}
