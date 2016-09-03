<%@ page language="java" contentType="text/html; charset=UTF-8"  
    pageEncoding="UTF-8"%>  
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">  
<html>  
<head>  
<script type="text/javascript" src="js/jquery-1.7.2.js"></script>  
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">  
<title>Insert title here</title>  
</head>  
<body>  
  
  	<form:form action="convert.do" method="post" modelAttribute="testConvert">
  		<form:input path="birthday"/>
        <input type="submit" value="sub" >  
  	</form:form>
    <br>
</body>  
</html>