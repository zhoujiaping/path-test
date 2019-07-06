package cn.zhou.path.service;
import java.util.List;

import cn.zhou.path.model.PathNodeModel;

public class PathNodeServiceImpl extends AbsPathNodeService<PathNodeModel>{

	@Override
	public PathNodeModel queryRootPathNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PathNodeModel queryById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int deleteByPathlike(String pathlike) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected List<PathNodeModel> queryByPathlike(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int insertNodes(List<PathNodeModel> nodes) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public PathNodeModel queryRootNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int batchUpdate(List<PathNodeModel> nodes) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<PathNodeModel> queryAncestors(Long nodeId) {
		// TODO Auto-generated method stub
		return null;
	}



}
