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
		function addToList(list,trees){
			if(!trees || trees.length==0){
				return;
			}
			var nextLevelTrees = [];
			trees.forEach((item,index,array)=>{
				list.push(item);
				if(item.children&&item.children.length>0){
					nextLevelTrees = nextLevelTrees.concat(item.children);
				}
			});
			addToList(list,nextLevelTrees);
		}
		const list = [res];
		addToList(list,res.children);
		list.forEach(item=>{
			item.text=item.name;
			item.name;
			item._id = item.id;
			delete item.id;//jstree接收对象有id会显示loading...
		});
		
		$('#tree').jstree({
			'core' : {
				'data' :[res]
			}
		});
/*		$('#tree').jstree({
			'core' : {
				'data' : [
				          { "text" : "Root node", "children" : [
				                                                { "text" : "Child node 1" },
				                                                { "text" : "Child node 2" }
				                                                ]}
				          ]
			}
		});
*/	});
});