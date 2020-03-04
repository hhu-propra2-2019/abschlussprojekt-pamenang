package mops.klausurzulassung.Token;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public class Tokengenerierung {

    public static String hashing(String HashValue){

        Hasher hasher = Hashing.sha256().newHasher();
        hasher.putString(HashValue, Charsets.UTF_8);
        HashCode token = hasher.hash();

        return token.toString();
    }

    public static String erstellenHashValue(String matr, String fach, String key){
        return matr+fach+key;
    }

    public static String erstellenStudenttoken(String matr, String fach, String token){
        return matr+fach+token;
    }
}
