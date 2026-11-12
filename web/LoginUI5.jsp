<%@page contentType="text/html" pageEncoding="UTF-8"%>
<% request.setAttribute("pageTitle", "Đăng nhập"); %>
<%@include file="WEB-INF/jspf/header.jspf"%>
<section class="panel narrow">
    <h2 class="panel-title">Đăng nhập hệ thống</h2>
    <div class="panel-body">
        <%@include file="WEB-INF/jspf/messages.jspf"%>
        <form action="<%= ctx %>/LoginServlet5" method="post" class="stack-form">
            <input type="hidden" name="csrf" value="<%= View5.h(csrf) %>">
            <label for="userName">Tên đăng nhập</label>
            <input id="userName" name="userName" autocomplete="username" maxlength="100" required value="<%= View5.h(request.getAttribute("userName")) %>" autofocus>
            <label for="password">Mật khẩu</label>
            <input id="password" type="password" name="password" autocomplete="current-password" maxlength="256" required>
            <div class="actions centered"><button class="primary" type="submit">Đăng nhập</button></div>
        </form>
    </div>
</section>
<%@include file="WEB-INF/jspf/footer.jspf"%>
