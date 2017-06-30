package test.mapper;

import org.apache.ibatis.annotations.Param;

import cn.howso.mybatis.anno.Table;
import cn.howso.mybatis.mapper.BaseMapper;
import test.model.PathNode;
import test.model.PathNodeExample;
@Table(name="path_node")
public interface PathNodeMapper extends BaseMapper<PathNode,PathNodeExample,Long>{

	int moveNode(@Param("newParentPath")String newParentPath, @Param("node")PathNode node);
	
}