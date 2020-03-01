app.controller('itemController',function($scope){
	//数量操作
	$scope.addNum=function(x){
      $scope.num+=x;
	  if($scope.num<1){
	     $scope.num=1;
	  }
	  }
	  
	 //记录用户所选择的规格
     $scope.specificationItems={};
	 
	 $scope.selectSpecification=function(name,value){
         $scope.specificationItems[name]=value; 
		 searchSku();
	 }
	 
	 //判断某规格选项是否被用户选中
	 $scope.isSelected=function(name,value){
         if( $scope.specificationItems[name]==value){
             return true;
		 }else{
		     return false;
		 }
        
	 }
      
	 $scope.sku={};//当前选择的SKU
	 //加载默认sku
	 $scope.loadSku=function(){
	      $scope.sku=skuList[0];
          $scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));
	 }

	 //匹配两个对象是否相等
	 matchObject=function(map1,map2){
	        for(var k in map1){
			   if(map1[k]!=map2[k]){
			      return false;
			   }
			}
			for(var k in map2){
			   if(map2[k]!=map1[k]){
			      return false;
			   }
			}
			return true;
	 }

	 //根据规格查询sku
	 searchSku=function(){
	     for(var i=0;i<skuList.length;i++){
		    if(matchObject(skuList[i].spec, $scope.specificationItems)){
			    $scope.sku=skuList[i];
				return ;
			}
		 }
		 $scope.sku={id:0,title:'-----',price:0};//如果没有匹配的		
	 }

	 //添加商品到购物车
	$scope.addToCart=function(){
		alert('SKUID:'+$scope.sku.id );		
	}
	  
	});