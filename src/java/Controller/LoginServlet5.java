package Controller;

import DAO.MemberDAO5;
import Model.Manager5;
import Model.Member5;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/LoginServlet5")
public class LoginServlet5 extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Member5 member = (Member5) request.getSession().getAttribute("member");
        if (member != null) {
            home(request, response, member);
            return;
        }
        request.getRequestDispatcher("/LoginUI5.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userName = request.getParameter("userName");
        String password = request.getParameter("password");

        boolean invalidUserName = userName == null || userName.isBlank() || userName.length() > 100;
        boolean invalidPassword = password == null || password.isEmpty();

        if (invalidUserName || invalidPassword) {
            request.setAttribute("error", "Vui lòng nhập tên đăng nhập và mật khẩu.");
        } else {
            try {
                MemberDAO5 memberDAO = new MemberDAO5();
                Member5 member = memberDAO.authenticate(userName.trim(), password);

                if (member != null) {
                    // Tạo phiên mới sau khi đăng nhập để không sử dụng lại phiên chưa xác thực.
                    request.getSession().invalidate();
                    HttpSession session = request.getSession(true);
                    session.setAttribute("member", member);
                    session.setAttribute("csrf", UUID.randomUUID().toString());

                    home(request, response, member);
                    return;
                }

                request.setAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng.");
            } catch (SQLException ex) {
                getServletContext().log("Không thể đăng nhập do lỗi CSDL", ex);
                response.setStatus(503);
                request.setAttribute("error",
                        "Không kết nối được MySQL. Vui lòng kiểm tra cấu hình kết nối và chạy script database.");
            }
        }

        request.setAttribute("userName", userName);
        request.getRequestDispatcher("/LoginUI5.jsp").forward(request, response);
    }

    private void home(HttpServletRequest request, HttpServletResponse response, Member5 member)
            throws IOException {
        String page = "/CustomerMainUI5.jsp";
        if (member instanceof Manager5) {
            page = "/ManagerMainUI5.jsp";
        }
        response.sendRedirect(request.getContextPath() + page);
    }
}
