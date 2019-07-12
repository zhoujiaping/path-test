import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.sfpay.asp.common.test.dbclient.DbClient;
import com.sfpay.asp.common.tools.CollectionUtil;
import com.sfpay.asp.front.DbClientHolder;

public class IndustryServiceImplTest {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private DbClient client = DbClientHolder.getClient();

	private IndustryServiceImpl industryService = new IndustryServiceImpl();
	@Test
	public void test1(){
		Industry node = new Industry();
		node.setName("农、林、牧、渔业");
		industryService.appendChild("", node );
		logger.info(JSON.toJSONString(node));
	}
	@Test
	public void test2(){
		List<Industry> list = client.query("select item_name as name, "
				+ " item_code as code, "
				+ " item_id as id, "
				+ " parent_item_id as parent_id"
				+ " from t_code_item where category_id = #{} and parent_item_id=#{}", Industry.class, -10004,0);
		logger.info(JSON.toJSONString(list));
	}
	/**
	 * 测试数据
	 * 根节点 ""
	 * 节点    
	 * /musi
	 * /musi/popm
	 * /musi/rock	
	 * /musi/popm/jayz
	 * /musi/popm/wtwt
	 * /pict
	 *  /pict/home
	 *   /pict/home/flow
	 *   /pict/home/pers          
	 *  /pict/comp
	 *  /movi
	 *  /movi/marr
	 *  /movi/part
	 */
	@Test
	public void test3(){
		client.update("truncate table t_industry");
		client.insert("insert into t_industry(path,name)values('','行业');");

		String rootPath = "";
		Industry music = new Industry("音乐", "music");
		industryService.appendChild(rootPath, music );
		Industry rock = new Industry("摇滚", "rock");
		industryService.appendChild(music.getPath(), rock );
		Industry hip = new Industry("嘻哈", "hip");
		industryService.appendChild(music.getPath(), hip );
		Industry jayz = new Industry("周", "jayz");
		industryService.appendChild(hip.getPath(), jayz );
		Industry wt = new Industry("诱惑本质", "wt");
		industryService.appendChild(hip.getPath(), wt );
		
		Industry pictrue = new Industry("图片", "pictrue");
		industryService.appendChild(rootPath, pictrue );
		Industry home = new Industry("家", "home");
		industryService.appendChild(pictrue.getPath(), home );
		Industry company = new Industry("公司", "company");
		industryService.appendChild(pictrue.getPath(), company );
		Industry flower = new Industry("花", "flower");
		industryService.appendChild(home.getPath(), flower );
		Industry person = new Industry("人", "person");
		industryService.appendChild(home.getPath(), person );
		
		Industry movie = new Industry("电影", "movie");
		industryService.appendChild(rootPath, movie );
		Industry marry = new Industry("结婚", "marry");
		industryService.appendChild(movie.getPath(), marry );
		Industry party = new Industry("聚会", "party");
		industryService.appendChild(movie.getPath(), party );
	}
	@Test
	public void test4(){
		Industry punk = new Industry("朋克", "punk");
		Industry opera = new Industry("歌剧", "opera");
		industryService.appendChildren("/000", CollectionUtil.asList(punk,opera));
	}
	@Test
	public void test5(){
		industryService.deleteTree("/000");
	}
	@Test
	public void test6(){
		industryService.moveTree("/000/001", "/002/001");
	}
	@Test
	public void test7(){
		List<Industry> nodes = industryService.queryAncestors("/001/000/001");
		logger.info(JSON.toJSONString(nodes,true));
	}
	@Test
	public void test8(){
		List<Industry> nodes = industryService.queryChildren("/001");
		logger.info(JSON.toJSONString(nodes,true));
	}
	@Test
	public void test9(){
		List<Industry> nodes = industryService.queryOffspring("/001");
		logger.info(JSON.toJSONString(nodes,true));
	}
	@Test
	public void test10(){
		Industry root = industryService.queryRootNode();
		logger.info(JSON.toJSONString(root,true));
	}
	@Test
	public void test11(){
		Industry pictrue = industryService.queryTree("/001");
		logger.info(JSON.toJSONString(pictrue,true));
	}
	@Test
	public void test12(){
	}
}
