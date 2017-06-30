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
		function toList(trees){
			const list = [];
			const subtrees = [];
			trees.forEach((item,index,array)=>{
				list.push(item);
				subtrees.concat(item.children);
			});
			list.conat
		}
		$('#tree').jstree({
			'core' : {
				'data' :res
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