package Controller;

import Model.Combo5;
import Util.Upload5;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/** Dọn ảnh của combo chưa lưu khi người dùng đăng xuất hoặc session hết hạn. */
@WebListener
public class DraftSessionListener5 implements HttpSessionListener {

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        Combo5 draft = (Combo5) event.getSession().getAttribute("comboDraft");
        if (draft != null) {
            Upload5.deleteDraft(draft.getImage());
        }
    }
}
