package mops.klausurzulassung.Token;

public class Tokenverifikation {

    //WICHTIG!!!
    //Key muss noch angepasst werden!
    public static boolean verifikation(String matr, String fachID, String token){
        String hashValue = Tokengenerierung.erstellenHashValue(matr, fachID, /*Key*/ "");
        String varToken = Tokengenerierung.hashing(hashValue);
        return varToken.equals(token);
    }
}
