$(()=>{
	$('#submit').click(evt=>{
		$.post('path-nodes',param.serializeObj({
			name:$('input[name="name"]').val(),
			pid:$('input[name="pid"]').val(),
			seq:$('input[name="seq"]').val()
		}),null,'json').done(res=>{
			console.info(res);
		});
	});
	$.get('path-nodes',{},null,'json').done(res=>{
		// 使用了尾递归优化，否则栈溢出。逐层将节点添加到集合
		function addToList(list,children){
			if(!children || children.length==0){
				return;
			}
			var nextLevelChildren = [];
			children.forEach((item,index,array)=>{
				list.push(item);
				if(item.children&&item.children.length>0){
					nextLevelChildren = nextLevelChildren.concat(item.children);
				}
			});
			addToList(list,nextLevelChildren);
		}
		const list = [res];
		addToList(list,res.children);
		list.forEach(item=>{
			item.text=item.name;
			item.name;
			item._id = item.id;
			delete item.id;// jstree接收对象有id时，会一直显示loading...
		});
		
		$('#tree').jstree({
			'core' : {
				'data' :[res]
			}
		});
	});
});