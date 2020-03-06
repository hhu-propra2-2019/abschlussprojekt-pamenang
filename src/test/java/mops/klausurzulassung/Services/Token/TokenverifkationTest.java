package mops.klausurzulassung.Services.Token;

import mops.klausurzulassung.Services.Token.Repositories.QuittungRepository;
import mops.klausurzulassung.Services.Token.Services.QuittungService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;


public class TokenverifkationTest {

    @Test
    public void testHexStringToByteArray(){
        QuittungService quittungService = mock(QuittungService.class);
        String hexString = "0A";
        byte b = 10;
        TokenverifikationService tv = new TokenverifikationService(quittungService);
        byte[] bytes = tv.hexStringToByteArray(hexString);

        assertEquals(bytes[0], b);
    }
}
