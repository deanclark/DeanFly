<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>

    <h1>DeanFly 3D</h1>
    
    <%--
    This example uses JSTL, uncomment the taglib directive above.
    To test, display the page like this: index.jsp?sayHello=true&name=Murphy
    --%>
    <%--
    <c:if test="${param.sayHello}">
        <!-- Let's welcome the user ${param.name} -->
        Hello ${param.name}!
    </c:if>
    --%>

    <div class=Section1>
	<p><font face="Verdana, Arial, Helvetica, sans-serif" size="3"><font color="#1847FF" size="5"><font size="3">
    <b>	<!--DeanFly-->
    </b>
    </font></font></font>
    </p>
    <div class=MsoNormal align=center style=text-align:center>
    </div>
    <p class=MsoNormal>

    <!-- <applet codebase="." code="DeanFly.class" width="900" height="500"> -->
     <applet codebase="." code="DeanFly.class" width="900" height="500"> 
<!--    <applet codebase="." code="http://localhost:8084/DeanFly/classes/DeanFly.class" width="900" height="500"> -->

    <!-- verbose parameter should be "false" for normal operation, if "true" only 3 objects will be loaded -->
    <param name=verbose value="false">
    <param name=world value="islandWorld">
    <!--<param name=homeServerPath value="http://127.0.0.1/deanfly/">-->
    <param name=homeServerPath value="http://dean.seetech.com/DeanFly/">
<!--    <param name=homeServerPath value="http://localhost:8084/DeanFly/"> --> <!-- Netbeans 4.1 -->
<!--    <param name=homeServerPath value="http://localhost:8084/DeanFly/"> -->
<!--    <param name=homeServerPath value="http://127.0.0.1/DeanFlyJava/"> -->
    <!--<param name=homeServerPath value="http://localhost/DeanFlyJava/">-->
    <!--<param name=homeServerPath value="http://www.ecid.cig.mot.com:80/~declark1/DeanFlyJava/">-->
    <!--<param name=homeServerPath value="http://www.ecid.cig.mot.com:80/~declark1/DeanFly/">-->
    <!--<param name=homeServerPath value="http://ZUK28-1243.ecid.cig.mot.com/DeanFlyJava/">-->
    <!--<param name=homeServerPath value="http://localhost/deanfly/">-->
    <!--<param name=homeServerPath value="http://127.0.0.1/deanfly/">-->
    </applet>
    </p>

    </body>
</html>
