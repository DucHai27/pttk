package Model;

/** Nhân viên kế thừa thông tin tài khoản từ Member5. */
public class Employee5 extends Member5 {
    private static final long serialVersionUID = 1L;
    private String position;

    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }
}
