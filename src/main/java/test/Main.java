package test;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.constant.PathConst;
import test.mapper.PathNodeMapper;
import test.model.PathNode;
import test.model.PathNodeExample;
import test.service.PathNodeService;

public class Main {
	private static String resource = "configuration.xml";
	private static SqlSessionFactory sqlSessionFactory;
	public static int RADIX = 36;
	public static int PATH_LEN = 3;

	@BeforeClass
	public static void beforeClass() throws IOException {
		/*
		 * 1、建库，执行init-test.sql 2、执行mybatis-generator:generate，拷贝文件到项目对应位置
		 * 3、执行测试
		 */
		Reader reader = Resources.getResourceAsReader(resource);
		XMLConfigBuilder xMLConfigBuilder = new XMLConfigBuilder(reader);
		Configuration configuration = xMLConfigBuilder.parse();
		sqlSessionFactory = new DefaultSqlSessionFactory(configuration);
		// SqlSessionFactory sqlSessionFactory = new
		// SqlSessionFactoryBuilder().build(reader);
	}

	@AfterClass
	public static void afterClass() {
	}

	@Test
	public void test() {
		SqlSession session = sqlSessionFactory.openSession();
		PathNodeMapper mapper = session.getMapper(PathNodeMapper.class);
		PathNodeService service = new PathNodeService();
		service.setMapper(mapper);
		service.insert("", "湖南省", 1);
		session.commit();
	}

	public void testInsertOne(String parentPath, String name, int seq) {

	}
}
