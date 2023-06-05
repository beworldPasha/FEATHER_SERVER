package pkg;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.crypto.BadPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.sql.*;
import java.util.Date;

public class Server {
    private static Socket clientSocket;
    private static ServerSocket server;
    private static BufferedReader in;
    private static BufferedWriter out;

    private static PrivateKey serverPrivateKey;
    private static PublicKey serverPublicKey;

    public static final String IP = "84.246.85.148";
    public static final int PORT = 65231;
    public static final String publicKeyFileAddress = "serverkey.pub";
    public static final String privateKeyFileAddress = "serverkey";
    private static Statement st;

    public static void main(String[] args) {
        ensureKeys();
        for(;;) {
            try {
                try {
                    server = new ServerSocket(PORT);
                    System.out.printf("Socket opened [%s]\n", new Date());
                    for (; ; ) {
                        System.out.printf("! waiting for input ! [%s]\n", new Date());
                        clientSocket = server.accept();
                        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String command = decode(in.readLine());
                        System.out.printf("Received command: `%s` [%s]\n", command, new Date());
                        String answer;
                        try {
                            answer = processCommand(command);
                        }catch(Exception e){
                            e.printStackTrace();
                            answer = "Error\n";
                        }
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
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf("Exception: %s [%s]\n", e.getMessage(), new Date());
            }
            System.out.println("Program stopped");
        }
    }

    private static String decode(String str){
        if(str.charAt(0)=='_')
            return str;
        byte[] deBase64 = Base64.getDecoder().decode(str);
        byte[] decrypt = FeatherKeys.cipher(serverPrivateKey, FeatherKeys.DECRYPT, deBase64);
        return new String(decrypt, StandardCharsets.UTF_8);
    }

    private static void ensureKeys() {
        File pubKeyFile = new File(publicKeyFileAddress);
        File privKeyFile = new File(privateKeyFileAddress);
        if(pubKeyFile.exists() && privKeyFile.exists()){
            serverPrivateKey = FeatherKeys.readPrivateKey(privKeyFile);
            serverPublicKey = FeatherKeys.readPublicKey(pubKeyFile);
        }else {
            KeyPair pair = FeatherKeys.generatePair();
            serverPrivateKey = pair.getPrivate();
            serverPublicKey = pair.getPublic();
            FeatherKeys.writeKey(pubKeyFile, serverPublicKey);
            FeatherKeys.writeKey(privKeyFile, serverPrivateKey);
        }
    }

    private static String createJWT(String user){
        Algorithm algorithm = Algorithm.HMAC512(serverPrivateKey.getEncoded());
        String token = JWT.create()
                .withIssuer("Feather Server")
                .withExpiresAt(new Date(new Date().getTime()+15*60*1000))
                .withClaim("usr", user)
                .withIssuedAt(new Date())
                .sign(algorithm);
        return token;
    }


    private static DecodedJWT verifyJWT(String[] cmd, int ind) throws JWTVerificationException, SQLException {
        StringJoiner sj = new StringJoiner("_");
        try {
            for (int i = ind; ; ++i)  sj.add(cmd[i]);
        }catch(IndexOutOfBoundsException ignored){}
        String token = sj.toString();
        return verifyJWT(token);
    }
    private static DecodedJWT verifyJWT(String token) throws JWTVerificationException, SQLException {
        Algorithm algorithm = Algorithm.HMAC512(serverPrivateKey.getEncoded());
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("Feather Server")
                .build();
        try {
            DecodedJWT jwt = verifier.verify(token);
            String usr = jwt.getClaim("usr").asString();
            String query = String.format("SELECT refresh_date FROM user WHERE username = \"%s\"", usr);
            ResultSet rs = st.executeQuery(query);
            if(!rs.next())
                throw new JWTVerificationException("no refresh in db");
            long refreshDate = rs.getLong("refresh_date");
            System.out.println(new Date(jwt.getIssuedAt().getTime()));
            System.out.println(new Date(refreshDate));
            if(jwt.getIssuedAt().before(new Date(refreshDate)))
                throw new JWTVerificationException("jwt.getIssuedAt().before(new Date(refreshDate))");
            return jwt;
        }catch(JWTDecodeException e){
            throw new JWTVerificationException("Unable to decode JWT");
        }
    }

    public static String processCommand(String command){
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:__musicdb.db");
            st = connection.createStatement();
            st.setQueryTimeout(30);
            String[] cmd = command.split("_"); // cmd[0] == "" всегда
            if (cmd.length <= 1) return "BadArgs\n";
            StringTokenizer tokenizer = new StringTokenizer(command, "/");
            tokenizer.nextToken();
            if (command.startsWith("_login_")) {
                String login = tokenizer.nextToken();
                String passwd = tokenizer.nextToken();
                String query = String.format("SELECT password FROM user WHERE username = \"%s\"", login);
                ResultSet rs = st.executeQuery(query);
                if(!rs.next()){
                    return "User does not exist\n";
                }
                if (BCrypt.verifyer().verify(passwd.toCharArray(), rs.getString("password").toCharArray()).verified){
                    long date = new Date().getTime();
                    st.executeUpdate(String.format("UPDATE user SET refresh_date = %d WHERE username = \"%s\"", date, login));
                    Thread.sleep(1000);
                    String jwt = createJWT(login);
                    String refresh = createRefresh(jwt);
                    query = String.format("UPDATE user SET refresh = \"%s\" WHERE username = \"%s\"", refresh, login);
                    st.executeUpdate(query);
                    return jwt + " " + refresh + "\n";
                } else {
                    return "Invalid password\n";
                }
            } else if (command.startsWith("_register_")) {
                String login = tokenizer.nextToken();
                String passwd = tokenizer.nextToken();
                String passwdHash = BCrypt.withDefaults().hashToString(12, passwd.toCharArray());
                String query = String.format("SELECT username FROM user WHERE UPPER(username) = UPPER(\"%s\")", login);
                ResultSet rs = st.executeQuery(query);
                if (!rs.next()){
                    query = String.format("INSERT INTO user VALUES (\"%s\", \"%s\", \"\", \"\")", login, passwdHash);
                    st.execute(query);
                    return "Successfully registered\n";
                } else return "Already registered\n";
            } else if (command.equals("_getKey_")) {
                return new String(Base64.getEncoder().encode(serverPublicKey.getEncoded()), StandardCharsets.UTF_8) + "\n";
            } else if(command.startsWith("_refresh_")){
                String token = tokenizer.nextToken();
                String refresh = tokenizer.nextToken();
                String refreshToMatch = createRefresh(token);
                if(!refreshToMatch.equals(refresh))
                    return "BadArgs\n";
                String query = String.format("SELECT username FROM user WHERE refresh = \"%s\"", refresh);
                ResultSet rs = st.executeQuery(query);
                if(rs.next()){
                    String login = rs.getString("username");
                    query = String.format("UPDATE user SET refresh_date = %d WHERE username = \"%s\"", new Date().getTime(), login);
                    st.executeUpdate(query);
                    Thread.sleep(1000);
                    String new_jwt = createJWT(login);
                    String new_refresh = createRefresh(new_jwt);
                    query = String.format("UPDATE user SET refresh = \"%s\" WHERE username = \"%s\"", new_refresh, login);
                    st.executeUpdate(query);
                    return new_jwt + " " + new_refresh + "\n";
                }
                System.out.println("E: Refresh denied");
                return "BadArgs\n";
            } else if (command.startsWith("_get_song_")) {
                DecodedJWT jwt = verifyJWT(cmd, 4);
                String findQuery = String.format("SELECT * FROM song WHERE UPPER(artist) = UPPER(\"%s\") AND UPPER(album) = UPPER(\"%s\") AND UPPER(title) = UPPER(\"%s\")", tokenizer.nextToken(), tokenizer.nextToken(), tokenizer.nextToken());
                ResultSet rs = st.executeQuery(findQuery);
                if(rs.next()) {
                    String path = rs.getString("path");
                    String query = String.format("SELECT * " +
                            "FROM (SELECT username, song.path FROM " +
                            "(SELECT user.username, like.song_id FROM like LEFT  JOIN user " +
                            "WHERE user.ROWID = like.user_id) LEFT JOIN song WHERE song_id=song.rowid) " +
                            "WHERE username = \"%s\" AND path = \"%s\"", jwt.getClaim("usr").asString(), path);
                    ResultSet liked = st.executeQuery(query);
                    boolean songLiked = liked.next();
                    rs = st.executeQuery(findQuery);
                    if(songLiked)
                        return formSong(rs, "true");
                    else
                        return formSong(rs, "false");
                }else {
                    System.out.println("E: No such song");
//                    return "No such song\n";
                    return "BadArgs\n";
                }
            } else if (command.startsWith("_get_artist_")) {
                verifyJWT(cmd, 4);
                String artist = tokenizer.nextToken();
                String query = String.format("SELECT * FROM song WHERE UPPER(artist) = UPPER(\"%s\")", artist);
                ResultSet rs = st.executeQuery(query);
                if(rs.next())
                    return formArtist(rs);
                else {
                    System.out.println("E: No such artist");
//                    return "No such artist\n";
                    return "BadArgs\n";
                }
            } else if (command.startsWith("_get_playlist") || command.startsWith("_get_album_")) {
                verifyJWT(cmd, 4);
                String artist = tokenizer.nextToken();
                String album = tokenizer.nextToken();
                String query = String.format("SELECT * FROM song WHERE UPPER(artist) = UPPER(\"%s\") AND UPPER(album) = UPPER(\"%s\")", artist, album);
                ResultSet rs = st.executeQuery(query);
                if(rs.next())
                    return formPlaylist(rs);
                else {
                    System.out.println("E: No such album");
//                    return "No such album\n";
                    return "BadArgs\n";
                }
            } else if (command.startsWith("_get_likes_")) {
                DecodedJWT jwt = verifyJWT(cmd, 3);
                String username = jwt.getClaim("usr").asString();
                String query = String.format("SELECT * FROM song " +
                        "WHERE ROWID in (SELECT song_id FROM like WHERE user_id = " +
                        "(SELECT user.ROWID FROM user WHERE username = \"%s\"))", username);
                ResultSet rs = st.executeQuery(query);
                if(rs.next())
                    return formLikes(rs);
                else
                    return "Empty playlist\n";
            } else if (command.startsWith("_init_")) {
                verifyJWT(cmd, 2);
                ResultSet rs = st.executeQuery("SELECT * FROM song");
                return formInit(rs);
            } else if(command.startsWith("_like_")){
                DecodedJWT jwt = verifyJWT(cmd, 3);
                String query = String.format("SELECT path FROM song WHERE UPPER(path) = UPPER(\"%s\")", cmd[2].substring(1, cmd[2].length()-1));
                ResultSet rs = st.executeQuery(query);
                if(!rs.next()) {
                    System.out.println("E: No such song");
//                    return "No such song\n";
                    return "BadArgs\n";
                }
                String path = rs.getString("path");
                query = String.format("SELECT * " +
                        "FROM (SELECT username, song.path FROM " +
                        "(SELECT user.username, like.song_id FROM like LEFT  JOIN user " +
                        "WHERE user.ROWID = like.user_id) LEFT JOIN song WHERE song_id=song.rowid) " +
                        "WHERE username = \"%s\" AND path = \"%s\"", jwt.getClaim("usr").asString(), path);
                rs = st.executeQuery(query);
                if(!rs.next()){
                    query = String.format("INSERT INTO like " +
                            "VALUES((SELECT ROWID FROM user WHERE username = \"%s\"), " +
                            "(SELECT ROWID FROM song WHERE path = \"%s\"))", jwt.getClaim("usr").asString(), path);
                    st.executeUpdate(query);
                    System.out.println("I: Liked");
                }else {
                    query = String.format("DELETE FROM like WHERE " +
                                    "song_id = (SELECT rowid FROM song WHERE path = \"%s\") AND user_id = (SELECT rowid FROM user WHERE username = \"%s\")",
                            path,
                            jwt.getClaim("usr").asString()
                            );
                    System.out.println(jwt.getClaim("usr"));
                    System.out.println(query);
                    st.executeUpdate(query);
                    System.out.println("I: Like already exists");
//                    return "Like already exists\n";
                }
                return "Success\n";
            } else {
                System.out.println("E: No such command");
                return "BadArgs\n";
            }
        }catch (JWTVerificationException e){
            e.printStackTrace();
            System.out.println("E: Auth Error");
            return "BadArgs\n";
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.printf("SQLException: %s [%s]%n", e.getMessage(), new Date());
            System.out.println("E: SQL Error");
            return "BadArgs\n";
        } catch(InterruptedException e){
            e.printStackTrace();
            System.out.println("E: Interrupted exception");
            return "BadArgs\n";
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.printf("SQLException: %s [%s]%n", e.getMessage(), new Date());
            }
        }
    }

    private static String createRefresh(String jwt) {
        StringBuilder refresh = new StringBuilder(10);
        long seed = 0;
        for(byte b : serverPrivateKey.getEncoded())
            seed += b;
        Random rnd = new Random(seed);
        for(int i=0;i<10;++i)
            refresh.append(jwt.charAt(rnd.nextInt(jwt.length())));
        return refresh.toString();
    }

    private static String formLikes(ResultSet rs) throws SQLException{
        return formPlaylist(rs);
    }

    private static String formSong(ResultSet rs, String liked) throws SQLException {
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
                .append(".mp3\"");

        result.append(", \"liked\": ").append(liked).append("}\n");
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
