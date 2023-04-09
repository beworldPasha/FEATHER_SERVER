package pkg;

import javax.crypto.Cipher;
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

public class Rsa {
    public static void main(String[] args){
        // Для прогона тестов для класса запустить функцию main
        File pub = new File("serverkey.pub");
        File pri = new File("serverkey");
        PrivateKey privateKey = Rsa.readPrivateKey(pri);
        PublicKey publicKey = Rsa.readPublicKey(pub);
        String message = "Hello?!";
        byte[] encrypted = Rsa.cipher(publicKey, Rsa.ENCRYPT, message.getBytes());
        byte[] decrypted = Rsa.cipher(privateKey, Rsa.DECRYPT, encrypted);
        if(message.equals(new String(decrypted, StandardCharsets.UTF_8))){
            System.out.println("Test 1 passed");
        }else{
            System.out.println("Test didn't pass");
        }
        encrypted = Rsa.cipher(privateKey, Rsa.ENCRYPT, message.getBytes());
        decrypted = Rsa.cipher(publicKey, Rsa.DECRYPT, encrypted);
        if(message.equals(new String(decrypted, StandardCharsets.UTF_8))){
            System.out.println("Test 2 passed");
        }else{
            System.out.println("Test didn't pass");
        }
        pub.delete();
        pri.delete();
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
            throw new RuntimeException("Could not (de)cipher");
        }
    }

    /*
    * Функция readPrivateKey(file)
    * Считывает приватный ключ из файла
    * Возвращает объект PrivateKey
     */
    public static PrivateKey readPrivateKey(File file){
        try {
            byte[] privKeyBytes = Files.readAllBytes(file.toPath());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privKeyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            return privateKey;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Функция readPublicKey(file)
     * Считывает приватный ключ из файла
     * Возвращает объект PublicKey
     */
    public static PublicKey readPublicKey(File file){
        try{
            byte[] pubKeyBytes = Files.readAllBytes(file.toPath());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pubKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            return publicKey;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
    /*
     * Функция writePrivateKey(file, privateKey){
     * Записывает приватный ключ в файл
     */
    public static void writePrivateKey(File file, PrivateKey privateKey){
        try {
            file.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                fos.write(privateKey.getEncoded());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /*
     * Функция writePublicKey(file, publicKey){
     * Записывает публичный ключ в файл
     */
    public static void writePublicKey(File file, PublicKey publicKey){
        try {
            file.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                fos.write(publicKey.getEncoded());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }
    public static KeyPair generatePair(int keysize){
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keysize);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
