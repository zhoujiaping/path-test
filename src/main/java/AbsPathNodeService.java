import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
/**
 * 
drop table if exists t_industry;
create table t_industry (
	id bigint primary key auto_increment,
    parent_id bigint,
    path varchar(50),
    name varchar(50),
    code varchar(50)
);
-- 初始化根路径
insert into t_industry(path,name)values('','行业');
 * @author 01375156
 *
 * @param <T>
 */
public abstract class AbsPathNodeService<T extends PathNode<T>> implements PathNodeService<T> {
	/**
	 * select * from t_industry where path=#{path}
	 * @param path
	 * @return
	 */
	protected abstract T queryByPath(String path);
	/**
	 * delete from t_table where path like #{pathlike};
	 */
	protected abstract int deleteByPathlike(String pathlike);
	/**
	 * select * from t_industry where path like #{pathlike}
	 * @param pathlike
	 * @return
	 */
	protected abstract List<T> queryByPathlike(String pathlike);
	/**
	 * select path from t_table where path like #{path}
	 * */
	protected abstract List<String> queryPathByPathlike(String path);
	/**
	 * insert into t_industry(path,parent_id)values(#{},#{})
	 * @param nodes
	 * @return
	 */
	protected abstract int insertNodes(List<T> nodes);
	/**
	 * select * from t_industry where path=''
	 */
	public abstract T queryRootNode();
	/**
	update t_table 
  	set path = concat(#{newpath},right(path,length(path)-length(#{oldPath}))) 
  	where path like #{oldPath}||'%'
	 */
	protected abstract int updatePath(String oldPath,String newpath);
	/**
	 * select * from t_industry where #{} like path||'%'
	 */
	public abstract List<T> queryAncestors(String path);

	/**
	 ** 删除子树
	 */
	@Override
	public int deleteTree(String path) {
		T parent = queryByPath(path);
		String parentPath = parent.getPath();
		String pathlike = parentPath + "%";
		return deleteByPathlike(pathlike);
	}
	/**
	 * 移动一颗树
	 */
	@Override
	public int moveTree(String path,String toPath) {
		if(!path.matches("(/[0-9a-zA-Z]{3})+")){
			throw new RuntimeException("path格式错误：【{}】");
		}
		if( !toPath.matches("(/[0-9a-zA-Z]{3})+")){
			throw new RuntimeException("path格式错误：【{}】");
		}
		List<String> brothers = queryPathByPathlike(toPath+PathNodeUtil.PATH_SPLITER+PathNodeUtil.MATCHER);
		List<String> paths = uniquePath2(toPath, brothers,1);
	    return updatePath(path,toPath+PathNodeUtil.PATH_SPLITER+paths.get(0));
	}
	
	/**
	 * 查询子节点
	 */
	@Override
	public List<T> queryChildren(String path) {
		return queryByPathlike(path + PathNodeUtil.PATH_SPLITER +PathNodeUtil.MATCHER);
	}


	/**
	 * 查询后代节点，包括自己
	 */
	@Override
	public List<T> queryOffspring(String path) {
		return queryByPathlike(path + "%");
	}
	
	/**
	 * 批量添加子节点
	 */
	@Override
	public int appendChildren(String path, List<T> nodes) {
		T parent = queryByPath(path);
		List<String> brothers = queryPathByPathlike(path+PathNodeUtil.PATH_SPLITER+PathNodeUtil.MATCHER);
		List<String> paths = uniquePath2(path, brothers, nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			T node = nodes.get(i);
			node.setPath(path+PathNodeUtil.PATH_SPLITER+paths.get(i));
			node.setParentId(parent.getId());
		}
		return insertNodes(nodes);
	}

	@Override
	public int appendChild(String path, T node) {
		List<T> list = new ArrayList<>(1);
		list.add(node);
		return appendChildren(path, list);
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
     * /003
     * /002/002
     * /002/001
     * /002
     * /001/002
     * /001/001/001
     * /001/001
     * /001
     * /000
     * 
     * ''
     * 算法是使用递归，假设该节点的各个子树都已生成，那么树就容易生成。生成子树，需要先把某个子树的所有节点收集起来。
     * */
	protected T toTreeInternal2(LinkedList<T> nodes) {
    	//树的根节点
    	T root = nodes.removeLast();
        //子树集合
        List<T> children = new ArrayList<>();
        root.setChildren(children);
        //根节点下一个子树的所有节点
        LinkedList<T> subNodes = null;
        while(!nodes.isEmpty()){
        	T node = nodes.removeLast();
            if(node.getPath().length()==root.getPath().length()+PathNodeUtil.PATH_SPLITER.length()+PathNodeUtil.LEN){
            	//如果是root节点的子节点，说明上一个子树的节点收集完毕。
            	//那就把之前收集到的节点处理掉，转成子树。然后重新收集。
            	if(subNodes!=null){
            		T subNode = toTreeInternal2(subNodes);
            		subNode.setParent(root);
            		subNode.setParentId(root.getId());
            		children.add(subNode);
            	}
                subNodes = new LinkedList<>();
            }
            //收集节点
            subNodes.addFirst(node);
        }
        //处理最后一个子树
        if(subNodes!=null){
        	T subNode = toTreeInternal2(subNodes);
        	subNode.setParent(root);
        	subNode.setParentId(root.getId());
        	children.add(subNode);
        }
        return root;
    }
	protected T toTreeInternal(LinkedList<T> nodes) {
    	//树的根节点
    	T root = nodes.removeLast();
    	//子树
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
        		if( last.getPath().length()>root.getPath().length()+PathNodeUtil.LEN+PathNodeUtil.PATH_SPLITER.length()){
        			subTreeNodeList.addFirst(nodes.removeLast());
        		}else{
        			break;
        		}
        	}
        }
        for(LinkedList<T> item:subTreesNodeList){//递归调用，设置关联
        	T subNode = toTreeInternal(item);
        	subNode.setParent(root);
        	subNode.setParentId(root.getId());
        	children.add(subNode);
        }
        return root;
    }

	@Override
	public T queryTree(String path) {
		List<T> nodes = queryByPathlike(path+"%");
        return toTree(nodes);
	}
	/**
	 * 根据父节点的path和已经存在的节点的path，计算获得未被使用的path。
	 * 这里的path是不带前后缀的path，其长度为PathNodeUtil.LEN
	 * @param parentPath
	 * @param brothers
	 * @param count
	 * @return
	 */
	protected List<String> uniquePath2(String parentPath, List<String> brothers, int count) {
		//超过最大节点数
		if (brothers.size() + count >= PathNodeUtil.MAX) {
			throw new RuntimeException("too much children");
		}
		//已存在的节点
		BitSet bitmap = new BitSet();
		for (String brotherPath : brothers) {
			bitmap.set(Integer.parseInt(brotherPath.substring(parentPath.length() + PathNodeUtil.PATH_SPLITER.length()),
					PathNodeUtil.RADIX));
		}
		List<String> paths = new ArrayList<>(count);
		for (int i = 0; i < PathNodeUtil.MAX; i++) {
			//还没有收集足够数量
			if (paths.size() < count) {
				//如果节点不存在，就收集
				if (!bitmap.get(i)) {
					String path = Integer.toString(i, PathNodeUtil.RADIX);
		            path =  PathNodeUtil.TEMPLATE.substring(path.length()) +path;
					paths.add(path);
				}
			} else {
				break;
			}
		}
		return paths;
	}
}
