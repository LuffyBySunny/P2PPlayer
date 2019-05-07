<%@page import="com.itheima.ck.bean.MovieDao"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.itheima.ck.bean.MovieBean"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<title>movieslist</title>
 <meta charset="utf-8"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <link rel="stylesheet" href="bootstrap-3.3.7-dist/css/bootstrap.css"/>
    <script type="text/javascript" src="jquery-1.11.3/jquery.js"></script>
    <script type="text/javascript" src="bootstrap-3.3.7-dist/js/bootstrap.js"></script>

</head>
<body>
<h1 align="center">妙蛙种子上传系统</h1>
<ul class="nav nav-tabs">
  <li role="presentation"><a href="index.jsp">上传</a></li>
  <li role="presentation"class="active" ><a href="#">查看</a></li>
</ul>

  
  <h2  align="center">电影信息</h2>
  <div class="row" style="margin:25px">
  	<%
  	//获取电影信息
  	List<MovieBean> list = MovieDao.getInstance().queryAll();
  	if(list.isEmpty()) {
  		out.print("没有数据");
  	}else {
  		for(MovieBean bean:list) {
  	%>	
  <div style="width:250px;display:inline-block;margin:40px" >
    <div class="thumbnail" >
     <img alt="图片暂时无法加载" src="<%=bean.imagePathString%>" style="height:360px;width:240px">
     <div class="caption"  style="height:200px; overflow:auto">
        <h4>电影名称：<%=bean.name%></h4>
        <p>电影描述: <%=bean.details %></p>
        <a href="<%=bean.torrentpathString%>">
         种子链接
        </a>
     </div>
     </div>
   </div>
     <% 
  	}
  	}
  	%>
  </div>
  	
</div>
</body>
</html>