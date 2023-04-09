package pkg;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.*;
import java.sql.*;
import java.util.Date;

public class Server {
    private static Socket clientSocket;
    private static ServerSocket server;
    private static BufferedReader in;
    private static BufferedWriter out;

    private static PrivateKey privateKey;
    private static PublicKey publicKey;

    public static final String IP = "84.246.85.148";
    public static final int PORT = 65231;
    public static final String publicKeyFileAddress = "serverkey.pub";
    public static final String privateKeyFileAddress = "serverkey";

    public static void main(String[] args) {
        ensureKeys();
        try {
            try {
                server = new ServerSocket(PORT);
                System.out.printf("Socket opened [%s]\n", new Date());
                for (; ; ) {
                    System.out.printf("! waiting for input ! [%s]\n", new Date());
                    clientSocket = server.accept();
                    out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String command = decipher(in.readLine());
                    if (command.equals("sTop")) return;
                    System.out.printf("Received command: `%s` [%s]\n", command, new Date());
                    String answer = processCommand(command);
                    out.write(answer);
                    System.out.println("Sent to client: " + answer);
                    out.flush();
                }
            } finally {
                clientSocket.close();
                in.close();
                out.close();
                System.out.printf("Server closed [%s]\n", new Date());
                server.close();
            }
        } catch (ClassNotFoundException e){
            e.printStackTrace();
            System.out.printf("ClassNotFoundException: %s [%s]\n", e.getMessage(), new Date());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf("IOException: %s [%s]\n", e.getMessage(), new Date());
        }
        System.out.println("Program stopped");
    }

    private static String decipher(String str) throws IOException, ClassNotFoundException {
        return str;
    }

    private static void ensureKeys() {
        File pubKeyFile = new File(publicKeyFileAddress);
        File privKeyFile = new File(privateKeyFileAddress);
        if(pubKeyFile.exists() && privKeyFile.exists()){
            privateKey = Rsa.readPrivateKey(privKeyFile);
            publicKey = Rsa.readPublicKey(pubKeyFile);
        }else {
            KeyPair pair = Rsa.generatePair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
            Rsa.writePublicKey(pubKeyFile, publicKey);
            Rsa.writePrivateKey(privKeyFile, privateKey);
        }
    }

    public static String processCommand(String command){
        Connection connection = null;
        // artist, title, album, track_num, length, path - Структура таблицы
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:__musicdb.db");
            Statement st = connection.createStatement();
            st.setQueryTimeout(30);
            String[] cmd = command.split("_"); // cmd[0] == "" всегда
            if (cmd.length == 1) return "Illegal command\n";
            StringTokenizer tokenizer = new StringTokenizer(command, "/");
            tokenizer.nextToken();
            if(command.startsWith("_establish_")) {
                return "Established\n";
            }else if(command.equals("get_song_")){
                String query = String.format("SELECT * FROM song WHERE UPPER(artist) = UPPER(\"%s\") AND UPPER(album) = UPPER(\"%s\") AND UPPER(title) = UPPER(\"%s\")", tokenizer.nextToken(), tokenizer.nextToken(), tokenizer.nextToken());
                ResultSet rs = st.executeQuery(query);
                return formSong(rs);
            }else if(command.startsWith("_get_artist_")) {
                String artist = tokenizer.nextToken();
                String query = String.format("SELECT * FROM song WHERE UPPER(artist) = UPPER(\"%s\")", artist);
                ResultSet rs = st.executeQuery(query);
                return formArtist(rs);
            }else if(command.startsWith("_get_playlist") || command.startsWith("_get_album_")){
                String artist = tokenizer.nextToken();
                String album = tokenizer.nextToken();
                String query = String.format("SELECT * FROM song WHERE UPPER(artist) = UPPER(\"%s\") AND UPPER(album) = UPPER(\"%s\")", artist, album);
                ResultSet rs = st.executeQuery(query);
                return formPlaylist(rs);
            }else if(command.startsWith("_init_")){
                ResultSet rs = st.executeQuery("SELECT * FROM song");
                return formInit(rs);
            }else if(command.startsWith("_set_")) {
                return "Not implemented\n";
            }else if(command.startsWith("_register_")) {
                String login = tokenizer.nextToken();
                String passwdHash = tokenizer.nextToken();
                String query = String.format("SELECT login FROM users WHERE UPPER(login) = UPPER(\"%s\")", login);
                ResultSet rs = st.executeQuery(query);
                if (rs.next()) {
                    return "Already registered\n";
                } else {
                    query = String.format("INSERT INTO users VALUES (\"%s\", \"%s\")", login, passwdHash);
                    st.execute(query);
                    return "Successfully registered\n";
                }
            }else if(command.startsWith("_login_")){

            }else
                return "Not implemented\n";
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.printf("SQLException: %s [%s]%n", e.getMessage(), new Date());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.printf("SQLException: %s [%s]%n", e.getMessage(), new Date());
            }
        }
        return "Illegal command\n";
    }

    private static String formSong(ResultSet rs) throws SQLException {
        StringBuilder result = new StringBuilder(500);
        result.append("{\"name\": \"").append(rs.getString("title"))
                .append("\", \"artist\": \"").append(rs.getString("artist"))
                .append("\", \"playlists\": [\"").append(rs.getString("artist")).append("/").append(rs.getString("album"));

        result.append("\"], \"image\":  \"http://").append(IP).append("/Music/").append(rs.getString("artist"))
                .append('/').append(rs.getString("album")).append("/COVER_").append(rs.getString("album"))
                .append(".jpg\",");

        result.append("\"length\": ").append(rs.getString("length"))
                .append(", \"trackIndex\": ").append(rs.getString("track_num"));

        result.append(", \"url\": \"http://").append(IP).append("/Music/")
                .append(rs.getString("path"))
                .append(".mp3\"}\n");
        return result.toString();
    }

    private static String formArtist(ResultSet rs) throws SQLException {
        String artist = rs.getString("artist");
        StringBuilder result = new StringBuilder(500);
        HashSet<String> albums = new HashSet<>();
        while (rs.next()) {
            albums.add(rs.getString("album"));
        }
        // имя альбомы изображение
        result.append("{\"name\": \"")
                .append(artist)
                .append("\", \"playlists\": [");
        int i = 0;
        for (String s : albums) {
            result.append("\"").append(artist).append("/").append(s).append("\"");
            if (i++ < albums.size() - 1) result.append(", ");
        }
        result.append("], \"image\": \"http://")
                .append(IP).append("/Music/")
                .append(artist).append("/COVER.jpg\"}\n");
        return result.toString();
    }

    private static String formPlaylist(ResultSet rs) throws SQLException {
        StringBuilder result = new StringBuilder(500);
        HashSet<String> artists = new HashSet<>();
        String artist = rs.getString("artist");
        String album = rs.getString("album");
        result.append("{\"songs\": [");
        int length = 0;
        rs.next();
        while (true) {
            result.append("{\"artist\": \"").append(rs.getString("artist")).append("\", \"title\": \"").append(rs.getString("title")).append("\", \"image\": \"http://").append(IP).append("/Music/").append(artist).append("/").append(album).append("/COVER_").append(album).append(".jpg\"}");
            artists.add(rs.getString("artist"));
            ++length;
            if (rs.next()) {
                result.append(", ");
            } else break;
        }
        result.append("], \"length\": ").append(length)
                .append(", \"image\": \"http://").append(IP).append("/Music/").append(artist).append("/").append(album).append("/COVER_").append(album).append(".jpg\"")
                .append(", \"name\": \"").append(album)
                .append("\", \"artists\": [");
        int i = 0;
        for (String s : artists) {
            result.append("\"").append(s).append("\"");
            if (++i != artists.size()) result.append(", ");
        }

        result.append("]}\n");
        return result.toString();
    }

    private static String formInit(ResultSet rs) throws SQLException {
        StringBuilder result = new StringBuilder(500);
        Random rnd = new Random();
        HashMap<String, String> dict = new HashMap<>();
        int PROB = 6;
        while (rs.next()) {
            int n = (int) (Math.random() * 3 * PROB);
            if (n == 0) dict.put(String.format("%s/%s", rs.getString("artist"), rs.getString("album")), "\"playlist\"");
            if (n == 1) dict.put(rs.getString("artist"), "\"artist\"");
            if (n == 2) dict.put(rs.getString("path"), "\"song\"");
        }
        result.append("{");
        int i = 0;
        for (Map.Entry<String, String> ent : dict.entrySet()) {
            result.append("\"").append(ent.getKey()).append("\": ");
            result.append(ent.getValue());
            if (++i < dict.entrySet().size()) result.append(", ");
        }
        result.append("}\n");
        return result.toString();
    }
}
