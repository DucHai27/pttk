package Controller;

import DAO.ComboDAO5;
import DAO.DishCategoryDAO5;
import DAO.DishDAO5;
import DAO.DishDetailDAO5;
import Model.Combo5;
import Model.ComboDetail5;
import Model.Dish5;
import Model.DishCategory5;
import Model.DishDetail5;
import Util.Upload5;
import Util.View5;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Điều khiển các bước: nhập thông tin -> chọn món -> xác nhận tạo combo. */
@WebServlet("/ComboCreateServlet5")
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024,
        fileSizeThreshold = 1024 * 1024
)
public class ComboCreateServlet5 extends HttpServlet {

    /** GET dùng để mở form, tìm món và hiển thị popup chọn kích cỡ. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        // Các yêu cầu của cùng một phiên được xử lý lần lượt để tránh sửa bản nháp đồng thời.
        synchronized (session) {
            String action = request.getParameter("action");

            if ("new".equals(action)) {
                clearDraft(session);
                session.setAttribute("draftToken", UUID.randomUUID().toString());
                response.sendRedirect(request.getContextPath()
                        + "/ComboCreateServlet5?action=information");
                return;
            }

            if (session.getAttribute("draftToken") == null) {
                response.sendRedirect(request.getContextPath() + "/ComboManagementServlet5");
                return;
            }

            Combo5 draft = (Combo5) session.getAttribute("comboDraft");
            if ("information".equals(action) || draft == null) {
                showInformationForm(request, response, draft);
                return;
            }

            try {
                selection(request, response, draft);
            } catch (IllegalArgumentException ex) {
                response.sendError(400);
            } catch (SQLException ex) {
                ControllerSupport5.databaseError(request, response, ex);
            }
        }
    }

    /** POST dùng khi người dùng nhập thông tin, thêm/xóa món, xác nhận hoặc hủy. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        synchronized (session) {
            String action = request.getParameter("action");
            String draftToken = (String) session.getAttribute("draftToken");
            String submittedToken = request.getParameter("draftToken");

            // Form cũ không được tiếp tục sửa bản nháp mới hoặc lưu lại combo đã xác nhận.
            if (draftToken == null || !draftToken.equals(submittedToken)) {
                session.setAttribute("success",
                        "Phiên tạo combo đã kết thúc. Vui lòng kiểm tra danh sách hoặc tạo combo mới.");
                response.sendRedirect(request.getContextPath() + "/ComboManagementServlet5");
                return;
            }

            Combo5 draft = (Combo5) session.getAttribute("comboDraft");

            try {
                if ("cancel".equals(action)) {
                    clearDraft(session);
                    response.sendRedirect(request.getContextPath() + "/ComboManagementServlet5");
                    return;
                }

                if ("information".equals(action)) {
                    saveInformation(request, session, draft);
                } else {
                    if (draft == null) {
                        throw new IllegalArgumentException("Vui lòng nhập thông tin combo trước.");
                    }

                    if (action == null) {
                        action = "";
                    }

                    switch (action) {
                        case "add":
                            addDish(request, draft);
                            break;
                        case "remove":
                            removeDish(request, draft);
                            break;
                        case "confirm":
                            confirmCombo(request, response, draft);
                            return;
                        default:
                            throw new IllegalArgumentException("Thao tác không hợp lệ.");
                    }
                }

                response.sendRedirect(request.getContextPath()
                        + "/ComboCreateServlet5" + filterQuery(request));
            } catch (IllegalArgumentException ex) {
                if ("information".equals(action) || draft == null) {
                    request.setAttribute("error", ex.getMessage());
                    retainForm(request);
                    showInformationForm(request, response, draft);
                } else {
                    session.setAttribute("draftError", ex.getMessage());
                    response.sendRedirect(request.getContextPath()
                            + "/ComboCreateServlet5" + filterQuery(request));
                }
            } catch (SQLException ex) {
                getServletContext().log("Không lưu được combo", ex);
                session.setAttribute("draftError",
                        "Không thể lưu dữ liệu. Các lựa chọn vẫn được giữ lại; vui lòng kiểm tra MySQL rồi thử lại.");
                response.sendRedirect(request.getContextPath()
                        + "/ComboCreateServlet5" + filterQuery(request));
            } catch (IOException ex) {
                getServletContext().log("Không ghi được ảnh combo", ex);
                request.setAttribute("error",
                        "Không đọc hoặc lưu được ảnh. Vui lòng chọn lại ảnh PNG/JPG/GIF hợp lệ.");
                retainForm(request);
                showInformationForm(request, response, draft);
            }
        }
    }

    /** Đọc dữ liệu form và lưu combo tạm vào session, chưa ghi vào MySQL. */
    private void saveInformation(HttpServletRequest request, HttpSession session, Combo5 previous)
            throws IOException, ServletException {
        String name = text(request, "name", 255, true);
        String description = text(request, "description", 5000, false);
        BigDecimal price = readPrice(request);

        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(text(request, "startDate", 10, true));
            end = LocalDate.parse(text(request, "endDate", 10, true));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không hợp lệ.");
        }

        if (start.getYear() < 1000 || end.getYear() > 9999 || end.isBefore(start)) {
            throw new IllegalArgumentException(
                    "Ngày kết thúc phải từ ngày bắt đầu trở đi; năm trong khoảng 1000–9999.");
        }

        Combo5 combo = new Combo5();
        combo.setName(name);
        combo.setPrice(price.doubleValue());
        combo.setDescription(description);
        combo.setStartDate(Date.valueOf(start));
        combo.setEndDate(Date.valueOf(end));
        combo.setStatus(combo.getDisplayStatus());

        // Quay lại sửa thông tin vẫn giữ ảnh và những món đã chọn.
        if (previous != null) {
            combo.setImage(previous.getImage());
            combo.setComboDetails(previous.getComboDetails());
        }

        Part imagePart = request.getPart("image");
        String uploadedImage = Upload5.save(imagePart);
        if (uploadedImage != null) {
            if (previous != null) {
                Upload5.deleteDraft(previous.getImage());
            }

            combo.setImage(uploadedImage);
            String fileName = imagePart.getSubmittedFileName();
            if (fileName == null) {
                fileName = "Ảnh combo";
            } else {
                fileName = fileName.replace('\\', '/');
            }

            String imageName = fileName.substring(fileName.lastIndexOf('/') + 1);
            session.setAttribute("comboImageName", imageName);
        }

        session.setAttribute("comboDraft", combo);
    }

    private BigDecimal readPrice(HttpServletRequest request) {
        BigDecimal price;
        try {
            price = new BigDecimal(text(request, "price", 20, true));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Giá bán phải là số hợp lệ.");
        }

        BigDecimal maxPrice = new BigDecimal("9999999999.99");
        boolean hasTooManyDecimals = price.stripTrailingZeros().scale() > 2;
        if (price.signum() <= 0 || price.compareTo(maxPrice) > 0 || hasTooManyDecimals) {
            throw new IllegalArgumentException(
                    "Giá bán phải lớn hơn 0, không quá 9.999.999.999,99đ và có tối đa 2 chữ số thập phân.");
        }
        return price;
    }

    /** Thêm dòng mới; nếu đã có cùng món và cùng size thì cộng số lượng. */
    private void addDish(HttpServletRequest request, Combo5 draft) throws SQLException {
        int detailId = ControllerSupport5.positiveId(request.getParameter("detailId"));
        int dishId = ControllerSupport5.positiveId(request.getParameter("dishId"));
        int quantity = ControllerSupport5.positiveId(request.getParameter("quantity"));

        DishDetailDAO5 detailDAO = new DishDetailDAO5();
        DishDetail5 detail = detailDAO.getDishDetail(detailId);
        if (detail == null || detail.getDish().getId() != dishId || !detail.getDish().isAvailable()) {
            throw new IllegalArgumentException("Món ăn hoặc kích cỡ đã chọn không còn phục vụ.");
        }
        if (quantity > 999) {
            throw new IllegalArgumentException("Số lượng phải từ 1 đến 999.");
        }

        List<ComboDetail5> selected = new ArrayList<>(Arrays.asList(draft.getComboDetails()));
        for (ComboDetail5 line : selected) {
            if (line.getDishDetail().getId().equals(detail.getId())) {
                int totalQuantity = line.getQuantity() + quantity;
                if (totalQuantity > 999) {
                    throw new IllegalArgumentException("Tổng số lượng cho mỗi kích cỡ không vượt quá 999.");
                }
                line.setQuantity(totalQuantity);
                return;
            }
        }

        ComboDetail5 newLine = new ComboDetail5();
        newLine.setDishDetail(detail);
        newLine.setQuantity(quantity);
        selected.add(newLine);
        draft.setComboDetails(selected.toArray(new ComboDetail5[0]));
    }

    private void removeDish(HttpServletRequest request, Combo5 draft) {
        int detailId = ControllerSupport5.positiveId(request.getParameter("detailId"));
        List<ComboDetail5> remaining = new ArrayList<>();

        for (ComboDetail5 line : draft.getComboDetails()) {
            if (line.getDishDetail().getId() != detailId) {
                remaining.add(line);
            }
        }
        draft.setComboDetails(remaining.toArray(new ComboDetail5[0]));
    }

    /** Đây là bước duy nhất ghi combo và các món thành phần vào MySQL. */
    private void confirmCombo(HttpServletRequest request, HttpServletResponse response, Combo5 draft)
            throws SQLException, IOException {
        if (draft.getComboDetails().length == 0) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một món ăn cho combo.");
        }

        ComboDAO5 comboDAO = new ComboDAO5();
        boolean saved = comboDAO.createCombo(draft);
        if (!saved) {
            throw new SQLException("Không lưu được combo.");
        }

        // Ảnh đã thuộc combo được lưu: bỏ bản nháp nhưng không xóa file ảnh.
        HttpSession session = request.getSession();
        session.removeAttribute("comboDraft");
        session.removeAttribute("draftToken");
        session.removeAttribute("comboImageName");
        session.setAttribute("success", "Thêm combo thành công!");
        response.sendRedirect(request.getContextPath() + "/ComboManagementServlet5");
    }

    /** Chuẩn bị dữ liệu cho trang chọn món và popup kích cỡ. */
    private void selection(HttpServletRequest request, HttpServletResponse response, Combo5 combo)
            throws SQLException, ServletException, IOException {
        String keyword = ControllerSupport5.keyword(request);
        String categoryValue = request.getParameter("categoryId");
        Integer categoryId = null;
        if (categoryValue != null && !categoryValue.isBlank()) {
            categoryId = ControllerSupport5.positiveId(categoryValue);
        }

        DishDAO5 dishDAO = new DishDAO5();
        List<Dish5> dishes;
        if (categoryId != null) {
            if (keyword.isEmpty()) {
                DishCategory5 category = new DishCategory5();
                category.setId(categoryId);
                dishes = dishDAO.getDishesByCategory(category);
            } else {
                dishes = dishDAO.findDishes(keyword, categoryId);
            }
        } else if (keyword.isEmpty()) {
            dishes = dishDAO.getAllDishes();
        } else {
            dishes = dishDAO.searchDishesByName(keyword);
        }

        DishCategoryDAO5 categoryDAO = new DishCategoryDAO5();
        ControllerSupport5.enrich(request, dishes);
        request.setAttribute("categoryList", categoryDAO.getAllCategories());
        request.setAttribute("keyword", keyword);
        request.setAttribute("categoryId", categoryId);
        request.setAttribute("combo", combo);
        request.setAttribute("selectedDetails", Arrays.asList(combo.getComboDetails()));

        HttpSession session = request.getSession();
        request.setAttribute("error", session.getAttribute("draftError"));
        session.removeAttribute("draftError");

        String dishValue = request.getParameter("dishId");
        if (dishValue != null) {
            int dishId = ControllerSupport5.positiveId(dishValue);
            Dish5 dish = dishDAO.getDishInformation(dishId);
            if (dish == null || !dish.isAvailable()) {
                response.sendError(404);
                return;
            }

            DishDetailDAO5 detailDAO = new DishDetailDAO5();
            request.setAttribute("selectedDish", dish);
            request.setAttribute("sizeOptions", detailDAO.getDishDetailsByDish(dish));
        }

        request.getRequestDispatcher("/ComboDishSelectionUI5.jsp").forward(request, response);
    }

    private void showInformationForm(HttpServletRequest request, HttpServletResponse response, Combo5 combo)
            throws ServletException, IOException {
        // JSP chỉ đọc form đã chuẩn bị, không phải tự ghép dữ liệu từ bản nháp.
        if (request.getAttribute("form") == null) {
            Map<String, String> form = new HashMap<>();
            if (combo != null) {
                String price = BigDecimal.valueOf(combo.getPrice()).stripTrailingZeros().toPlainString();
                form.put("name", combo.getName());
                form.put("price", price);
                form.put("description", combo.getDescription());
                form.put("startDate", combo.getStartDate().toString());
                form.put("endDate", combo.getEndDate().toString());
            }
            request.setAttribute("form", form);
        }
        request.setAttribute("combo", combo);
        request.getRequestDispatcher("/ComboInformationUI5.jsp").forward(request, response);
    }

    private static String text(HttpServletRequest request, String key, int limit, boolean required) {
        String value = request.getParameter(key);
        if (value == null) {
            value = "";
        }
        value = value.trim();

        if (required && value.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đủ tên combo, giá bán và thời gian áp dụng.");
        }
        if (value.length() > limit) {
            throw new IllegalArgumentException(
                    "Thông tin nhập vượt quá độ dài cho phép (" + limit + " ký tự).");
        }
        return value;
    }

    private static void retainForm(HttpServletRequest request) {
        Map<String, String> form = new HashMap<>();
        String[] fields = {"name", "price", "description", "startDate", "endDate"};
        for (String field : fields) {
            form.put(field, request.getParameter(field));
        }
        request.setAttribute("form", form);
    }

    private static String filterQuery(HttpServletRequest request) {
        String keyword = request.getParameter("keyword");
        String categoryId = request.getParameter("categoryId");
        return "?keyword=" + View5.q(keyword) + "&categoryId=" + View5.q(categoryId);
    }

    private static void clearDraft(HttpSession session) {
        Combo5 draft = (Combo5) session.getAttribute("comboDraft");
        if (draft != null) {
            Upload5.deleteDraft(draft.getImage());
        }
        session.removeAttribute("comboDraft");
        session.removeAttribute("draftToken");
        session.removeAttribute("comboImageName");
        session.removeAttribute("draftError");
    }
}
