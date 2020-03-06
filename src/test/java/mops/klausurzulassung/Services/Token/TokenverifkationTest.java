package mops.klausurzulassung.Services.Token;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TokenverifkationTest {

    @Test
    public void testHexStringToByteArray(){
        String hexString = "0A";
        byte b = 10;
        TokenverifikationService tv = new TokenverifikationService();
        byte[] bytes = tv.hexStringToByteArray(hexString);

        assertEquals(bytes[0], b);
    }
}
