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
}
