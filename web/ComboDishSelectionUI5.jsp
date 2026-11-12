<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    if (request.getAttribute("dishList") == null) {
        response.sendRedirect(request.getContextPath() + "/ComboCreateServlet5");
        return;
    }
    request.setAttribute("pageTitle", "Chọn món cho combo");
%>
<%@include file="WEB-INF/jspf/header.jspf"%>
<%
    Combo5 combo = (Combo5) request.getAttribute("combo");
    List<Dish5> dishList = (List<Dish5>) request.getAttribute("dishList");
    List<DishCategory5> categoryList = (List<DishCategory5>) request.getAttribute("categoryList");
    List<ComboDetail5> selectedDetails = (List<ComboDetail5>) request.getAttribute("selectedDetails");
    Map<Integer, DishCategory5> categoryMap = (Map<Integer, DishCategory5>) request.getAttribute("categoryMap");
    Map<Integer, DishDetail5> minPrices = (Map<Integer, DishDetail5>) request.getAttribute("minPrices");
    String keyword = (String) request.getAttribute("keyword");
    Integer categoryId = (Integer) request.getAttribute("categoryId");
    String draftToken = (String) session.getAttribute("draftToken");

    String categoryValue = "";
    if (categoryId != null) {
        categoryValue = categoryId.toString();
    }
    String filterQuery = "keyword=" + View5.q(keyword) + "&categoryId=" + categoryValue;

    String imageName = (String) session.getAttribute("comboImageName");
    if (imageName == null) {
        imageName = "Chưa chọn ảnh";
    }
%>
<%@include file="WEB-INF/jspf/messages.jspf"%>

<section class="panel compact">
    <h2 class="panel-title">Thông tin combo đã nhập</h2>
    <div class="combo-summary">
        <span><strong>Tên:</strong> <%= View5.h(combo.getName()) %></span>
        <span>Giá bán: <%= View5.money(combo.getPrice()) %></span>
        <span>Từ: <%= View5.date(combo.getStartDate()) %> Đến: <%= View5.date(combo.getEndDate()) %></span>
        <span class="summary-description">Mô tả: <%= View5.h(combo.getDescription()) %></span>
        <span>Ảnh: <%= View5.h(imageName) %></span>
    </div>
</section>

<section class="panel compact">
    <h2 class="panel-title">Tìm kiếm món ăn</h2>
    <form action="<%= ctx %>/ComboCreateServlet5" method="get" class="filter-form">
        <label for="keyword">Tên món:</label>
        <input id="keyword" name="keyword" maxlength="255" value="<%= View5.h(keyword) %>">

        <label for="categoryId">Thể loại:</label>
        <select id="categoryId" name="categoryId">
            <option value="">Tất cả</option>
            <%
                for (DishCategory5 category : categoryList) {
                    String selected = "";
                    if (category.getId().equals(categoryId)) {
                        selected = "selected";
                    }
            %>
                <option value="<%= category.getId() %>" <%= selected %>>
                    <%= View5.h(category.getName()) %>
                </option>
            <% } %>
        </select>

        <button class="primary" type="submit">Tìm kiếm</button>
    </form>
</section>

<div class="selection-grid">
    <section>
        <h2 class="section-title">Danh sách món ăn</h2>
        <p class="hint">
            <%= dishList.size() %> kết quả
            <% if (!keyword.isEmpty()) { %>
                cho từ khóa “<%= View5.h(keyword) %>”
            <% } %>
        </p>

        <div class="table-scroll">
            <table class="compact-table">
                <thead>
                    <tr>
                        <th>Mã món</th>
                        <th>Tên món</th>
                        <th>Thể loại</th>
                        <th>Hình ảnh</th>
                        <th>Giá thấp nhất</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (Dish5 dish : dishList) {
                            DishCategory5 category = categoryMap.get(dish.getId());
                            DishDetail5 minPrice = minPrices.get(dish.getId());

                            String categoryName = "";
                            if (category != null) {
                                categoryName = category.getName();
                            }

                            String priceText = "Chưa có giá";
                            if (minPrice != null) {
                                priceText = View5.money(minPrice.getPrice());
                            }

                            String selectUrl = ctx + "/ComboCreateServlet5?dishId="
                                    + dish.getId() + "&" + filterQuery;
                    %>
                        <tr>
                            <td><%= dish.getCode() %></td>
                            <td><%= View5.h(dish.getName()) %></td>
                            <td><%= View5.h(categoryName) %></td>
                            <td>
                                <img class="small-thumbnail"
                                     src="<%= View5.h(View5.image(ctx, dish.getImage())) %>"
                                     alt="<%= View5.h(dish.getName()) %>">
                            </td>
                            <td><%= priceText %></td>
                            <td>
                                <% if (dish.isAvailable() && minPrice != null) { %>
                                    <a class="button small" href="<%= View5.h(selectUrl) %>">Chọn</a>
                                <% } else { %>
                                    <span class="hint">Ngừng phục vụ</span>
                                <% } %>
                            </td>
                        </tr>
                    <% } %>

                    <% if (dishList.isEmpty()) { %>
                        <tr>
                            <td colspan="6" class="empty">Không tìm thấy món ăn phù hợp.</td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </section>

    <section>
        <h2 class="section-title">Các món ăn đã chọn</h2>
        <p class="hint">Mỗi món có thể xóa từng dòng</p>

        <div class="table-scroll">
            <table class="compact-table">
                <thead>
                    <tr>
                        <th>Mã món</th>
                        <th>Tên món</th>
                        <th>Size</th>
                        <th>Số lượng</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (ComboDetail5 line : selectedDetails) {
                            DishDetail5 detail = line.getDishDetail();
                            Dish5 dish = detail.getDish();
                    %>
                        <tr>
                            <td><%= dish.getCode() %></td>
                            <td><%= View5.h(dish.getName()) %></td>
                            <td><%= View5.h(detail.getSize()) %></td>
                            <td><%= line.getQuantity() %></td>
                            <td>
                                <form action="<%= ctx %>/ComboCreateServlet5" method="post">
                                    <input type="hidden" name="csrf" value="<%= View5.h(csrf) %>">
                                    <input type="hidden" name="draftToken" value="<%= View5.h(draftToken) %>">
                                    <input type="hidden" name="detailId" value="<%= detail.getId() %>">
                                    <input type="hidden" name="keyword" value="<%= View5.h(keyword) %>">
                                    <input type="hidden" name="categoryId" value="<%= categoryValue %>">
                                    <button class="small" type="submit" name="action" value="remove"
                                            aria-label="Xóa <%= View5.h(dish.getName()) %>, size <%= View5.h(detail.getSize()) %>">
                                        Xóa
                                    </button>
                                </form>
                            </td>
                        </tr>
                    <% } %>

                    <% if (selectedDetails.isEmpty()) { %>
                        <tr>
                            <td colspan="5" class="empty">Chưa chọn món ăn nào</td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </section>
</div>

<div class="actions right selection-actions">
    <form action="<%= ctx %>/ComboCreateServlet5" method="post">
        <input type="hidden" name="csrf" value="<%= View5.h(csrf) %>">
        <input type="hidden" name="draftToken" value="<%= View5.h(draftToken) %>">
        <input type="hidden" name="keyword" value="<%= View5.h(keyword) %>">
        <input type="hidden" name="categoryId" value="<%= categoryValue %>">
        <button class="primary" type="submit" name="action" value="confirm">Xác nhận</button>
    </form>
    <a class="button" href="<%= ctx %>/ComboCreateServlet5?action=information">Quay lại</a>
</div>

<% if (request.getAttribute("selectedDish") != null) { %>
    <%@include file="DishSizeSelectionUI5.jsp"%>
<% } %>

<%@include file="WEB-INF/jspf/footer.jspf"%>
