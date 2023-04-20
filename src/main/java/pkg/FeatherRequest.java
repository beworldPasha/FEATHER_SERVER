package pkg;

import java.io.Serializable;
import java.security.PublicKey;

public class FeatherRequest implements Serializable {
    public PublicKey publicKey;
    public boolean isEncrypted = false;
    public byte[] request;
    public byte[] JWT;
    public FeatherRequest setPubKey(PublicKey key){
        publicKey = key;
        return this;
    }
    public FeatherRequest setEncrypted(boolean b){
        isEncrypted = b;
        return this;
    }
    public FeatherRequest setRequest(byte[] req){
        request = req;
        return this;
    }
    public FeatherRequest setRequest(String req){
        request = req.getBytes();
        return this;
    }
    public FeatherRequest setJWT(byte[] jwt){
        JWT = jwt;
        return this;
    }
    public FeatherRequest setJWT(String jwt){
        JWT = jwt.getBytes();
        return this;
    }
//    public FeatherRequest(PublicKey key, byte[] req, byte[] jwt){
//        publicKey = key;
//        request = req;
//        JWT = jwt;
//    }
//    public FeatherRequest(PublicKey key, String req, String jwt){
//        publicKey = key;
//        request = req.getBytes();
//        JWT = jwt.getBytes();
//    }
//
//    public FeatherRequest(PublicKey key, byte[] req, String jwt){
//        publicKey = key;
//        request = req;
//        JWT = jwt.getBytes();
//    }
//
//    public FeatherRequest(PublicKey key, String req, byte[] jwt){
//        publicKey = key;
//        request = req.getBytes();
//        JWT = jwt;
//    }
}