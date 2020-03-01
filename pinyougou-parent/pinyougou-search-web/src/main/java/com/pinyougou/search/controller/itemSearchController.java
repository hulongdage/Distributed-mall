package com.pinyougou.search.controller;

import java.util.Map;


import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.itemSearchService;

@RestController
@RequestMapping("/itemSearch")
public class itemSearchController {

	@Reference
	private itemSearchService itemSearchService;
	
	@RequestMapping("/search")
	public Map search(@RequestBody Map searchMap) {
		return itemSearchService.search(searchMap);
	}
}
