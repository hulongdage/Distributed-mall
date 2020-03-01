package com.pinyougou.cart.service;

import java.util.List;

import com.pinyougou.pojogroup.Cart;

public interface CartService {

	/**
	 * 添加商品到购物车
	 * @param cartList
	 * @param itemId
	 * @param num
	 * @return
	 */
	public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);
}
