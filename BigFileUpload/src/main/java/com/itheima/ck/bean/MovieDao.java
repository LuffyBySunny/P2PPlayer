package com.itheima.ck.bean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovieDao {
	private Connection connection;
	private MovieDao(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static class MovieDaoHolder{
		public static final MovieDao INSTANCE_DAO = new MovieDao();
	}
	
	public static MovieDao getInstance() {
		return MovieDaoHolder.INSTANCE_DAO;
	}
	
	public Connection getConnection() throws SQLException{
		if (connection == null) {
			String dbUrl = "jdbc:mysql://localhost:3306/movies?useSSL=false";
			String username = "root";
			String password = "1234";
			connection = DriverManager.getConnection(dbUrl, username, password);
		}
		
		return connection;
	}

	//插入电影
	
	public void addMovie(MovieBean movie)throws SQLException {
		String sqlString = "insert into movie(name,details,datasourcePath,imagePathString,torrentpathString) values(?,?,?,?,?)";
		PreparedStatement preparedStatement = getConnection().prepareStatement(sqlString);
		preparedStatement.setString(1, movie.name);
		preparedStatement.setString(2, movie.details);
		preparedStatement.setString(3, movie.datasourcePath);
		preparedStatement.setString(4, movie.imagePathString);
		preparedStatement.setString(5,movie.torrentpathString);
		preparedStatement.execute();
		preparedStatement.close();
	}
	//查询是否已经存在
	public boolean ifExsts(String movieName) throws SQLException {
		String searchString = "select * from movie where name=?";
		PreparedStatement statement = getConnection().prepareStatement(searchString);
		statement.setString(1, movieName);
		ResultSet resultSet = statement.executeQuery();
		return resultSet.next();
	}
	//查询所有电影
	public List<MovieBean> queryAll() {
		List<MovieBean> list = new ArrayList<MovieBean>();
		String search = "select * from movie";
		try {
			PreparedStatement statement = getConnection().prepareStatement(search);
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()) {
				MovieBean movieBean = new MovieBean();
				movieBean.name = resultSet.getString("name");
				movieBean.details = resultSet.getString("details");
				movieBean.datasourcePath = resultSet.getString("datasourcePath");
				movieBean.imagePathString = resultSet.getString("imagePathString");
				movieBean.torrentpathString = resultSet.getString("torrentpathString");
				list.add(movieBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
		
	}
	
	//查询电影,按匹配度
	public List<MovieBean> query(String name) throws SQLException{
		List<MovieBean> list = new ArrayList<MovieBean>();
		String searchString = "select * from movie where name like ? order by replace(name,?,?) limit 10";
		PreparedStatement statement = getConnection().prepareStatement(searchString);
		statement.setString(1, "%" + name + "%");
		statement.setString(2, name);
		statement.setString(3, "");
		ResultSet resultSet = statement.executeQuery();
		while(resultSet.next()) {
			MovieBean movieBean = new MovieBean();
			movieBean.name = resultSet.getString("name");
			movieBean.details = resultSet.getString("details");
			movieBean.datasourcePath = resultSet.getString("datasourcePath");
			movieBean.imagePathString = resultSet.getString("imagePathString");
			movieBean.torrentpathString = resultSet.getString("torrentpathString");
			list.add(movieBean);
			
		}
		
		statement.close();
		return list;
	}
	
	
	public void CreateTable() throws SQLException{
		 String creatsql = "CREATE TABLE IF NOT EXISTS movie(" + "name varchar(1000) not null  PRIMARY KEY,"
		           + "details text," 
				   + "datasourcePath text," 
		           + "imagePathString text," 
				   + "torrentpathString text,"
				   + "INDEX nameindex(name(1000))"
		           + ")charset=utf8";
		 PreparedStatement statement = getConnection().prepareStatement(creatsql);
		 statement.execute();
		 
	}
	
	
	
}
