<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    if (session.getAttribute("draftToken") == null) {
        response.sendRedirect(request.getContextPath() + "/ComboCreateServlet5?action=new");
        return;
    }
    if (request.getAttribute("form") == null) {
        response.sendRedirect(request.getContextPath() + "/ComboCreateServlet5?action=information");
        return;
    }
    request.setAttribute("pageTitle", "Thêm combo mới");
%>
<%@include file="WEB-INF/jspf/header.jspf"%>
<%
    Combo5 combo = (Combo5) request.getAttribute("combo");
    Map<String, String> comboForm = (Map<String, String>) request.getAttribute("form");
    String draftToken = (String) session.getAttribute("draftToken");
%>
<%@include file="WEB-INF/jspf/messages.jspf"%>

<form id="combo-information" action="<%= ctx %>/ComboCreateServlet5"
      method="post" enctype="multipart/form-data" class="combo-information-form">
    <input type="hidden" name="csrf" value="<%= View5.h(csrf) %>">
    <input type="hidden" name="draftToken" value="<%= View5.h(draftToken) %>">
    <input type="hidden" name="action" value="information">

    <section class="panel">
        <h2 class="panel-title">Thông tin combo</h2>
        <div class="panel-body form-grid">
            <label for="name">Tên combo <span aria-hidden="true">*</span></label>
            <input id="name" name="name" maxlength="255" required
                   value="<%= View5.h(comboForm.get("name")) %>" autofocus>

            <label for="price">Giá bán (đ) <span aria-hidden="true">*</span></label>
            <input id="price" type="number" name="price"
                   min="0.01" max="9999999999.99" step="0.01" required
                   value="<%= View5.h(comboForm.get("price")) %>">

            <label for="description">Mô tả</label>
            <textarea id="description" name="description" rows="3" maxlength="5000"><%= View5.h(comboForm.get("description")) %></textarea>

            <label for="startDate">Ngày bắt đầu <span aria-hidden="true">*</span></label>
            <input id="startDate" type="date" name="startDate"
                   min="1000-01-01" max="9999-12-31" required
                   value="<%= View5.h(comboForm.get("startDate")) %>">

            <label for="endDate">Ngày kết thúc <span aria-hidden="true">*</span></label>
            <input id="endDate" type="date" name="endDate"
                   min="1000-01-01" max="9999-12-31" required
                   value="<%= View5.h(comboForm.get("endDate")) %>">

            <label for="image">Hình ảnh</label>
            <div>
                <input id="image" type="file" name="image"
                       accept="image/png,image/jpeg,image/gif">
                <p class="hint">PNG, JPG hoặc GIF · Tối đa 5 MB</p>
                <% if (combo != null && combo.getImage() != null) { %>
                    <p class="hint">Đã chọn: <%= View5.h(session.getAttribute("comboImageName")) %></p>
                    <img class="dish-thumbnail"
                         src="<%= View5.h(View5.image(ctx, combo.getImage())) %>"
                         alt="Ảnh combo đã chọn">
                <% } %>
            </div>
        </div>
    </section>
</form>

<div class="actions right information-actions">
    <button class="primary" type="submit" form="combo-information">Tiếp tục</button>
    <form action="<%= ctx %>/ComboCreateServlet5" method="post">
        <input type="hidden" name="csrf" value="<%= View5.h(csrf) %>">
        <input type="hidden" name="draftToken" value="<%= View5.h(draftToken) %>">
        <button type="submit" name="action" value="cancel">Hủy</button>
    </form>
</div>

<%@include file="WEB-INF/jspf/footer.jspf"%>
