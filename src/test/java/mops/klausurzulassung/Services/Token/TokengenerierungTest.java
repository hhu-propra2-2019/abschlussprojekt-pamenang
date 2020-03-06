package mops.klausurzulassung.Services.Token;

import mops.klausurzulassung.Services.Token.Repositories.QuittungRepository;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class TokengenerierungTest {



  @Test
  public void testErstellenHashValue() {
    String matr = "3333333";
    String fach = "propra1";
    QuittungService quittungService = mock(QuittungService.class);

    TokengenerierungService tg = new TokengenerierungService(quittungService);
    String ergebnis = tg.erstellenHashValue(matr, fach);

    assertEquals(ergebnis, "3333333propra1");
  }

  @Test
  public void testStudentenToken() {
    String matr = "3333333";
    String fach = "propra1";
    String token = "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3";
    QuittungService quittungService = mock(QuittungService.class);

    TokengenerierungService tg = new TokengenerierungService(quittungService);
    String ergebnis = tg.erstellenQuittung(matr, fach, token);

    assertEquals(
        ergebnis, "3333333propra1a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3");
  }

  @Test
  public void testBytesToHex(){
    QuittungService quittungService = mock(QuittungService.class);

    byte[] bytes ={10};
    TokengenerierungService tg = new TokengenerierungService(quittungService);
    String test = tg.bytesToHex(bytes);

    assertEquals(test,"0A");

  }

  @Test
  public void testTokengenerierung() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    QuittungRepository repository = mock(QuittungRepository.class);
    QuittungService quittungService = new QuittungService(repository);
    String matr = "1234567";
    String fachID = "123";
    TokengenerierungService tg = new TokengenerierungService(quittungService);

    String token = tg.erstellenToken(matr, fachID);

    assertThat(token).isNotNull();
    verify(repository,times(1)).save(any());
  }

}
