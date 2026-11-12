<%@page contentType="text/html" pageEncoding="UTF-8" isErrorPage="true"%>
<%
    request.setAttribute("pageTitle", "Không thể thực hiện yêu cầu");
    if (request.getAttribute("error") == null) {
        Integer status = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String message = "Hệ thống chưa xử lý được yêu cầu. Vui lòng thử lại.";

        if (Integer.valueOf(404).equals(status)) {
            message = "Không tìm thấy dữ liệu hoặc trang bạn yêu cầu.";
        } else if (Integer.valueOf(403).equals(status)) {
            message = "Bạn không có quyền truy cập chức năng này.";
        } else if (Integer.valueOf(400).equals(status)) {
            message = "Yêu cầu không hợp lệ hoặc phiên làm việc đã thay đổi. Vui lòng mở lại chức năng.";
        }
        request.setAttribute("error", message);
    }
%>
<%@include file="WEB-INF/jspf/header.jspf"%>
<%@include file="WEB-INF/jspf/messages.jspf"%>

<div class="actions">
    <a class="button" href="<%= ctx %>/LoginServlet5">Về trang chủ</a>
    <% if (signedMember instanceof Manager5 && session.getAttribute("comboDraft") != null) { %>
        <a class="button" href="<%= ctx %>/ComboCreateServlet5">Tiếp tục tạo combo</a>
    <% } %>
</div>

<%@include file="WEB-INF/jspf/footer.jspf"%>
