package cn.zhou.path.service;
import java.util.List;

public interface PathNodeService<T extends PathNode<T>>{

	/**
	 ** 删除子树
	 */
	int deleteTree(Long pid);

	/** 移动所有孩子节点到新的父节点 */
	int moveChildren(Long TId, Long targetPid);

	/** 获取子节点 */
	List<T> queryChildren(Long pid);

	/** 获取祖先节点 */
	List<T> queryAncestors(Long TId);

	/** 获取后代节点 */
	List<T> queryOffspring(Long TId);

	/** 批量添加节点 */
	int appendChildren(Long targetPid, List<T> Ts);

	/** 添加单个节点 */
	int appendChild(Long targetPid, T T);

	/** 列表转成一颗树 */
	T toTree(List<T> Ts);

	T queryTree(Long rootId);

	T queryRootPathNode();

}