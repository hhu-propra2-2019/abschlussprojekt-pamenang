package mops.klausurzulassung.Token;

import java.security.*;

public class Tokenverifikation {

    static PublicKey pk;
    //WICHTIG!!!
    //Key muss noch angepasst werden!

    public static boolean verifikation(String matr, String fachID, String token) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        Signature sign = Signature.getInstance("SHA256withRSA");


        byte[] bytes = hexStringToByteArray(token);


        sign.initVerify(pk);
        sign.update(bytes);



        System.out.println(sign.verify(bytes));
        return sign.verify(bytes);


    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
