package DAO;

import Model.Customer5;
import Model.Manager5;
import Model.Member5;
import Util.Password5;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Kiểm tra tài khoản và xác định vai trò trước khi vào hai module. */
public class MemberDAO5 extends DAO5 {

    public Member5 authenticate(String userName, String password) throws SQLException {
        String sql = "SELECT m.*, mg.employeeId AS managerId, c.memberId AS customerId, e.position "
                + "FROM tblMember5 m "
                + "LEFT JOIN tblEmployee5 e ON e.memberId = m.id "
                + "LEFT JOIN tblManager5 mg ON mg.employeeId = e.memberId "
                + "LEFT JOIN tblCustomer5 c ON c.memberId = m.id "
                + "WHERE m.userName = ?";

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userName);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                if (!Password5.verify(password, result.getString("password"))) {
                    return null;
                }

                Member5 member;
                if (result.getObject("managerId") != null) {
                    Manager5 manager = new Manager5();
                    manager.setPosition(result.getString("position"));
                    member = manager;
                } else if (result.getObject("customerId") != null) {
                    member = new Customer5();
                } else {
                    return null;
                }

                member.setId(result.getInt("id"));
                member.setUserName(result.getString("userName"));
                member.setFullName(result.getString("fullName"));
                member.setDateOfBirth(result.getDate("dateOfBirth"));
                member.setGender(result.getString("gender"));
                member.setAddress(result.getString("address"));
                member.setEmail(result.getString("email"));
                member.setPhoneNumber(result.getString("phoneNumber"));
                return member;
            }
        }
    }
}
