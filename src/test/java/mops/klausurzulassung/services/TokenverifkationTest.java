package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.exceptions.InvalidToken;
import mops.klausurzulassung.exceptions.NoPublicKeyInDatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class TokenverifkationTest {

  private QuittungService quittungService;
  private TokenverifikationService tokenverifikationService;
  private StudentService studentService;

  @BeforeEach
  void setUp() {
    this.quittungService = mock(QuittungService.class);
    this.studentService = mock(StudentService.class);
    this.tokenverifikationService = new TokenverifikationService(quittungService, studentService);
  }

  @Test
  void test_tokenVerifikation_shouldReturnTrue() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidToken {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMmHAssfzLds1MyR0LXFyetlKFPShZXBbsNFSJVmFe21YIMKmSZkmiaWgMudOFo0lCxiSF/w9SrymA9T6tFQ6KkCAwEAAQ==";
    String quittung = "WlPnhwXQlOsO4Ez3cosVsqaMVDK5IJMzTsfeRmlEwC2CCLLIDXTniqcuQHua5HKejcuY6SyAwjiQrfQY7iJAsQ==§MTIzNDU1MjM3NDg3§MTI=";
    long matrikelnummer = 123455237487L;
    long modulID = 12L;
    Student student = new Student(null, null, null, matrikelnummer, modulID, null, null);

    when(quittungService.findPublicKey(any(),any())).thenReturn(getKey(publicKey));
    tokenverifikationService.verifikationToken(quittung);

    verify(studentService, times(1)).save(student);
  }

  @Test
  void test_tokenVerifikation_publicKeyIstInvalide_shouldReturnFalse() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidToken {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSTJBAMmHAssfzLds1MyR0LXFyetlKFPShZXBbsNFSJVmFe21YIMKmSZkmiaWgMudOFo0lCxiSF/w9SrymA9T6tFQ6KkCAwEAAQ==";
    String quittung = "WlPnhwXQlOsO4Ez3cosVsqaMVDK5IJMzTsfeRmlEwC2CCLLIDXTniqcuQHua5HKejcuY6SyAwjiQrfQY7iJAsQ==§MTIzNDU1MjM3NDg3§MTI=";
    String matrikelnummer = "123455237487";
    String modulID = "12";

    when(quittungService.findPublicKey(any(),any())).thenReturn(getKey(publicKey));
    tokenverifikationService.verifikationToken(quittung);

    verify(studentService, times(0)).save(any());
  }

  @Test
  void test_tokenVerifikation_tokenIstInvalide_shouldReturnFalse() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidToken {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMmHAssfzLds1MyR0LXFyetlKFPShZXBbsNFSJVmFe21YIMKmSZkmiaWgMudOFo0lCxiSF/w9SrymA9T6tFQ6KkCAwEAAQ==";
    String quittung = "WlPnhwXQlOsO4Ez3cosVsqaMVDK5IJMzTsfeRmlEwC2CALLIDXTniqcuQHua5HKejcuY6SyAwjiQrfQY7iJAsQ==§MTIzNDU1MjM3NDg3§MTI=";
    String matrikelnummer = "123455237487";
    String modulID = "12";

    when(quittungService.findPublicKey(any(),any())).thenReturn(getKey(publicKey));
    tokenverifikationService.verifikationToken(quittung);

    verify(studentService, times(0)).save(any());
  }

    @Test
    void tokenIstZuKurz() {
      String quittung = "IM";
      String matrikelnummer = "1234567";
      String modulID = "17";
      try {
        tokenverifikationService.verifikationToken(quittung);
        fail();
      } catch (Exception e) {
        assertThat(e.toString().contains("Token fehlerhaft"));
      }
    }



  @Test
  void test_tokenVerifikation_publicKeyIsNUll() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidToken {
    String quittung = "WlPnhwXQlOsO4Ez3cosVsqaMVDK5IJMzTsfeRmlEwC2CCLLIDXTniqcuQHua5HKejcuY6SyAwjiQrfQY7iJAsQ==§MTIzNDU1MjM3NDg3§MTI=";
    String matrikelnummer = "12345523747";
    String modulID = "13";

    when(quittungService.findPublicKey(any(),any())).thenReturn(null);
    tokenverifikationService.verifikationToken(quittung);

    verify(studentService, times(0)).save(any());
  }


    static PublicKey getKey(String key) {
    try{
      byte[] byteKey = Base64.getDecoder().decode(key.getBytes());
      X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
      KeyFactory kf = KeyFactory.getInstance("RSA");

      return kf.generatePublic(X509publicKey);
    }
    catch(Exception e){
      e.printStackTrace();
    }

    return null;
  }
}
