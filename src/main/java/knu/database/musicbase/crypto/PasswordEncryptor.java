package knu.database.musicbase.crypto;

public interface PasswordEncryptor {
    public String getPasswordHash(String password);
}
