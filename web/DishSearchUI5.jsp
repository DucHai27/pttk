<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "Tìm thông tin món ăn");
    String keyword = (String) request.getAttribute("keyword");
    if (keyword == null) {
        keyword = request.getParameter("keyword");
    }
%>
<%@include file="WEB-INF/jspf/header.jspf"%>

<section class="panel narrow">
    <h2 class="panel-title">Tìm kiếm món ăn</h2>
    <div class="panel-body">
        <%@include file="WEB-INF/jspf/messages.jspf"%>
        <form action="<%= ctx %>/DishSearchServlet5" method="get" class="stack-form">
            <label for="keyword">Từ khóa tìm kiếm</label>
            <input id="keyword" name="keyword" required maxlength="255"
                   value="<%= View5.h(keyword) %>" autofocus>

            <div class="actions centered">
                <button class="primary" type="submit">Tìm kiếm</button>
                <a class="button" href="<%= ctx %>/CustomerMainUI5.jsp">Quay lại</a>
            </div>
            <p class="hint">Nhập tên món ăn cần tìm</p>
        </form>
    </div>
</section>

<%@include file="WEB-INF/jspf/footer.jspf"%>
