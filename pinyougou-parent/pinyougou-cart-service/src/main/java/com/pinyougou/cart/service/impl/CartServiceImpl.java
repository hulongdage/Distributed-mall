package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private TbItemMapper itemMapper;
	
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		//1.根据SKU ID查询商品明细SKU的对象
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if(item==null) {
			throw new RuntimeException("商品不存在");
		}
		if(!item.getStatus().equals("1")) {
			throw new RuntimeException("商品状态不合法");
		}
		//2.根据SKU对象得到商家ID
		String sellerId = item.getSellerId();//商家id
		//3.根据商家ID在购物车列表中查询购物车对象
		Cart cart = searchCartBySellerId(cartList,sellerId);
		if(cart==null) {//4.如果购物车列表中不存在该商家的购物车
			//4.1创建一个新的购物车对象
			cart=new Cart();
			cart.setSellerId(sellerId);//商家id
			cart.setSellerName(item.getSeller());//商家名称
			List<TbOrderItem> orderItemList = new ArrayList();//创建购物车明细列表
			TbOrderItem orderItem = createOrderItem(item, num);
			orderItemList.add(orderItem);
			cart.setOrderItemlist(orderItemList);
			//4.2将新的购物车对象加到购物车列表中
			cartList.add(cart);
		}else {//5.如果购物车列表中存在该商家的购物车
			//判断该商品是否在该购物车的明细列表中存在
			//5.1如果不存在,创建新的购物车明细对象,并添加到该购物车的明细列表中
			
			//5.2如果存在,在原有的数量上添加数量,并且更新金额
		}

		return null;
	}
	/**
	 * 根据商家id在购物车列表中查询购物车对象
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
		for(Cart cart:cartList) {
			if(cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;		
	}
	/**
	 * 创建订单明细
	 * @param item
	 * @param num
	 * @return
	 */
	private TbOrderItem createOrderItem(TbItem item,Integer num){
		if(num<=0){
			throw new RuntimeException("数量非法");
		}
		
		TbOrderItem orderItem=new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
		return orderItem;
	}
}

}
