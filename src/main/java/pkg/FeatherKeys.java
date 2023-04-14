package pkg;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class FeatherKeys {
    public static void main(String[] args){
        // Для прогона тестов для класса запустить функцию main
        // Тесты:
        File pub = new File("serverkey.pub");
        File pri = new File("serverkey");
        File sec = new File("secretkey");

        String message = "Hello?!";

        // <Generate secret>
//        writeKey(sec, generateSecret());
//        SecretKey secretKey = readSecret(sec);
        SecretKey secretKey = generateSecret();
        // </Generate secret>

        // <Generate RSA>
        KeyPair pair = generatePair();
        writeKey(pri, pair.getPrivate());
        writeKey(pub, pair.getPublic());
        PrivateKey privateKey = readPrivateKey(pri);
        PublicKey publicKey = readPublicKey(pub);
        // </Generate RSA>

        // <Use RSA>
        byte[] encrypted = cipher(publicKey, FeatherKeys.ENCRYPT, message.getBytes());
        byte[] decrypted = cipher(privateKey, FeatherKeys.DECRYPT, encrypted);
        if(message.equals(new String(decrypted, StandardCharsets.UTF_8)))
            System.out.println("Test 1 passed");
        else
            System.out.println("Test 1 didn't pass");
        encrypted = cipher(privateKey, FeatherKeys.ENCRYPT, message.getBytes());
        decrypted = cipher(publicKey, FeatherKeys.DECRYPT, encrypted);
        if(message.equals(new String(decrypted, StandardCharsets.UTF_8)))
            System.out.println("Test 2 passed");
        else
            System.out.println("Test 2 didn't pass");
        // </Use RSA>

        // <Use secret>
        encrypted = cipher(secretKey, ENCRYPT, message.getBytes());
        decrypted = cipher(secretKey, DECRYPT, encrypted);
        if(message.equals(new String(decrypted, StandardCharsets.UTF_8)))
            System.out.println("Test 3 passed");
        else
            System.out.println("Test 3 didn't pass");

        // </Use secret>

        // pub.delete();
        // pri.delete();
    }



    public static SecretKey generateSecret() {
        try{
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            return keyGenerator.generateKey();
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            throw new RuntimeException("Could not generate secret key");
        }
    }


    /*
    * Функция cipher(key, way, msg)
    * Зашифровывает (way = ENCRYPT)
    * или Дешишфровывает (way = DECRYPT)
    * сообщение msg с помощью ключа key
    *
    * Возвращает массив байт
     */
    public static final int ENCRYPT = Cipher.ENCRYPT_MODE;
    public static final int DECRYPT = Cipher.DECRYPT_MODE;

    public static byte[] cipher(Key key, int way, byte[] msg){
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(way, key);
            return cipher.doFinal(msg);
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Could not (de)cipher" + way);
        }
    }
    /*
    * Функция readPrivateKey(file)
    * Считывает приватный ключ из файла
    * Возвращает объект PrivateKey
     */

    private static SecretKey readSecret(File file) {
        try {
            byte[] secretKeyBytes = Files.readAllBytes(file.toPath());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(secretKeyBytes, "AES");
            return keyFactory.generateSecret(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not read secret key");
        }
    }
    public static PrivateKey readPrivateKey(File file){
        try {
            byte[] privKeyBytes = Files.readAllBytes(file.toPath());
            return readPrivateKey(privKeyBytes);
        }catch(IOException e){
            e.printStackTrace();
            throw new RuntimeException("Could not read private key");
        }
    }
    public static PrivateKey readPrivateKey(byte[] privKeyBytes){
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not read private key");
        }
    }

    /*
     * Функция readPublicKey(file)
     * Считывает приватный ключ из файла
     * Возвращает объект PublicKey
     */
    public static PublicKey readPublicKey(File file){
        try {
            byte[] pubKeyBytes = Files.readAllBytes(file.toPath());
            return readPublicKey(pubKeyBytes);
        }catch(IOException e){
            e.printStackTrace();
            throw new RuntimeException("Could not read public key");
        }
    }

    public static PublicKey readPublicKey(byte[] bytes){
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bytes);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not read public key");
        }
    }

    /*
     * Функция writePrivateKey(file, privateKey){
     * Записывает приватный ключ в файл
     */
    public static void writeKey(File file, Key key){
        try {
            file.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                fos.write(key.getEncoded());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not write key");
        }
    }
    /*
    * Функция generatePair()
    * Генерирует пару ключей
    * По умолчанию размер - 512 бит
     */
    public static KeyPair generatePair(){
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(512);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not generate pair");
        }
    }
    public static KeyPair generatePair(int keysize){
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keysize);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not generate pair");
        }
    }
}
