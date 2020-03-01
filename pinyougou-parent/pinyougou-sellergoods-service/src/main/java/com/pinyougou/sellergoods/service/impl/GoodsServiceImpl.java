package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private TbBrandMapper brandMapper;
	
	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	@Autowired
	private TbSellerMapper sellerMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");//设置未申请状态
		goodsMapper.insert(goods.getGoods());
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//设置id
		goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展数据
		saveItemList(goods);//插入商品SKU列表数据
	}
	
	private void setItemValus(Goods goods,TbItem item) {
		List<Map> list = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if(list.size()>0) {
			item.setImage((String)list.get(0).get("url"));
		}
		item.setGoodsId(goods.getGoods().getId());//商品SPU编号
		item.setSellerId(goods.getGoods().getSellerId());//商家编号
		item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号（3级）
		item.setCreateTime(new Date());//创建日期
		item.setUpdateTime(new Date());//修改日期 
		//品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());		
		//商家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//设置未申请状态:如果是经过修改的商品，需要重新设置状态
		goods.getGoods().setAuditStatus("0");
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//删除原有的SKU列表数据
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example );
		//添加新的SKU列表数据
		saveItemList(goods);//插入商品SKU列表数据	
	}	
	
	/**
	 * 插入SKU列表数据
	 * @param goods
	 */
	private void saveItemList(Goods goods) {
		if("1".equals(goods.getGoods().getIsEnableSpec())) {
			for(TbItem item : goods.getItemList()) {
				//标题
				String title = goods.getGoods().getGoodsName();
				Map<String,Object> map = JSON.parseObject(item.getSpec());
				for(String key : map.keySet()) {
					title+=""+map.get(key);
				}
				item.setTitle(title);
				setItemValus(goods, item);
				itemMapper.insert(item);
			}
		}else {			
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());
			item.setNum(99999);
			item.setPrice(goods.getGoods().getPrice());
			item.setStatus("1");
			item.setIsDefault("1");
			item.setSpec("{}");
			setItemValus(goods, item);
			itemMapper.insert(item);
		      }
	}
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(goodsDesc);
		//查询SKU商品列表
		TbItemExample itemExample = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = itemExample.createCriteria();
		criteria.andGoodsIdEqualTo(id);//查询条件:商品id
		List<TbItem> list = itemMapper.selectByExample(itemExample);
		goods.setItemList(list);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(tbGoods);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();//非删除状态
		if(goods!=null){			
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			  //criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public void updateStatus(Long[] ids, String status) {
			for(Long id : ids) {
				TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
				tbGoods.setAuditStatus(status);
				goodsMapper.updateByPrimaryKey(tbGoods);
			}
			
		}

		@Override
		public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
			TbItemExample example = new TbItemExample();
			com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdIn(Arrays.asList(goodsIds));
			criteria.andStatusEqualTo(status);
			return itemMapper.selectByExample(example );
			
		}	
}
