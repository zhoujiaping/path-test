package cn.zhou.path.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import cn.zhou.path.constant.PathConst;
import cn.zhou.path.mapper.PathNodeMapper;
import cn.zhou.path.model.PathNode;
import cn.zhou.path.model.PathNodeExample;
@Service
public class PathNodeService {
    @Resource
    private PathNodeMapper mapper;

    /*
     * 为了方便，程序可以统一处理，path统一以'-'开头，这虽然不是必须的，但有分隔符可以增强可读性。 查询顶级节点的子节点，parentPath传"" 根节点的path为""。
     */
    private int batchInsertInternal(String parentPath, List<PathNode> nodes) {
        PathNodeExample example = new PathNodeExample();
        example.createCriteria().andPathLike(parentPath + PathConst.PREFIX + PathConst.MATCHER);
        List<PathNode> children = mapper.selectByExample(example);
        List<String> paths = uniquePath(parentPath, children, nodes.size());
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).setPath(paths.get(i));
        }
        return mapper.batchInsertSelective(nodes);
    }

    /**
     * @Description 删除子树
     * @param path
     * @return
     */
    public int delete(long pid) {
        PathNode parent = mapper.selectByPrimaryKey(pid);
        PathNodeExample example = new PathNodeExample();
        example.createCriteria().andPathLike(parent.getPath() + "%");
        return mapper.deleteByExample(example);
    }

    /**
     * @Description 更新节点
     * @param node
     * @return
     */
    public int update(PathNode node) {
        return mapper.updateByPrimaryKeySelective(node);
    }

    /** 移动一棵树 */
    public int move(PathNode node, long pid) {
        PathNode parentNode = mapper.selectByPrimaryKey(pid);
        String newParentPath = parentNode.getPath();
        String oldParentPath = parentPath(node);
        return mapper.moveNode(oldParentPath, newParentPath);
    }

    /** 获取子节点 */
    public List<PathNode> children(long pid) {
        PathNode parentNode = mapper.selectByPrimaryKey(pid);
        String parentPath = parentNode.getPath();
        PathNodeExample example = new PathNodeExample();
        example.createCriteria().andPathLike(parentPath + PathConst.PREFIX + PathConst.MATCHER);
        List<PathNode> children = mapper.selectByExample(example);
        return children;
    }

    /** 获取祖先节点 */
    public List<PathNode> ancestors(long id) {
        PathNode node = mapper.selectByPrimaryKey(id);
        String path = node.getPath();
        List<PathNode> ancestors = mapper.ancestors(path);
        return ancestors;
    }

    /** 获取后代节点 */
    public List<PathNode> offspring(PathNode node) {
        String path = node.getPath();
        PathNodeExample example = new PathNodeExample();
        example.createCriteria().andPathLike(path + PathConst.PREFIX + "%");
        List<PathNode> offSpring = mapper.selectByExample(example);
        return offSpring;
    }

    private String parentPath(PathNode node) {
        String path = node.getPath();
        return path.substring(0, path.lastIndexOf(PathConst.PREFIX));
    }

    private List<String> uniquePath(String parentPath, Collection<PathNode> children, int count) {
        // 经过测试，该方法性能极高。获取1w个path的执行时间也可以忽略不计，才3毫秒。
        // 不过如果用到stream，没有并行，都要花44毫秒左右。所以stream启动成本高，比较适合用在需要并行计算的地方。
        // 因为pg的事务隔离级别默认为read commited，所以在并发情况下，该机制可能导致失败。
        // 数据库最好加给path字段加约束，保证数据的正确性。
        //可以使用bitmap，进一步提升性能和可读性。
        // long start = System.currentTimeMillis();
        if (children.size() + count >= PathConst.MAX) {
            throw new RuntimeException("too much children");
        }
        Set<String> existPaths = new HashSet<>();
        for (PathNode node : children) {
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
        // long end = System.currentTimeMillis();
        // System.out.println("time cost = "+(end-start));
        return paths;
    }
    /**bitmap版本*/
    private List<String> uniquePath2(String parentPath, Collection<PathNode> children, int count) {
        if (children.size() + count >= PathConst.MAX) {
            throw new RuntimeException("too much children");
        }
        BitSet bitmap = new BitSet();
        for(PathNode node:children){
            bitmap.set(Integer.parseInt(node.getPath().substring(parentPath.length()-PathConst.PREFIX.length()), PathConst.RADIX));
        }
        List<String> paths = new ArrayList<>(count);
        for(int i=0;i<PathConst.MAX;i++){
            if(paths.size()<count){
                String path = Integer.toString(i, PathConst.RADIX);
                path = PathConst.PREFIX + PathConst.TEMPLATE.substring(path.length()) + path;
                paths.add(path);
            }else{
                break;
            }
        }
        return paths;
    }

    /** 批量添加节点 */
    public int batchInsert(long pid, List<PathNode> nodes) {
        PathNode parent = mapper.selectByPrimaryKey(pid);
        return batchInsertInternal(parent.getPath(), nodes);
    }

    /** 添加单个节点 */
    public int insert(long pid, PathNode node) {
        PathNode parent = mapper.selectByPrimaryKey(pid);
        return batchInsertInternal(parent.getPath(), Arrays.asList(new PathNode[] {node }));
    }

    /** 列表转成一颗树 */
    public PathNode toTree(List<PathNode> nodes) {
        Assert.isTrue(!nodes.isEmpty(), "nodes should not be empty");
        LinkedList<PathNode> dest = new LinkedList<>();
        dest.addAll(nodes);
        dest.sort((o1, o2) -> o2.getPath().compareTo(o1.getPath()));//按字母逆序排序
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
    private PathNode toTreeInternal(LinkedList<PathNode> nodes) {
    	//树的根节点
        PathNode root = nodes.removeLast();
        //子树集合
        List<PathNode> children = new ArrayList<>();
        root.setChildren(children);
        LinkedList<PathNode> subNodes = null;
        while(!nodes.isEmpty()){
            PathNode node = nodes.removeLast();
            if(node.getPath().length()==root.getPath().length()+PathConst.PREFIX.length()+PathConst.LEN){
                if(subNodes!=null){
                    PathNode subNode = toTreeInternal(subNodes);
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
        	PathNode subNode = toTreeInternal(subNodes);
            subNode.setParent(root);
            children.add(subNode);
        }
        for(PathNode child:children){
        	child.setParentId(child.getParent().getId());
        }
        return root;
    }
    /**这里用linkedList好些，因为数据量大些时，也不会出现频繁拷贝数组的情况*/
    private PathNode toTreeInternal2(LinkedList<PathNode> nodes) {
    	//树的根节点
        PathNode root = nodes.removeLast();
        List<PathNode> children = new ArrayList<>();
        root.setChildren(children);
        //节点按子树分组
        LinkedList<LinkedList<PathNode>> subTreesNodeList = new LinkedList<>();
        LinkedList<PathNode> subTreeNodeList;
        while(!nodes.isEmpty()){//每一次循环收集一颗子树的所有节点到一个新的集合
        	PathNode node = nodes.removeLast();
        	subTreeNodeList = new LinkedList<>();//某个子树所有的节点
        	subTreesNodeList.add(subTreeNodeList);
        	subTreeNodeList.addFirst(node);
        	while(!nodes.isEmpty()){
        		PathNode last = nodes.getLast();
        		if( last.getPath().length()>root.getPath().length()+PathConst.PREFIX.length()+PathConst.LEN){
        			subTreeNodeList.addFirst(nodes.removeLast());
        		}else{
        			break;
        		}
        	}
        }
        for(LinkedList<PathNode> item:subTreesNodeList){//递归调用，设置关联
        	PathNode subNode = toTreeInternal2(item);
        	subNode.setParent(root);
        	subNode.setParentId(root.getId());
        	children.add(subNode);
        }
        return root;
    }
    public PathNode queryTree(Long rootId) {
        PathNode node = null;
        if(rootId==null){
            node = queryRootPathNode();
        }else{
            node = mapper.selectByPrimaryKey(rootId);
        }
        List<PathNode> list = offspring(node);
        list.add(node);
        return toTree(list);
    }
    public PathNode queryRootPathNode(){
        PathNodeExample example = new PathNodeExample();
        example.createCriteria().andPathEqualTo("");
        List<PathNode> list = mapper.selectByExample(example);
        Assert.isTrue(list.size()==1,"root path node not found");
        return list.get(0);
    }
}
