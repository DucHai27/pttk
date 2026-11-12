<%@page contentType="text/html" pageEncoding="UTF-8"%>
<% request.setAttribute("pageTitle", "Trang chủ nhân viên quản lý"); %>
<%@include file="WEB-INF/jspf/header.jspf"%>
<section class="panel narrow home-panel">
    <h2 class="panel-title">Chức năng</h2>
    <div class="panel-body">
        <a class="button feature-button primary" href="<%= ctx %>/ComboManagementServlet5">Quản lý combo</a>
    </div>
</section>
<%@include file="WEB-INF/jspf/footer.jspf"%>
