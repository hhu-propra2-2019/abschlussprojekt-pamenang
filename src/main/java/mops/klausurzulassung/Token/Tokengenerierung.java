package mops.klausurzulassung.Token;

import java.nio.charset.StandardCharsets;
import java.security.*;

public class Tokengenerierung {

    public static String erstellenHashValue(String matr, String fach){
        return matr+fach;
    }

    public static String erstellenQuittung(String matr, String fach, String token){
        return matr+fach+token;
    }

    public static String signatur(String HashValue) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        KeyPair pair = KeyPaarGenerierung();
        PrivateKey privKey = pair.getPrivate();
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privKey);
        byte[] bytes = HashValue.getBytes(StandardCharsets.UTF_8);
        sign.update(bytes);

        //Speicher Student + Public Key ab
        PublicKey publicKey = pair.getPublic();

        byte[] signature = sign.sign();
        return bytesToHex(signature);

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
