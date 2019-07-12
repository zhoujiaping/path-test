import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sfpay.asp.common.test.dbclient.DbClient;
import com.sfpay.asp.front.DbClientHolder;

public class IndustryServiceImpl extends AbsPathNodeService<Industry>{
	private DbClient client = DbClientHolder.getClient();
	private Class<Industry> clazz = Industry.class;
	@Override
	protected Industry queryByPath(String path) {
		return client.queryUnique("select * from t_industry where path=#{}", clazz, path);
	}

	@Override
	protected int deleteByPathlike(String pathlike) {
		return client.update("delete from t_industry where path like #{}", pathlike);
	}

	@Override
	protected List<Industry> queryByPathlike(String pathlike) {
		return client.query("select * from t_industry where path like #{}", clazz, pathlike);
	}

	@Override
	protected List<String> queryPathByPathlike(String pathlike) {
		List<Industry> nodes = client.query("select path from t_industry where path like #{}", clazz, pathlike);
		return nodes.stream().map(Industry::getPath).collect(Collectors.toList());
	}

	@Override
	protected int insertNodes(List<Industry> nodes) {
		String sql = "insert into t_industry(path,name,code,parent_id)values(#{},#{},#{},#{})";
		nodes.forEach(node->{
			Long id = client.insert(sql, node.getPath(),node.getName(),node.getCode(),node.getParentId());
			node.setId(id);
		});
		return nodes.size();
	}

	@Override
	public Industry queryRootNode() {
		return queryByPath("");
	}

	@Override
	protected int updatePath(String oldPath,String newpath) {
		String sql = "update t_industry set path = concat('${}',right(path,length(path)-length('${}'))) where path like #{oldPathlike}";
		return client.update(sql,oldPath+"%",newpath,oldPath);
	}

	@Override
	public List<Industry> queryAncestors(String path) {
		String sql = "select * from t_industry where path in";
		String[] parts = path.split("/");
		Object[] paths = new Object[parts.length];
		String pre = "";
		for(int i=0;i<parts.length;i++){
			paths[i] = pre+parts[i];
			pre = pre+parts[i]+"/";
		}
		String values = Stream.of(parts).map(part->"#{}").collect(Collectors.joining(",", "(", ")"));
		return client.query(sql+values, clazz, paths);
	}

}
