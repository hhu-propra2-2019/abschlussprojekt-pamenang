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

        KeyPair pair = KeyPaarGenerierung();
        PrivateKey privKey = pair.getPrivate();
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privKey);
        byte[] bytes = "JOSHI".getBytes(StandardCharsets.UTF_8);
        sign.update(bytes);

        //Speicher Student + Public Key ab

        byte[] signature = sign.sign();
        String string = bytesToHex(signature);
        System.out.println(string);
        return string;

    }

    private static KeyPair KeyPaarGenerierung() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        return keyPairGen.generateKeyPair();
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
