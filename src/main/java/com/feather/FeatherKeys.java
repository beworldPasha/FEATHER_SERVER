package com.feather;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

class FeatherKeys {
    public static final int ENCRYPT = Cipher.ENCRYPT_MODE;
    public static final int DECRYPT = Cipher.DECRYPT_MODE;

    /*
     * Функция cipher(key, way, msg)
     * Зашифровывает (way = ENCRYPT)
     * или Дешишфровывает (way = DECRYPT)
     * сообщение msg с помощью ключа key
     *
     * Возвращает массив байт
     */
    public static byte[] cipher(Key key, int way, String msg) {
        return cipher(key, way, msg.getBytes());
    }

    private static byte[] cipher(Key key, int way, byte[] msg) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(way, key);
            return cipher.doFinal(msg);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not (de)cipher" + way);
        }
    }

    /*
     * Функция readPublicKey(file)
     * Считывает приватный ключ из файла
     * Возвращает объект PublicKey
     */
    public static PublicKey readPublicKey(String base64) {
        return readPublicKey(Base64.getDecoder().decode(base64));
    }

    private static PublicKey readPublicKey(byte[] bytes) { // Работает
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bytes);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not read public key");
        }
    }
}