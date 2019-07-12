import java.util.List;

public interface PathNodeService<T extends PathNode<T>>{

	/**
	 ** 删除子树
	 */
	int deleteTree(String path);

	int moveTree(String path,String toPath);

	/** 获取子节点 */
	List<T> queryChildren(String path);

	/** 获取祖先节点 */
	List<T> queryAncestors(String path);

	/** 获取后代节点 */
	List<T> queryOffspring(String path);

	/** 批量添加节点 */
	int appendChildren(String path, List<T> nodes);

	/** 添加单个节点 */
	int appendChild(String path, T node);

	/** 列表转成一颗树 */
	T toTree(List<T> nodes);
	
	T queryTree(String path);

	T queryRootNode();

}