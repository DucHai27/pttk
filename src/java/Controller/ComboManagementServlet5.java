package Controller;

import DAO.ComboDAO5;
import Model.Combo5;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/ComboManagementServlet5")
public class ComboManagementServlet5 extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            ComboDAO5 comboDAO = new ComboDAO5();
            List<Combo5> combos = comboDAO.getAllCombos();
            request.setAttribute("comboList", combos);

            // Thông báo chỉ hiển thị một lần sau khi thêm combo thành công.
            request.setAttribute("success", request.getSession().getAttribute("success"));
            request.getSession().removeAttribute("success");

            request.getRequestDispatcher("/ComboManagementUI5.jsp").forward(request, response);
        } catch (SQLException ex) {
            ControllerSupport5.databaseError(request, response, ex);
        }
    }
}
