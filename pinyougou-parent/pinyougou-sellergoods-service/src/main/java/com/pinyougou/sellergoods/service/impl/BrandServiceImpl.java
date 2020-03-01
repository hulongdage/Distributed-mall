package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
@Service
@Transactional
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper brandMapper;
	
	@Override
	public List<TbBrand> findAll() {

		return brandMapper.selectByExample(null);
	}

	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
	    Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(TbBrand tbBrand) {
		brandMapper.insert(tbBrand);		
	}

	@Override
	public TbBrand findOne(Long id) {
		TbBrand brand = brandMapper.selectByPrimaryKey(id);
		return brand;
	}

	@Override
	public void update(TbBrand tbBrand) {
		brandMapper.updateByPrimaryKey(tbBrand);		
	}

	@Override
	public void delete(Long[] ids) {
		for(Long id : ids) {
			brandMapper.deleteByPrimaryKey(id);
		}
		
	}

	@Override
	public PageResult findPage(TbBrand tbBrand, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		TbBrandExample tbBrandExample = new TbBrandExample();
		Criteria criteria = tbBrandExample.createCriteria();
		if(tbBrand != null) {
			if(tbBrand.getName() != null && tbBrand.getName().length()>=0) {
				criteria.andNameLike("%"+tbBrand.getName()+"%");
			}else if(tbBrand.getFirstChar() != null && tbBrand.getFirstChar().length()>=0) {
				criteria.andFirstCharEqualTo(tbBrand.getFirstChar());
			}
		}
		Page<TbBrand> list = (Page<TbBrand>) brandMapper.selectByExample(tbBrandExample);
		return new PageResult(list.getTotal(), list.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		return brandMapper.selectOptionList();		
	}

}
