package dbclient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sfpay.asp.common.test.dbclient.Callback.Callback00;
import com.sfpay.asp.common.test.dbclient.Callback.Callback01;
import com.sfpay.asp.common.test.dbclient.Callback.Callback10;
import com.sfpay.asp.common.test.dbclient.Callback.Callback11;

public class DbClient {
	private static final List<Object> emptyObjectList = new ArrayList<>(0);
	private static final Logger logger = LoggerFactory.getLogger(DbClient.class);
	private ThreadLocal<Stack<Connection>> connStackHolder = new ThreadLocal<>();
	private DataSource ds;

	public DbClient(DataSource ds) {
		this.ds = ds;
	}

	private Connection openConn() throws SQLException {
		Connection conn = ds.getConnection();
		conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		if (conn.getAutoCommit()) {
			conn.setAutoCommit(false);
		}
		return conn;
	}

	public void withConn(Callback10<Connection> callback) {
		withConn((conn) -> {
			callback.apply(conn);
			return null;
		});
	}

	public <R> R withConn(Callback11<R, Connection> callback) {
		Connection conn = null;
		try {
			Stack<Connection> stack = connStackHolder.get();
			if (stack == null) {
				stack = new Stack<>();
				connStackHolder.set(stack);
			}
			conn = openConn();
			logger.debug("线程{}已打开{}个连接", Thread.currentThread().getName(), stack.size());
			stack.push(conn);
			R res = callback.apply(conn);
			commit(conn);
			return res;
		} catch (Exception e) {
			rollback(conn);
			throw new RuntimeException(e);
		} finally {
			Stack<Connection> stack = connStackHolder.get();
			if (stack != null && !stack.isEmpty()) {
				if (stack.peek() == conn) {
					stack.pop();
				}
				logger.debug("线程{}还有{}个连接", Thread.currentThread().getName(), stack.size());
			} else {
				logger.debug("线程{}还有{}个连接", Thread.currentThread().getName(), 0);
			}
			close(conn);
		}
	}

	private void commit(Connection conn) {
		if (conn != null) {
			try {
				if (!conn.isClosed() && !conn.getAutoCommit()) {
					conn.commit();
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void close(Connection conn) {
		if (conn != null) {
			try {
				if (!conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void rollback(Connection conn) {
		if (conn != null) {
			try {
				if (!conn.isClosed() && !conn.getAutoCommit()) {
					conn.rollback();
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void withTx(Callback00 callback) {
		withTx(() -> {
			callback.apply();
			return null;
		});
	}

	private <T> T withTxInternal(Callback01<T> callback) {
		Stack<Connection> stack = connStackHolder.get();
		Connection conn = stack.peek();
		try {
			T res = callback.apply();
			commit(conn);
			return res;
		} catch (Exception e) {
			rollback(conn);
			throw new RuntimeException(e);
		}
	}

	public <T> T withTx(Callback01<T> callback) {
		if (hasConn()) {
			return withTxInternal(callback);
		}
		return withConn(conn -> {
			return withTxInternal(callback);
		});
	}

	private boolean hasConn() {
		Stack<Connection> stack = connStackHolder.get();
		return stack != null && !stack.isEmpty();
	}

	public <T> T queryUnique(String sql, Class<T> clazz, Object... params) {
		Map<String, Object> map = queryUniqueMap(sql, params);
		JSONObject json = (JSONObject) JSON.toJSON(map);
		return json.toJavaObject(clazz);
	}

	public Map<String, Object> queryUniqueMap(String sql, Object... params) {
		List<Map<String, Object>> array = queryMap(sql, params);
		int size = array.size();
		if (size == 0) {
			return null;
		} else if (size > 1) {
			throw new RuntimeException("查询结果" + size + "条，预期1条");
		}
		return array.get(0);
	}

	public <T> T queryUniqueByMap(String sql, Class<T> clazz, Map<String, ?> params) {
		Map<String, Object> map = queryUniqueMapByMap(sql, params);
		JSONObject json = (JSONObject) JSON.toJSON(map);
		return json.toJavaObject(clazz);
	}

	public Map<String, Object> queryUniqueMapByMap(String sql, Map<String, ?> params) {
		List<Map<String, Object>> array = queryMapByMap(sql, params);
		int size = array.size();
		if (size == 0) {
			return null;
		} else if (size > 1) {
			throw new RuntimeException("查询结果" + size + "条，预期1条");
		}
		return array.get(0);
	}

	public <T> List<T> query(String sql, Class<T> clazz, Object... params) {
		List<Map<String, Object>> list = queryMap(sql, params);
		JSONArray array = (JSONArray) JSON.toJSON(list);
		return array.toJavaList(clazz);
	}

	public List<Map<String, Object>> queryMap(String sql, Object... params) {
		if (hasConn()) {
			return queryMapInternal(sql, params);
		}
		return withConn(conn -> {
			return queryMapInternal(sql, params);
		});
	}

	private List<Map<String, Object>> queryMapInternal(String sql, Object... params) {
		SqlPlaceHolder holder = parseSqlIndexedPlaceHolder(sql, params);
		return queryMap0(holder);
	}

	public <T> List<T> queryByMap(String sql, Class<T> clazz, Map<String, ?> params) {
		List<Map<String, Object>> list = queryMapByMap(sql, params);
		JSONArray array = (JSONArray) JSON.toJSON(list);
		return array.toJavaList(clazz);
	}

	public List<Map<String, Object>> queryMapByMap(String sql, Map<String, ?> params) {
		if (hasConn()) {
			return queryMapByMapInternal(sql, params);
		}
		return withConn(conn -> {
			return queryMapByMapInternal(sql, params);
		});
	}

	private List<Map<String, Object>> queryMapByMapInternal(String sql, Map<String, ?> params) {
		SqlPlaceHolder holder = parseSqlNamedPlaceHolder(sql, params);
		return queryMap0(holder);
	}

	private List<Map<String, Object>> queryMap0(SqlPlaceHolder holder) {
		logger.info("sql=>{}", holder.sql);
		logger.info("params=>{}", holder.strValues);
		Stack<Connection> stack = connStackHolder.get();
		Connection conn = stack.peek();
		try (PreparedStatement ps = conn.prepareStatement(holder.sql);) {
			int i0 = 1;
			for (Object value : holder.values) {
				ps.setObject(i0++, value);
			}
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int cc = rsmd.getColumnCount();
			List<Map<String, Object>> list = new ArrayList<>();
			while (rs.next()) {
				Map<String, Object> map = new HashMap<>();
				for (int i = 1; i <= cc; i++) {
					String label = rsmd.getColumnLabel(i);
					String key = Beans.underline2camel(label);
					Object value = rs.getObject(label);
					map.put(key, value);
				}
				list.add(map);
			}
			rs.close();
			logger.info("result size=>{}", list.size());
			return list;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private SqlPlaceHolder parseSqlIndexedPlaceHolder(String sql, Object... params) {
		SqlPlaceHolder res = new SqlPlaceHolder();
		if (params == null) {
			res.sql = sql;
			return res;
		}
		List<Object> values = new ArrayList<>();
		Int index = new Int(0);
		sql = new GenericTokenParser("#{", "}", (token) -> {
			values.add(params[index.getAndInc()]);
			return "?";
		}).parse(sql);
		sql = new GenericTokenParser("${", "}", (token) -> {
			String trimedToken = token.trim();
			if (trimedToken.equals("")) {
				return String.valueOf(params[index.getAndInc()]);
			} else {
				return String.valueOf(params[Integer.parseInt(trimedToken)]);
			}
		}).parse(sql);
		res.sql = sql;
		res.values = values;
		res.strValues = values.stream().map(v -> String.valueOf(v)).collect(Collectors.joining(", "));
		return res;
	}

	private static class Int {
		int value;

		public Int(int value) {
			this.value = value;
		}

		public int getAndInc() {
			int v = value;
			this.value++;
			return v;
		}
	}

	private SqlPlaceHolder parseSqlNamedPlaceHolder(String sql, Map<String, ?> params) {
		SqlPlaceHolder res = new SqlPlaceHolder();
		if (params == null) {
			res.sql = sql;
			return res;
		}
		List<Object> values = new ArrayList<>();
		sql = new GenericTokenParser("#{", "}", (token) -> {
			values.add(params.get(token.trim()));
			return "?";
		}).parse(sql);
		sql = new GenericTokenParser("${", "}", (token) -> {
			return String.valueOf(params.get(token.trim()));
		}).parse(sql);
		res.sql = sql;
		res.values = values;
		res.strValues = values.stream().map(v -> String.valueOf(v)).collect(Collectors.joining(", "));
		return res;
	}

	private static class SqlPlaceHolder {
		private String sql;
		public List<Object> values = emptyObjectList;
		public String strValues = "";
	}

	public int update(String sql, Object... params) {
		if (hasConn()) {
			return updateInternal(sql, params);
		}
		return withConn(conn -> {
			return updateInternal(sql, params);
		});
	}

	private int updateInternal(String sql, Object... params) {
		SqlPlaceHolder holder = parseSqlIndexedPlaceHolder(sql, params);
		return update0(holder);
	}

	private int update0(SqlPlaceHolder holder) {
		logger.info("sql=>{}", holder.sql);
		logger.info("params=>{}", holder.strValues);
		Stack<Connection> stack = connStackHolder.get();
		Connection conn = stack.peek();
		try (PreparedStatement ps = conn.prepareStatement(holder.sql)) {
			int i0 = 1;
			for (Object value : holder.values) {
				ps.setObject(i0++, value);
			}
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public int updateByMap(String sql, Map<String, ?> params) {
		if (hasConn()) {
			return updateByMapInternal(sql, params);
		}
		return withConn(conn -> {
			return updateByMapInternal(sql, params);
		});
	}

	public <T> T insert(String sql, Object... params) {
		if (hasConn()) {
			return insertInternal(sql, params);
		}
		return withConn(conn -> {
			return insertInternal(sql, params);
		});
	}

	private <T> T insertInternal(String sql, Object... params) {
		SqlPlaceHolder holder = parseSqlIndexedPlaceHolder(sql, params);
		return insert0(holder);
	}

	public <T> T insertByMap(String sql, Map<String, Object> params) {
		if (hasConn()) {
			return insertByMapInternal(sql, params);
		}
		return withConn(conn -> {
			return insertByMapInternal(sql, params);
		});
	}

	private <T> T insertByMapInternal(String sql, Map<String, Object> params) {
		SqlPlaceHolder holder = parseSqlNamedPlaceHolder(sql, params);
		return insert0(holder);
	}

	private <T> T insert0(SqlPlaceHolder holder) {
		logger.info("sql=>{}", holder.sql);
		logger.info("params=>{}", holder.strValues);
		Stack<Connection> stack = connStackHolder.get();
		Connection conn = stack.peek();
		try (PreparedStatement ps = conn.prepareStatement(holder.sql, Statement.RETURN_GENERATED_KEYS)) {
			int i0 = 1;
			for (Object value : holder.values) {
				ps.setObject(i0++, value);
			}
			ps.executeUpdate();
			ResultSet generatedKeys = ps.getGeneratedKeys();
			generatedKeys.next();
			@SuppressWarnings("unchecked")
			T id = (T) generatedKeys.getObject(1);
			if (id == null) {
				throw new RuntimeException("自动生成主键为空");
			}
			return id;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private int updateByMapInternal(String sql, Map<String, ?> params) {
		SqlPlaceHolder holder = parseSqlNamedPlaceHolder(sql, params);
		return update0(holder);
	}
}
