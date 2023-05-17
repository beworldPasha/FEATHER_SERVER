package pkg;

import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Base64;

public class Test {
    static String token;
    static String oldToken;
    static String refresh;
    static PublicKey serverPubKey;
    public static void main(String[] args) throws IOException{

        for(String s : args)
            System.out.println(s);
        String host;
        if(args.length == 0)
            host = "http://84.246.85.148";
        else
            host = args[0];

        // Получить публичный ключ сервера
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_getKey_");
            writer.write("_getKey_\n"); // ->
            writer.flush();
            String spk = reader.readLine();  // <-
            serverPubKey = FeatherKeys.readPublicKey(spk);
        }
        System.out.println();

        // Зарегистрировать admin
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_register_/admin/adminpwd/_");
            byte[] bytes = FeatherKeys.cipher(serverPubKey, FeatherKeys.ENCRYPT, "_register_/admin/adminpwd/_");
            writer.write(new String(Base64.getEncoder().encode(bytes)) + "\n"); // ->
            writer.flush();
            String answer = reader.readLine(); // <-
            System.out.println("answer: " + answer);
        }
        System.out.println();


        // Залогинить admin
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_login_/admin/adminpwd/_");
            byte[] bytes = FeatherKeys.cipher(serverPubKey, FeatherKeys.ENCRYPT, "_login_/admin/adminpwd/_");
            writer.write(new String(Base64.getEncoder().encode(bytes)) + "\n"); // ->
            writer.flush(); // ->
            String[] answer = reader.readLine().split(" ");
            token = answer[0];
            refresh = answer[1];
            System.out.println("token: " + token);
        }
        System.out.println();


        // Получить Automatic как admin
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_get_song_/Amaranthe/Amaranthe/Automatic/_" + token);
            writer.write("_get_song_/Amaranthe/Amaranthe/Automatic/_" + token + "\n"); // ->
            writer.flush();
            String answer = reader.readLine(); // <-
            System.out.println(answer);
        }
        System.out.println();


        // Получить Automatic c невалидным JWT
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_get_song_/Amaranthe/Amaranthe/Automatic/_A.B.C");
            writer.write("_get_song_/Amaranthe/Amaranthe/Automatic/_A.B.C\n");
            writer.flush();
            String answer = reader.readLine(); // <-
            System.out.println(answer);
        }
        System.out.println();


        // Получить лайки как admin
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_get_likes_" + token);
            writer.write("_get_likes_" + token + "\n"); // ->
            writer.flush();
            String answer = reader.readLine(); // <-
            System.out.println(answer);
            writer.close();
        }
        System.out.println();


        // Получить инит как admin
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_init_" + token);
            writer.write("_init_" + token + "\n"); // ->
            writer.flush();
            String answer = reader.readLine();  // <-
            System.out.println(answer);
        }
        System.out.println();


        //  Зарегистрировать newestLogin
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_register_/newestLogin/newsetPassword/_");
            byte[] bytes = FeatherKeys.cipher(serverPubKey, FeatherKeys.ENCRYPT, "_register_/newestLogin/newsetPassword/_");
            writer.write(new String(Base64.getEncoder().encode(bytes)) + "\n"); // ->
            writer.flush();
            String answer = reader.readLine();  // <-
            System.out.println(answer);
        }
        System.out.println();


        // Залогинить newestLogin
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_login_/newestLogin/newsetPassword/_");
            byte[] bytes = FeatherKeys.cipher(serverPubKey, FeatherKeys.ENCRYPT, "_login_/newestLogin/newsetPassword/_");
            writer.write(new String(Base64.getEncoder().encode(bytes)) + "\n"); // ->
            writer.flush();
            String[] answer = reader.readLine().split(" ");  // <-
            token = answer[0];
            refresh = answer[1];
            System.out.println(token);
        }
        System.out.println();


        // Лайкнуть Automatic как newestLogin
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_like_/Amaranthe/Amaranthe/Automatic/_" + token);
            writer.write("_like_/Amaranthe/Amaranthe/Automatic/_" + token + "\n"); // ->
            writer.flush();
            String answer = reader.readLine(); // <-
            System.out.println(answer);
            writer.close();
        }
        System.out.println();


        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_like_/Amaranthe/Amaranthe/Call out my name/_" + token);
            writer.write("_like_/Amaranthe/Amaranthe/Call out my name/_" + token + "\n"); // ->
            writer.flush();
            String answer = reader.readLine(); // <-
            System.out.println(answer);
            writer.close();
        }
        System.out.println();


        // Получить лайки как newestLogin
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_get_likes_" + token);
            writer.write("_get_likes_" + token + "\n"); // ->
            writer.flush();
            String answer = reader.readLine(); // <-
            System.out.println(answer);
            writer.close();
        }
        System.out.println();


        // Залогинить несуществующего пользователя
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_login_/nonexist/adminpwd/_");
            byte[] bytes = FeatherKeys.cipher(serverPubKey, FeatherKeys.ENCRYPT, "_login_/nonexist/adminpwd/_");
            writer.write(new String(Base64.getEncoder().encode(bytes)) + "\n"); // ->
            writer.flush(); // ->
            String answer = reader.readLine(); // <-
            System.out.println(answer);
        }
        System.out.println();
        System.out.println();
        System.out.println();


        // Получить новые JWT для admin
        // тем самым обесценив сущестующий токен
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String requset = String.format("_refresh_/%s/%s/_\n", token, refresh);
            System.out.println(requset);
            writer.write(requset); // ->
            writer.flush(); // ->
            String[] answer = reader.readLine().split(" "); // <-
            oldToken = token;
            token = answer[0];
            refresh = answer[1];
            System.out.printf("new token: %s\nnew refresh: %s\n", token, refresh);
        }
        System.out.println();
//        try{
//            Thread.sleep(20000);
//        }catch (InterruptedException ignored){}

        // Попробовать снова
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String requset = String.format("_refresh_/%s/%s/_\n", oldToken, refresh);
            System.out.println(requset);
            writer.write(requset); // ->
            writer.flush(); // ->
            String answer = reader.readLine(); // <-
            System.out.println("received: " + answer);
        }
        System.out.println();

//        try{
//            Thread.sleep(10000);
//        }catch (InterruptedException ignored){}

        // Получить Automatic как newestLogin с недействительным JWT
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_get_song_/Amaranthe/Amaranthe/Automatic/_" + oldToken);
            writer.write("_get_song_/Amaranthe/Amaranthe/Automatic/_" + oldToken + "\n"); // ->
            writer.flush();
            String answer = reader.readLine(); // <-
            System.out.println(answer);
        }
        System.out.println();

        // Получить Automatic как newestLogin с действительным JWT
        try (Socket socket = new Socket(host, Server.PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("_get_song_/Amaranthe/Amaranthe/Automatic/_" + token);
            writer.write("_get_song_/Amaranthe/Amaranthe/Automatic/_" + token + "\n"); // ->
            writer.flush();
            String answer = reader.readLine(); // <-
            System.out.println(answer);
        }
    }

}