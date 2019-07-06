package cn.zhou.path.service;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class AbsPathNodeService<T extends PathNode<T>> implements PathNodeService<T> {

	protected abstract T queryById(Long id);

	protected abstract int deleteByPathlike(String pathlike);

	protected abstract List<T> queryByPathlike(String string);

	protected abstract int insertNodes(List<T> nodes);
	public abstract T queryRootNode();
	protected abstract int batchUpdate(List<T> nodes);


	@Override
	public int deleteTree(Long pid) {
		T parent = queryById(pid);
		String parentPath = parent.getPath();
		String pathlike = parentPath + "%";
		return deleteByPathlike(pathlike);
	}
	/**
	update path_node 
  	set path = concat(#{newParentPath},right(length(path)-length(#{oldParentPath}))) 
  	where path like #{oldParentPath}||'-%'
  	
  	update path_node 
  	set path = 
	 */
	@Override
	public int moveChildren(Long nodeId, Long targetPid) {
		T oldParent = queryById(nodeId);
		List<T> nodes = queryChildren(oldParent);
		T newParent = queryById(targetPid);
		String newParentPath = newParent.getPath();
		List<T> newBrothers = queryChildren(newParent);
		int i = 0;
		List<String> paths = uniquePath2(newParentPath, newBrothers, nodes.size());
		for(T node : nodes){
			node.setPath(paths.get(i++));
		}
	    return batchUpdate(nodes);
	}

	private List<T> queryChildren(T pnode) {
		return queryByPathlike(pnode.getPath() + PathConst.PREFIX + PathConst.MATCHER);
	}

	@Override
	public List<T> queryChildren(Long pid) {
		T parent = queryById(pid);
		String parentPath = parent.getPath();
		return queryByPathlike(parentPath + PathConst.PREFIX + PathConst.MATCHER);
	}

	/**
	 * select id,name,path,seq from path_node where #{currentPath} like
	 * path||'-%'
	 */
	@Override
	public abstract List<T> queryAncestors(Long nodeId);

	@Override
	public List<T> queryOffspring(Long nodeId) {
		T parent = queryById(nodeId);
		String parentPath = parent.getPath();
		return queryByPathlike(parentPath + PathConst.PREFIX + "%");
	}

	@Override
	public int appendChildren(Long targetPid, List<T> nodes) {
		T parent = queryById(targetPid);
		List<T> brothers = queryChildren(parent);
		List<String> paths = uniquePath2(parent.getPath(), brothers, nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			T node = nodes.get(i);
			node.setPath(paths.get(i));
			node.setParentId(targetPid);
		}
		return insertNodes(nodes);
	}

	@Override
	public int appendChild(Long targetPid, T node) {
		List<T> list = new ArrayList<>(1);
		list.add(node);
		return appendChildren(targetPid, list);
	}

	@Override
	public T toTree(List<T> nodes) {
		LinkedList<T> dest = new LinkedList<>();
		dest.addAll(nodes);
		dest.sort((o1, o2) -> o2.getPath().compareTo(o1.getPath()));// 按字母逆序排序
		return toTreeInternal2(dest);
	}
	 //一个很小的风险，树的层数太深，导致递归调用栈溢出。通过将递归转成循环+栈可以解决。
    /**
     * 参数nodes是一颗树的节点列表，已经根据path逆序排序。
     * 例如
     * -003
     * -002-002
     * -002-001
     * -001-002
     * -001-001-001
     * -001-001
     * -000
     * ''
     * 算法是使用递归，假设该节点的各个子树都已生成，那么树就容易生成。生成子树，需要先把某个子树的所有节点收集起来。
     * */
    private T toTreeInternal(LinkedList<T> nodes) {
    	//树的根节点
    	T root = nodes.removeLast();
        //子树集合
        List<T> children = new ArrayList<>();
        root.setChildren(children);
        LinkedList<T> subNodes = null;
        while(!nodes.isEmpty()){
        	T node = nodes.removeLast();
            if(node.getPath().length()==root.getPath().length()+PathConst.PREFIX.length()+PathConst.LEN){
                if(subNodes!=null){
                	T subNode = toTreeInternal(subNodes);
                    subNode.setParent(root);
                    children.add(subNode);
                }
                subNodes = new LinkedList<>();
                subNodes.addFirst(node);
            }else{
            	subNodes.addFirst(node);
            }
        }
        if(subNodes!=null){
        	T subNode = toTreeInternal(subNodes);
            subNode.setParent(root);
            children.add(subNode);
        }
       /* for(T child:children){
        	child.setParentId(child.getParent().getId());
        }*/
        return root;
    }
	private T toTreeInternal2(LinkedList<T> nodes) {
    	//树的根节点
    	T root = nodes.removeLast();
        List<T> children = new ArrayList<>();
        root.setChildren(children);
        //节点按子树分组
        LinkedList<LinkedList<T>> subTreesNodeList = new LinkedList<>();
        LinkedList<T> subTreeNodeList;
        while(!nodes.isEmpty()){//每一次循环收集一颗子树的所有节点到一个新的集合
        	T node = nodes.removeLast();
        	subTreeNodeList = new LinkedList<>();//某个子树所有的节点
        	subTreesNodeList.add(subTreeNodeList);
        	subTreeNodeList.addFirst(node);
        	while(!nodes.isEmpty()){
        		T last = nodes.getLast();
        		if( last.getPath().length()>root.getPath().length()+PathConst.PREFIX.length()+PathConst.LEN){
        			subTreeNodeList.addFirst(nodes.removeLast());
        		}else{
        			break;
        		}
        	}
        }
        for(LinkedList<T> item:subTreesNodeList){//递归调用，设置关联
        	T subNode = toTreeInternal2(item);
        	subNode.setParent(root);
        	children.add(subNode);
        }
        return root;
    }

	@Override
	public T queryTree(Long rootId) {
		T node = null;
        if(rootId==null){
            node = queryRootNode();
        }else{
            node = queryById(rootId);
        }
        List<T> list = queryOffspring(node.getId());
        list.add(node);
        return toTree(list);
	}
	private List<String> uniquePath(String parentPath, Collection<T> children, int count) {
        // 经过测试，该方法性能极高。获取1w个path的执行时间也可以忽略不计，才3毫秒。
        // 不过如果用到stream，没有并行，都要花44毫秒左右。所以stream启动成本高，比较适合用在需要并行计算的地方。
        // 因为pg的事务隔离级别默认为read commited，所以在并发情况下，该机制可能导致失败。
        // 数据库最好加给path字段加约束，保证数据的正确性。
        //可以使用bitmap，进一步提升性能和可读性。
        // Long start = System.currentTimeMillis();
        if (children.size() + count >= PathConst.MAX) {
            throw new RuntimeException("too much children");
        }
        Set<String> existPaths = new HashSet<>();
        for (T node : children) {
            existPaths.add(node.getPath().substring(parentPath.length()));
        }
        List<String> paths = new ArrayList<>(count);
        int num = 0;
        while (count > 0) {
            String path = Integer.toString(num, PathConst.RADIX);
            path = PathConst.PREFIX + PathConst.TEMPLATE.substring(path.length()) + path;
            if (!existPaths.contains(path)) {
                paths.add(parentPath + path);
                count--;
            }
            num++;
        }
        // Long end = System.currentTimeMillis();
        // System.out.println("time cost = "+(end-start));
        return paths;
    }
	protected List<String> uniquePath2(String parentPath, List<T> children, int count) {
		if (children.size() + count >= PathConst.MAX) {
			throw new RuntimeException("too much children");
		}
		BitSet bitmap = new BitSet();
		for (T node : children) {
			bitmap.set(Integer.parseInt(node.getPath().substring(parentPath.length() + PathConst.PREFIX.length()),
					PathConst.RADIX));
		}
		List<String> paths = new ArrayList<>(count);
		for (int i = 0; i < PathConst.MAX; i++) {
			if (paths.size() < count) {
				if (!bitmap.get(i)) {
					String path = Integer.toString(i, PathConst.RADIX);
					path = PathConst.PREFIX + PathConst.TEMPLATE.substring(path.length()) + path;
					paths.add(path);
				}
			} else {
				break;
			}
		}
		return paths;
	}
}
