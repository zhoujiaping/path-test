window.param = (function(){
	function serialize(key,value,param){
		
		if(Array.isArray(value)){
			for(let i=0;i<value.length;i++){
				serialize(key+'['+i+']',value[i],param)
			}
		}else if(value.constructor===Object){
			for(let k in value){
				serialize(key+'.'+k,value[k],param);
			}
		}else{
			param[key]=value;
		}
	}
	function serializeObj(obj){
		const param = {};
		for(let k in obj){
			serialize(k,obj[k],param);
		}
		return param;
	}
	return {serializeObj};
})();
