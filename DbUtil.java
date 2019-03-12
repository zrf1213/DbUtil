package com.yeetrc.common.utils;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;
import com.mysql.jdbc.PreparedStatement;

public class DbUtil {
	private final static Logger logger = (Logger) LoggerFactory
			.getLogger(DbUtil.class);

	public static void main(String[] args) {
		List<File> filelist = new ArrayList<File>();
		refreshFileList(filelist, PathKit.getRootClassPath());
		for (File xmlfile : filelist) {
			System.out.println(xmlfile.getName());
		}
	}

	// 递归查找路径strPath下的所有sql.xml后缀的文件
	public static void refreshFileList(List<File> filelist, String strPath) {
		String filename;// 文件名
		File dir = new File(strPath);// 文件夹dir
		File[] files = dir.listFiles();// 文件夹下的所有文件或文件夹
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				refreshFileList(filelist, files[i].getAbsolutePath());// 递归文件夹！！！
			} else {
				filename = files[i].getName();
				if (filename.endsWith(".xml")
						&& !filename.equals("mybatis.xml"))// 判断是不是msml后缀的文件
				{
					filelist.add(files[i]);// 对于文件才把它的路径加到filelist中
				}
			}

		}
	}

	/*
	 * 查询-easyui树
	 */
	public static List<Map<String, Object>> findTree(List<Record> list) {
		List<Map<String, Object>> permissions = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < list.size(); i++) {
			Record p = list.get(i);
			if (p.get("parentId") == null || p.getInt("parentId") == 0) {
				List<Map<String, Object>> children = getChildren(list,
						p.get("id"));
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", p.get("id"));
				map.put("text", p.get("name"));
				map.put("name", p.get("name"));
				map.put("url", p.get("url"));
				map.put("iconCls", p.get("iconCls"));
				if (children != null && children.size() > 0) {
					map.put("children", children);
					map.put("state", "closed");
				}
				permissions.add(map);
			}
		}
		return permissions;
	}

	public static List<Map<String, Object>> getChildren(List<Record> list,
			Object parentId) {
		List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < list.size(); i++) {
			Record p = list.get(i);
			if (parentId.equals(p.get("parentId"))) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", p.get("id"));
				map.put("text", p.get("name"));
				map.put("name", p.get("name"));
				map.put("url", p.get("url"));
				map.put("iconCls", p.get("iconCls"));
				List<Map<String, Object>> c = getChildren(list, p.get("id"));
				if (c != null && c.size() > 0) {
					map.put("children", c);
					map.put("state", "closed");
				}
				children.add(map);
			}
		}
		return children;
	}

	public static void executeconnect(Connection conn, String sql,
			Object[] params, boolean isAutoSubmit) throws Exception {
		PreparedStatement prepareState = null;
		prepareState = (PreparedStatement) conn.prepareStatement(sql);
		for (int i = 0; i < params.length; i++) {
			Object value = params[i];
			prepareState.setObject(i + 1, value);
		}
		prepareState.execute();
		if (isAutoSubmit) {
			conn.commit();
		}
		if (!prepareState.isClosed())
			prepareState.close();
		if (!conn.isClosed())
			conn.close();
	}

	public static Connection getConnection() {
		Connection con = null;
		Properties properties = new Properties();
		InputStream is = DbUtil.class.getClassLoader().getResourceAsStream(
				"jdbcyunwei.properties");
		try {
			properties.load(is);
			String username = properties.getProperty("username");
			String password = properties.getProperty("password");
			String driver = properties.getProperty("driver");
			String url = properties.getProperty("url");
			Class.forName(driver);
			con = DriverManager.getConnection(url, username, password);
			con.setAutoCommit(false);
			con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

		} catch (Exception e) {

			logger.info(e.toString());
		}
		return con;
	}
}
