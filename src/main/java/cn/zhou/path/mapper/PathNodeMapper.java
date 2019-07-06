package cn.zhou.path.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.zhou.path.model.PathNodeModel;
import cn.zhou.path.service.PathNode;
@Table(name="path_node")
public interface PathNodeMapper extends BaseMapper<PathNodeModel,PathNodeExample,Long>{

	int moveNode(@Param("oldParentPath")String oldParentPath,@Param("newParentPath")String newParentPath);

    List<PathNodeModel> ancestors(@Param("currentPath")String path);
	
}