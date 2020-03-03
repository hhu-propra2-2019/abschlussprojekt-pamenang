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
}
