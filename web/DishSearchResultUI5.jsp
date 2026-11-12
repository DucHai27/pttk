<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    if (request.getAttribute("dishList") == null) {
        response.sendRedirect(request.getContextPath() + "/DishSearchUI5.jsp");
        return;
    }
    request.setAttribute("pageTitle", "Kết quả tìm kiếm món ăn");
%>
<%@include file="WEB-INF/jspf/header.jspf"%>
<%
    List<Dish5> dishList = (List<Dish5>) request.getAttribute("dishList");
    Map<Integer, DishCategory5> categoryMap = (Map<Integer, DishCategory5>) request.getAttribute("categoryMap");
    Map<Integer, DishDetail5> minPrices = (Map<Integer, DishDetail5>) request.getAttribute("minPrices");
    String keyword = (String) request.getAttribute("keyword");
%>

<p class="hint">Từ khóa: <strong><%= View5.h(keyword) %></strong> · <%= dishList.size() %> kết quả</p>

<div class="table-scroll">
    <table class="search-results">
        <thead>
            <tr>
                <th>Mã món ăn</th>
                <th>Tên món ăn</th>
                <th>Thể loại</th>
                <th>Hình ảnh đại diện</th>
                <th>Giá bán thấp nhất</th>
            </tr>
        </thead>
        <tbody>
            <%
                for (Dish5 dish : dishList) {
                    DishCategory5 category = categoryMap.get(dish.getId());
                    DishDetail5 minPrice = minPrices.get(dish.getId());

                    String categoryName = "Chưa phân loại";
                    if (category != null) {
                        categoryName = category.getName();
                    }

                    String priceText = "Chưa có giá bán";
                    if (minPrice != null) {
                        priceText = View5.money(minPrice.getPrice());
                    }

                    String detailUrl = ctx + "/DishDetailServlet5?id="
                            + dish.getId() + "&keyword=" + View5.q(keyword);
            %>
                <tr>
                    <td><a href="<%= View5.h(detailUrl) %>"><%= dish.getCode() %></a></td>
                    <td class="dish-name">
                        <a href="<%= View5.h(detailUrl) %>"><%= View5.h(dish.getName()) %></a>
                    </td>
                    <td><%= View5.h(categoryName) %></td>
                    <td>
                        <a href="<%= View5.h(detailUrl) %>" aria-label="Xem <%= View5.h(dish.getName()) %>">
                            <img class="dish-thumbnail"
                                 src="<%= View5.h(View5.image(ctx, dish.getImage())) %>"
                                 alt="<%= View5.h(dish.getName()) %>">
                        </a>
                    </td>
                    <td><%= priceText %></td>
                </tr>
            <% } %>

            <% if (dishList.isEmpty()) { %>
                <tr>
                    <td colspan="5" class="empty">Không tìm thấy món ăn phù hợp. Vui lòng thử từ khóa khác.</td>
                </tr>
            <% } %>
        </tbody>
    </table>
</div>

<div class="actions">
    <a class="button" href="<%= ctx %>/DishSearchUI5.jsp?keyword=<%= View5.q(keyword) %>">Quay lại</a>
</div>

<%@include file="WEB-INF/jspf/footer.jspf"%>
