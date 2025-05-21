package com.example.registo01;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {

    // Gera um salt aleatório
    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Gera o hash SHA-256 da senha + salt
    public static String hashPassword(String password, String salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    // Verifica se a senha fornecida bate com o hash armazenado
    public static boolean verifyPassword(String inputPassword, String storedHash, String salt) throws Exception {
        String newHash = hashPassword(inputPassword, salt);
        return newHash.equals(storedHash);
    }
}
