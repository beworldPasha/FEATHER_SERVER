package com.feather;

import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Base64;

import com.google.gson.Gson;

public class FeatherAPI {
    public final String SIGN_UP_ERROR = "Already registered";
    public final String SIGN_UP_SUCCESS = "Successfully registered";
    public final String SIGN_IN_PASSWORD_ERROR = "Invalid password";
    public final String SIGN_IN_EXIST_ERROR = "User does not exist";
    public final String REFRESH_DENIED = "Refresh denied";
    private final IOException networkException = new IOException("NetworkError");
    private final IOException badArgs = new IOException("BadArgs");
    private final IOException tokensError = new IOException("TOKENS ERROR");
    private PublicKey serverKey = null;
    private final String IPServer = "84.246.85.148";
    private final int portID = 65231;
    private final String KEY_REQUEST = "_getKey_";
    private final String REFRESH_TOKEN_REQUEST = "_refresh_/";
    public final String SIGN_UP_REQUEST = "_register_/";
    public final String SIGN_IN_REQUEST = "_login_/";
    private final String CHANGE_SONG_STATUS_REQUEST = "_like_";
    private final String LIKES_REQUEST = "_get_likes_";
    private static final FeatherAPI INSTANCE = new FeatherAPI();

    private String accessToken = null;
    private String refreshToken = null;

    private FeatherAPI() {
    }

    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;

    public static FeatherAPI getInstance() {
        return INSTANCE;
    }

    private void startSession() throws IOException {
        try {
            clientSocket = new Socket(IPServer, portID);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (Exception exception) {
            throw networkException;
        }
    }

    public String getTokens() throws IOException {
        if (accessToken == null || refreshToken == null)
            return null;

        return accessToken + " " + refreshToken;
    }

    public void refreshTokens(String accessToken, String refreshToken) throws IOException {
        startSession();

        String request = REFRESH_TOKEN_REQUEST + accessToken + '/' + refreshToken + "/_";
        request += '\n';

        try {
            out.write(request);
            out.flush();

            String serverAnswer = in.readLine();
            if (serverAnswer.equals(REFRESH_DENIED))
                throw badArgs;

            fetchTokens(serverAnswer);
        } catch (Exception exception) {
            if (exception == badArgs)
                throw badArgs;
            throw networkException;
        } finally {
            closeSession();
        }
    }

    public void refreshTokens() throws IOException {
        startSession();

        String request = REFRESH_TOKEN_REQUEST + accessToken + '/' + refreshToken + "/_";
        request += '\n';

        try {
            out.write(request);
            out.flush();

            String serverAnswer = in.readLine();
            if (serverAnswer.equals(REFRESH_DENIED))
                throw badArgs;

            fetchTokens(serverAnswer);
        } catch (Exception exception) {
            if (exception == badArgs)
                throw badArgs;
            throw networkException;
        } finally {
            closeSession();
        }
    }

    private void fetchServerKey() throws IOException {
        startSession();
        try {
            out.write(KEY_REQUEST + '\n');
            out.flush();

            serverKey = FeatherKeys.readPublicKey(in.readLine());
        } catch (Exception exception) {
            throw networkException;
        } finally {
            closeSession();
        }
    }

    void fetchTokens(String tokenAnswers) {
        String[] tokens = tokenAnswers.split(" ");
        accessToken = tokens[0];
        refreshToken = tokens[1];
    }

    public Boolean authorize(String typeRequest, String login, String password) throws IOException {
        if (serverKey == null) fetchServerKey();
        startSession();

        String request = typeRequest + login + '/' + password + "/_";
        byte[] bytes = FeatherKeys.cipher(serverKey, FeatherKeys.ENCRYPT, request);
        String encryptRequest = new String(Base64.getEncoder().encode(bytes));
        encryptRequest += '\n';

        boolean isSuccess = true;
        try {
            out.write(encryptRequest);
            out.flush();

            String serverAnswer = in.readLine();
            if (serverAnswer.equals(SIGN_IN_EXIST_ERROR)
                    || serverAnswer.equals(SIGN_IN_PASSWORD_ERROR)
                    || serverAnswer.equals(SIGN_UP_ERROR)) {
                isSuccess = false;
            }

            if (typeRequest.equals(SIGN_IN_REQUEST) && isSuccess) {
                fetchTokens(serverAnswer);
            }
        } catch (Exception exception) {
            isSuccess = false;
            throw networkException;
        } finally {
            closeSession();
        }
        return isSuccess;
    }

    public String getAccessToken() {
        return accessToken;
    }

    private void closeSession() throws IOException {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (Exception exception) {
            throw networkException;
        }
    }

    public <T> T fetchData(String name, Class<T> typeClass) throws IOException {
        if (accessToken == null) throw tokensError;

        startSession();
        String request;

        if (typeClass == null) request = "_init_";
        else request = "_get_" + typeClass.getSimpleName() +
                "_/" + name + "/_" + accessToken;
        request += '\n';
        request = request.toLowerCase();
        request += accessToken;

        T objectData;
        try {
            out.write(request);
            out.flush();

            String json = in.readLine();
            if (json == badArgs.getMessage())
                throw badArgs;

            Gson data = new Gson();

            objectData = data.fromJson(json, typeClass);
        } catch (Exception exception) {
            if (exception == badArgs)
                throw badArgs;
            throw networkException;
        } finally {
            closeSession();
        }

        return objectData;
    }

    public Playlist getFavouritePlaylist() throws IOException {
        if (accessToken == null) throw tokensError;

        startSession();

        Playlist favouritePlaylist;
        try {
            out.write(LIKES_REQUEST);
            out.flush();

            String json = in.readLine();
            if (json.equals(badArgs.getMessage())) throw badArgs;

            Gson data = new Gson();

            favouritePlaylist = data.fromJson(json, Playlist.class);
        } catch (Exception exception) {
            if (exception == badArgs) throw badArgs;
            else throw networkException;
        } finally {
            closeSession();
        }

        return favouritePlaylist;
    }

    public void changeSongStatus(String artist, String album, String song) throws IOException {
        if (accessToken == null) throw tokensError;

        startSession();
        String request = CHANGE_SONG_STATUS_REQUEST +
                '/' + artist + '/' + album + '/' + song + "/_";

        try {
            out.write(request);
            out.flush();
        } catch (Exception exception) {
            throw networkException;
        } finally {
            closeSession();
        }
    }
}