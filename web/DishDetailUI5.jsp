<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    if (request.getAttribute("dish") == null) {
        response.sendRedirect(request.getContextPath() + "/DishSearchUI5.jsp");
        return;
    }
    request.setAttribute("pageTitle", "Chi tiết món ăn");
%>
<%@include file="WEB-INF/jspf/header.jspf"%>
<%
    Dish5 dish = (Dish5) request.getAttribute("dish");
    DishCategory5 category = (DishCategory5) request.getAttribute("category");
    List<DishDetail5> details = (List<DishDetail5>) request.getAttribute("dishDetails");
    String keyword = (String) request.getAttribute("keyword");

    String categoryName = "Chưa phân loại";
    if (category != null) {
        categoryName = category.getName();
    }
%>

<div class="detail-area">
    <table class="information-table">
        <tbody>
            <tr>
                <th>Mã món:</th>
                <td><%= dish.getCode() %></td>
                <td rowspan="4" class="detail-image-cell">
                    <img class="detail-image"
                         src="<%= View5.h(View5.image(ctx, dish.getImage())) %>"
                         alt="<%= View5.h(dish.getName()) %>">
                </td>
            </tr>
            <tr>
                <th>Tên món:</th>
                <td><%= View5.h(dish.getName()) %></td>
            </tr>
            <tr>
                <th>Thể loại:</th>
                <td><%= View5.h(categoryName) %></td>
            </tr>
            <tr>
                <th>Hình ảnh:</th>
                <td>Ảnh món ăn</td>
            </tr>
            <tr>
                <th>Mô tả:</th>
                <td colspan="2" class="description"><%= View5.h(dish.getDescription()) %></td>
            </tr>
            <tr>
                <th>Trạng thái phục vụ:</th>
                <td colspan="2"><%= View5.h(dish.getStatus()) %></td>
            </tr>
        </tbody>
    </table>

    <table class="size-table">
        <caption>Kích cỡ và giá bán</caption>
        <thead class="sr-only">
            <tr>
                <th>Kích cỡ</th>
                <th>Giá bán</th>
            </tr>
        </thead>
        <tbody>
            <% for (DishDetail5 detail : details) { %>
                <tr>
                    <td><%= View5.h(detail.getSize()) %></td>
                    <td><%= View5.money(detail.getPrice()) %></td>
                </tr>
            <% } %>

            <% if (details.isEmpty()) { %>
                <tr>
                    <td colspan="2">Chưa có kích cỡ và giá bán.</td>
                </tr>
            <% } %>
        </tbody>
    </table>

    <div class="actions centered">
        <a class="button" href="<%= ctx %>/DishSearchServlet5?keyword=<%= View5.q(keyword) %>">Quay lại</a>
    </div>
</div>

<%@include file="WEB-INF/jspf/footer.jspf"%>
