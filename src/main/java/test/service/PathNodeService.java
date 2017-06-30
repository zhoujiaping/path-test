package test.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import test.constant.PathConst;
import test.mapper.PathNodeMapper;
import test.model.PathNode;
import test.model.PathNodeExample;

public class PathNodeService {
	private PathNodeMapper mapper;

	public PathNodeMapper getMapper() {
		return mapper;
	}

	public void setMapper(PathNodeMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * 为了方便，程序可以统一处理，path统一以'-'开头，这虽然不是必须的，但有分隔符可以增强可读性。
	 * 查询顶级节点的子节点，parentPath传""
	 * 根节点的path为""。
	 */
	private int batchInsertInternal(String parentPath,List<PathNode> nodes) {
		PathNodeExample example = new PathNodeExample();
		example.createCriteria().andPathLike(parentPath + PathConst.PREFIX + PathConst.MATCHER);
		List<PathNode> children = mapper.selectByExample(example);
		List<String> paths = uniquePath(parentPath, children, nodes.size());
		for(int i=0;i<nodes.size();i++){
			nodes.get(i).setPath(paths.get(i));
		}
		return mapper.batchInsertSelective(nodes);
	}
	public int delete(long id){
		return mapper.deleteByPrimaryKey(id);
	}
	public int update(){
		//TODO
		return -1;
	}
	//移动多棵树
	public int move(List<PathNode> nodes,long toPid){
		if(nodes.isEmpty()){
			return 0;
		}
		for(PathNode node:nodes){
			
		}
		PathNode to = mapper.selectByPrimaryKey(toPid);
		String oldParentPath = parentPath(node);
		String newParentPath = to.getPath();
		List<String> newLocalPaths = uniquePath();
		return mapper.moveNode(newParentPath,nodes,newLocalPaths);
	}
	private String parentPath(PathNode node){
		String path = node.getPath();
		return path.substring(0, path.lastIndexOf(PathConst.PREFIX));
	}
	private String uniquePath(String parentPath, Collection<PathNode> children) {
		List<String> paths = uniquePath(parentPath,children,1);
		return paths.get(0);
	}
	private List<String> uniquePath(String parentPath, Collection<PathNode> children,int count) {
		//经过测试，该方法性能极高。获取1w个path的执行时间也可以忽略不计，才3毫秒。
		//不过如果用到stream，没有并行，都要花44毫秒左右。所以stream性能不高，比较适合用在需要并行计算的地方。
		//long start = System.currentTimeMillis();
		if (children.size() + count >= PathConst.MAX) {
			throw new RuntimeException("too much children");
		}
		Set<String> existPaths = new HashSet<>();
		for(PathNode node:children){
			existPaths.add(node.getPath().substring(parentPath.length()));
		}
		List<String> paths = new ArrayList<>(count);
		int num = 0;
		while(count>0){
			String path = Integer.toString(num, PathConst.RADIX);
			path = PathConst.PREFIX+PathConst.TEMPLATE.substring(path.length())+path;
			if(!existPaths.contains(path)){
				paths.add(parentPath+path);
				count--;
			}
			num++;
		}
		//long end = System.currentTimeMillis();
		//System.out.println("time cost = "+(end-start));
		return paths;
	}

	public int batchInsert(long pid, List<PathNode> nodes) {
		PathNode parent = mapper.selectByPrimaryKey(pid);
		return batchInsertInternal(parent.getPath(),nodes);
	}
	public int insert(long pid, PathNode node) {
		PathNode parent = mapper.selectByPrimaryKey(pid);
		return batchInsertInternal(parent.getPath(),Arrays.asList(new PathNode[]{node}));
	}
}
