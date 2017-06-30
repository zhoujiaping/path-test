package cn.zhou.path.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.howso.mybatis.anno.Table;
import cn.howso.mybatis.mapper.BaseMapper;
import cn.zhou.path.model.PathNode;
import cn.zhou.path.model.PathNodeExample;
@Table(name="path_node")
public interface PathNodeMapper extends BaseMapper<PathNode,PathNodeExample,Long>{

	int moveNode(@Param("oldParentPath")String oldParentPath,@Param("newParentPath")String newParentPath);

    List<PathNode> ancestors(@Param("currentPath")String path);
	
}