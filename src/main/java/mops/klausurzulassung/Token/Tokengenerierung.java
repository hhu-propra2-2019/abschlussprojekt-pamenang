package mops.klausurzulassung.Token;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class Tokengenerierung {

    public static String erstellenHashValue(String matr, String fach){
        return matr+fach;
    }

    public static String erstellenQuittung(String matr, String fach, String token){
        return matr+fach+token;
    }

    public static String erstellenToken(String HashValue) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        KeyPair pair = KeyPaarGenerierung();
        PrivateKey privateKey = pair.getPrivate();
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        byte[] hashValueBytes = HashValue.getBytes(StandardCharsets.UTF_8);
        sign.update(hashValueBytes);

        //Speicher Student + Public Key ab
        PublicKey publicKey = pair.getPublic();
        byte[] token = sign.sign();

        return bytesToHex(token);
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
