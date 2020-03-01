//基本控制层
app.controller('baseController',function($scope){
	//分页控件配置
    //currentPage:当前页   totalItems :总记录数  itemsPerPage:每页记录数  perPageOptions :分页选项  onChange:当页码变更后自动触发的方法 
	$scope.paginationConf = {
		currentPage: 1,
		totalItems: 10,
		itemsPerPage: 10,
		perPageOptions: [10, 20, 30, 40, 50],
		onChange: function(){
			$scope.reloadList();
		}
	};
   
    //刷新列表
    $scope.reloadList=function(){
    	$scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }
    
    //复选框选中id集合
    $scope.selectIds=[];
    
    //更新复选框选中id集合
    $scope.updateIds=function($event,id){
    	if($event.target.checked){
    		$scope.selectIds.push(id); //push向集合添加元素
    	}else{
    		var index = $scope.selectIds.indexOf(id);
    		$scope.selectIds.splice(index,1);
    	}
    	
    }
    
	//提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
	$scope.jsonToString=function(jsonString,key){
		var json=JSON.parse(jsonString);//将json字符串转换为json对象
		var value="";
		for(var i=0;i<json.length;i++){		
			if(i>0){
				value+=","
			}
			value+=json[i][key];			
		}
		return value;
	}
	
	//从集合中按照key查询对象
	//[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["5.5寸","5寸"]}]
	$scope.searchObjectByKey=function(list,key,keyValue){
		for(var i=0;i<list.length;i++){
			if(list[i][key]==keyValue){
				return list[i];
			}
		} 
		return null;
	}
});