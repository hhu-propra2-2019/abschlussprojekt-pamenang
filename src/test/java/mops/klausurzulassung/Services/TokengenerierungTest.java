package mops.klausurzulassung.Services;

import mops.klausurzulassung.Repositories.QuittungRepository;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

class TokengenerierungTest {



  @Test
  void testErstellenHashValue() {
    String matr = "3333333";
    String modulID = "1";
    QuittungService quittungService = mock(QuittungService.class);

    TokengenerierungService tg = new TokengenerierungService(quittungService);
    String ergebnis = tg.erstellenHashValue(matr, modulID);

    assertEquals(ergebnis, "33333331");
  }

  @Test
  void testTokengenerierung() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    QuittungRepository repository = mock(QuittungRepository.class);
    QuittungService quittungService = new QuittungService(repository);
    String matr = "1234567";
    String modulID = "123";
    TokengenerierungService tg = new TokengenerierungService(quittungService);

    String token = tg.erstellenToken(matr, modulID);

    assertThat(token).isNotNull();
    verify(repository,times(1)).save(any());
  }
}
