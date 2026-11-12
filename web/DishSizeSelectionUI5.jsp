<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List,Model.Dish5,Model.DishDetail5,Util.View5"%>
<%-- Servlet chuẩn bị dữ liệu; trang chọn món nhúng JSP này để hiển thị popup. --%>
<%
    if (request.getAttribute("selectedDish") == null) {
        response.sendRedirect(request.getContextPath() + "/ComboCreateServlet5");
        return;
    }

    Dish5 modalDish = (Dish5) request.getAttribute("selectedDish");
    List<DishDetail5> modalSizes = (List<DishDetail5>) request.getAttribute("sizeOptions");
    String modalCtx = request.getContextPath();
    String modalCsrf = (String) session.getAttribute("csrf");
    String modalDraftToken = (String) session.getAttribute("draftToken");
    String modalKeyword = (String) request.getAttribute("keyword");
    Integer modalCategory = (Integer) request.getAttribute("categoryId");

    String modalCategoryValue = "";
    if (modalCategory != null) {
        modalCategoryValue = modalCategory.toString();
    }

    String cancelUrl = modalCtx + "/ComboCreateServlet5?keyword="
            + View5.q(modalKeyword) + "&categoryId=" + modalCategoryValue;
%>
<div class="modal-backdrop">
    <section class="modal panel" role="dialog" aria-modal="true"
             aria-labelledby="size-title" data-modal>
        <h2 class="panel-title" id="size-title">
            Chọn kích cỡ và số lượng – <%= View5.h(modalDish.getName()) %> (<%= modalDish.getCode() %>)
        </h2>

        <div class="panel-body">
            <p>Món ăn: <%= View5.h(modalDish.getName()) %> | Mã món: <%= modalDish.getCode() %></p>
            <form action="<%= modalCtx %>/ComboCreateServlet5" method="post">
                <input type="hidden" name="csrf" value="<%= View5.h(modalCsrf) %>">
                <input type="hidden" name="draftToken" value="<%= View5.h(modalDraftToken) %>">
                <input type="hidden" name="dishId" value="<%= modalDish.getId() %>">
                <input type="hidden" name="keyword" value="<%= View5.h(modalKeyword) %>">
                <input type="hidden" name="categoryId" value="<%= modalCategoryValue %>">

                <table>
                    <thead>
                        <tr>
                            <th>Kích cỡ</th>
                            <th>Giá bán</th>
                            <th>Chọn</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (DishDetail5 option : modalSizes) { %>
                            <tr>
                                <td>
                                    <label for="size-<%= option.getId() %>">
                                        <%= View5.h(option.getSize()) %>
                                    </label>
                                </td>
                                <td><%= View5.money(option.getPrice()) %></td>
                                <td>
                                    <input id="size-<%= option.getId() %>" type="radio"
                                           name="detailId" value="<%= option.getId() %>" required
                                           aria-label="Chọn size <%= View5.h(option.getSize()) %>">
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>

                <div class="quantity-field">
                    <label for="quantity">Số lượng:</label>
                    <input id="quantity" type="number" name="quantity"
                           min="1" max="999" step="1" value="1" required>
                </div>

                <div class="actions centered">
                    <% if (!modalSizes.isEmpty()) { %>
                        <button class="primary" type="submit" name="action" value="add">Thêm vào combo</button>
                    <% } else { %>
                        <button class="primary" type="button" disabled>Thêm vào combo</button>
                    <% } %>
                    <a class="button" data-modal-cancel href="<%= View5.h(cancelUrl) %>">Hủy</a>
                </div>
            </form>
        </div>
    </section>
</div>
