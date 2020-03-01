app.controller('searchController',function($scope,$location,searchService){
	//搜索对象
	$scope.searchMap={"keywords":"","category":"","brand":"","price":"","spec":{},"pageNo":1,"pageSize":30,"sortField":"","sort":""};
	//搜索
	$scope.search=function(){
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(
		    function(response){
		    	$scope.resultMap=response;		    	
		    	bulidPageLabel();
		    }		
		);
	}
	
	bulidPageLabel=function(){	
		$scope.pageLabel=[];
		var firstPage=1;//开始页码
		var lastPage=$scope.resultMap.totalPages;//截止页码
		$scope.firstDot=true;//前面有点
		$scope.lastDot=true;//后面有点
		if($scope.resultMap.totalPages>5){//如果页码数量大于5
			if($scope.searchMap.pageNo<=3){//如果当前页码小于等于3,显示5页				
				lastPage=5;
				$scope.firstDot=false;
			}else if($scope.searchMap.pageNo>=$scope.resultMap.totalPages-2){//显示后5页				
				firstPage=$scope.resultMap.totalPages-4;
				$scope.lastDot=false;
			}else{//显示以当前页为中心的5页
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
		}else{
			$scope.firstDot=false;//前面无点
			$scope.lastDot=false;//后边无点
		}
		//循环产生页码标签
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}
	}
	
	//根据页码查询
	$scope.queryByPage=function(pageNo){
		//页码验证
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return ;
		}
		$scope.searchMap.pageNo=pageNo;			
		$scope.search();
		
	}
	
	
	//添加搜索对象
	$scope.addSearchItem=function(key,value){
		if(key=="category" || key=="brand" || key=="price"){//如果点击的是分类或者品牌
			$scope.searchMap[key]=value;
		}else{
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//执行搜索
	}
	
	//移除复合搜索条件
	$scope.removeSearchItem=function(key){
		if(key=="category" || key=="brand" || key=="price"){//如果点击的是分类或者品牌
			$scope.searchMap[key]="";
		}else{
			delete $scope.searchMap.spec[key];//移除此属性
		}
		$scope.search();//执行搜索
	}
	
	//判断当前页是否是第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	//判断当前页是否是最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}
	//设置排序规则
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sort=sort;
		$scope.searchMap.sortField=sortField;
		$scope.search();
	}
	//判断关键字是否是品牌
	$scope.keywordsIsBrand=function(){
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			if($scope.resultMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
				return true;
			}
		}
		return false;
	}
	//加载查询字符串
	$scope.loadKeywords=function(){
		$scope.searchMap.keywords=$location.search()['keywords'];
		$scope.search();
	}
	
});