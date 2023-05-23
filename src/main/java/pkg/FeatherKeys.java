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
import java.util.Base64;

public class FeatherKeys {
    public static void main(String[] args){
        // Для прогона тестов для класса запустить функцию main
        // Тесты:
        File pub = new File("serverkey.pub");
        File pri = new File("serverkey");

        String message = "Hello?!";

        // <Generate RSA>
        KeyPair pair = generatePair();

        writeKey(pri, pair.getPrivate());
        writeKey(pub, pair.getPublic());

        PrivateKey privateKey = readPrivateKey(pri);
        PublicKey publicKey = readPublicKey(pub);
        // </Generate RSA>

        // <Use RSA>
        byte[] encrypted = cipher(publicKey, FeatherKeys.ENCRYPT, message.getBytes()); // шифруется публичным
        byte[] decrypted = cipher(privateKey, FeatherKeys.DECRYPT, encrypted); // дешифруется приватным
        if(message.equals(new String(decrypted, StandardCharsets.UTF_8)))
            System.out.println("Test 1 passed");
        else
            System.out.println("Test 1 didn't pass");

        encrypted = cipher(privateKey, FeatherKeys.ENCRYPT, message.getBytes()); // шифруется приватным
        decrypted = cipher(publicKey, FeatherKeys.DECRYPT, encrypted); // дешифруется публичным
        if(message.equals(new String(decrypted, StandardCharsets.UTF_8)))
            System.out.println("Test 2 passed");
        else
            System.out.println("Test 2 didn't pass");
        // </Use RSA>

        // Удалить файлы с ключами:
        // pub.delete();
        // pri.delete();
    }

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

    public static byte[] cipher(Key key, int way, byte[] msg){
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(way, key);
            return cipher.doFinal(msg);
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Could not (de)cipher" + way);
        }
    }
    public static byte[] cipher(Key key, int way, String msg){
        return cipher(key, way, msg.getBytes());
    }

    /*
    * Функция readPrivateKey(file)
    * Считывает приватный ключ из файла
    * Возвращает объект PrivateKey
     */


    /*
     * Функция readPublicKey(file)
     * Считывает приватный ключ из файла
     * Возвращает объект PublicKey
     */
    public static PublicKey readPublicKey(byte[] bytes){ // Работает
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bytes);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not read public key");
        }
    }
    public static PublicKey readPublicKey(String base64){
        return readPublicKey(Base64.getDecoder().decode(base64));
    }
    public static PublicKey readPublicKey(File file){ // Работает
        try {
            byte[] pubKeyBytes = Files.readAllBytes(file.toPath());
            return readPublicKey(pubKeyBytes);
        }catch(IOException e){
            e.printStackTrace();
            throw new RuntimeException("Could not read public key");
        }
    }

    /*
     * Функция writeKey(file, privateKey){
     * Записывает ключ в файл (public, private, secret)
     */
    public static void writeKey(File file, Key key){ // Работает
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
    * Генерирует пару ключей public и private
    * По умолчанию размер - 512 бит
     */
    public static KeyPair generatePair(){ // Работает
        return generatePair(512);
    }
    public static KeyPair generatePair(int keysize){ // Работает
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keysize);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not generate pair");
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
    public static PrivateKey readPrivateKey(byte[] privKeyBytes){ // Работает
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not read private key");
        }
    }
}
