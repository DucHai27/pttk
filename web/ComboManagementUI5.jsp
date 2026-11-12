<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    if (request.getAttribute("comboList") == null) {
        response.sendRedirect(request.getContextPath() + "/ComboManagementServlet5");
        return;
    }
    request.setAttribute("pageTitle", "Quản lý combo");
%>
<%@include file="WEB-INF/jspf/header.jspf"%>
<%@include file="WEB-INF/jspf/messages.jspf"%>
<%
    List<Combo5> comboList = (List<Combo5>) request.getAttribute("comboList");
%>

<h2 class="section-title">Danh sách combo hiện có</h2>
<div class="table-scroll">
    <table>
        <thead>
            <tr>
                <th>Mã combo</th>
                <th>Tên combo</th>
                <th>Giá bán</th>
                <th>Thời gian áp dụng</th>
                <th>Trạng thái</th>
            </tr>
        </thead>
        <tbody>
            <% for (Combo5 combo : comboList) { %>
                <tr>
                    <td><%= combo.getCode() %></td>
                    <td><%= View5.h(combo.getName()) %></td>
                    <td><%= View5.money(combo.getPrice()) %></td>
                    <td><%= View5.date(combo.getStartDate()) %> – <%= View5.date(combo.getEndDate()) %></td>
                    <td><%= View5.h(combo.getDisplayStatus()) %></td>
                </tr>
            <% } %>

            <% if (comboList.isEmpty()) { %>
                <tr>
                    <td colspan="5" class="empty">Chưa có combo nào.</td>
                </tr>
            <% } %>
        </tbody>
    </table>
</div>

<div class="actions">
    <a class="button primary" href="<%= ctx %>/ComboCreateServlet5?action=new">Thêm combo mới</a>
    <a class="button" href="<%= ctx %>/ManagerMainUI5.jsp">Quay lại</a>
</div>

<%@include file="WEB-INF/jspf/footer.jspf"%>
