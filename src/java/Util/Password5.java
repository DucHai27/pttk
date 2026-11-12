package Util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** So sánh mật khẩu nhập vào với mật khẩu đã băm trong database. */
public final class Password5 {

    private Password5() {
    }

    /** Định dạng lưu: pbkdf2_sha256$iterations$saltBase64$hashBase64. */
    public static boolean verify(String password, String encoded) {
        if (password == null || encoded == null || password.length() > 256) {
            return false;
        }

        try {
            String[] parts = encoded.split("\\$");
            if (parts.length != 4 || !"pbkdf2_sha256".equals(parts[0])) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 100000 || iterations > 1000000) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);

            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                byte[] actualHash = factory.generateSecret(spec).getEncoded();
                return MessageDigest.isEqual(expectedHash, actualHash);
            } finally {
                spec.clearPassword();
            }
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            return false;
        }
    }
}
