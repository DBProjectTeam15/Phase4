package knu.database.musicbase.crypto;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor()
public class PasswordEncryptorImpl implements PasswordEncryptor {

    @Override
    public String getPasswordHash(String password) {
//        try {
            // TODO: SEEDING을 쉽게하기 위해 일부러 평문으로 처리합니다.
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            md.update((password).getBytes());
//            byte[] digest = md.digest();
//
//            StringBuilder sb = new StringBuilder();
//            for (byte b : digest) {
//                String hex = Integer.toHexString(0xff & b);
//                if (hex.length() == 1) {
//                    sb.append('0');
//                }
//                sb.append(hex);
//            }
//
//            return sb.toString();
            return password;

//        } catch (NoSuchAlgorithmException e) {
//
//            log.error(e.getMessage());
//            throw new RuntimeException(e);
//        }
    }
}
