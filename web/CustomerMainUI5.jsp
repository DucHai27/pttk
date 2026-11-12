<%@page contentType="text/html" pageEncoding="UTF-8"%>
<% request.setAttribute("pageTitle", "Trang chủ khách hàng"); %>
<%@include file="WEB-INF/jspf/header.jspf"%>
<section class="panel narrow home-panel">
    <h2 class="panel-title">Chức năng</h2>
    <div class="panel-body">
        <a class="button feature-button primary" href="<%= ctx %>/DishSearchUI5.jsp">Tìm thông tin món ăn</a>
    </div>
</section>
<%@include file="WEB-INF/jspf/footer.jspf"%>
