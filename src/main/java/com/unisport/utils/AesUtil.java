package com.unisport.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Simple AES utility for reversible encryption/decryption.
 */
public final class AesUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private AesUtil() {
    }

    /**
     * Encrypt plain text with the given secret.
     *
     * @param plaintext plain text
     * @param secret    secret key
     * @return base64 encoded cipher text
     */
    public static String encrypt(String plaintext, String secret) {
        try {
            if (plaintext == null) {
                return null;
            }
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, buildKey(secret));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encrypt failed", e);
        }
    }

    /**
     * Decrypt cipher text with the given secret.
     *
     * @param cipherText base64 encoded cipher text
     * @param secret     secret key
     * @return decrypted plain text
     */
    public static String decrypt(String cipherText, String secret) {
        try {
            if (cipherText == null) {
                return null;
            }
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, buildKey(secret));
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decrypt failed", e);
        }
    }

    private static SecretKeySpec buildKey(String secret) throws Exception {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("Secret must not be empty");
        }
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(secret.getBytes(StandardCharsets.UTF_8));
        byte[] aesKey = new byte[16];
        System.arraycopy(key, 0, aesKey, 0, aesKey.length);
        return new SecretKeySpec(aesKey, ALGORITHM);
    }
}
