package mops.klausurzulassung.Token;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.security.*;

public class Tokengenerierung {

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        signatur();
    }

    public static String hashing(String HashValue){

        Hasher hasher = Hashing.sha256().newHasher();
        hasher.putString(HashValue, Charsets.UTF_8);
        HashCode token = hasher.hash();

        return token.toString();
    }

    public static String erstellenHashValue(String matr, String fach, String key){
        return matr+fach+key;
    }

    public static String erstellenQuittung(String matr, String fach, String token){
        return matr+fach+token;
    }

    public static String signatur() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        //Creating KeyPair generator object
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

        //Initializing the key pair generator
        keyPairGen.initialize(2048);

        //Generate the pair of keys
        KeyPair pair = keyPairGen.generateKeyPair();

        //Getting the privatekey from the key pair
        PrivateKey privKey = pair.getPrivate();

        //Creating a Signature object
        Signature sign = Signature.getInstance("SHA256withRSA");

        //Initializing the signature
        sign.initSign(privKey);
        byte[] bytes = "JOSHI".getBytes(StandardCharsets.UTF_8);

        //Adding data to the signature
        sign.update(bytes);

        //Calculating the signature
        byte[] signature = sign.sign();
        String string = bytesToHex(signature);
        System.out.println(string);
        return string;

    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
