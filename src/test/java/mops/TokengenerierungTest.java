package mops;

import mops.klausurzulassung.Token.Tokengenerierung;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokengenerierungTest {

    @Test
    public void testHashing(){
        String testValue = "123";
        String testHash = "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3";

        String hash = Tokengenerierung.hashing(testValue);

        assertEquals(hash, testHash);
    }

    @Test
    public void testErstellenHashValue(){
        String matr = "3333333";
        String fach = "propra1";
        String key = "key";

        String ergebnis = Tokengenerierung.erstellenHashValue(matr, fach, key);

        assertEquals(ergebnis, "3333333propra1key");
    }

    @Test
    public void testStudentenToken(){
        String matr = "3333333";
        String fach = "propra1";
        String token = "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3";

        String ergebnis = Tokengenerierung.erstellenQuittung(matr, fach, token);

        assertEquals(ergebnis, "3333333propra1a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3");
    }

    @Test
    public void testTokengenerierung(){
        String matr = "1111111";
        String fach = "analysis1";
        String key = "keykey";

        String hashValue = Tokengenerierung.erstellenHashValue(matr, fach, key);
        String token = Tokengenerierung.hashing(hashValue);
        String quittung = Tokengenerierung.erstellenQuittung(matr, fach, token);

        assertEquals(quittung, "1111111analysis1bde7f92b46a2fad549b0449eca8314ff5458884e2c6e1272b7d1aff56cb215f3");
    }
}
