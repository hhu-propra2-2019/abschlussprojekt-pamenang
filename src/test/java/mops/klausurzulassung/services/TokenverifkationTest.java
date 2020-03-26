package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.Modul;
import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.exceptions.InvalidFrist;
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
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

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
  private ModulService modulService;

  @BeforeEach
  void setUp() {
    this.quittungService = mock(QuittungService.class);
    this.studentService = mock(StudentService.class);
    this.modulService = mock(ModulService.class);
    this.tokenverifikationService = new TokenverifikationService(quittungService, studentService, modulService);
  }

  @Test
  void test_tokenVerifikation_shouldReturnTrue() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidToken, InvalidFrist {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMmHAssfzLds1MyR0LXFyetlKFPShZXBbsNFSJVmFe21YIMKmSZkmiaWgMudOFo0lCxiSF/w9SrymA9T6tFQ6KkCAwEAAQ==";
    String quittung = "WlPnhwXQlOsO4Ez3cosVsqaMVDK5IJMzTsfeRmlEwC2CCLLIDXTniqcuQHua5HKejcuY6SyAwjiQrfQY7iJAsQ==§MTIzNDU1MjM3NDg3§MTI=";
    long matrikelnummer = 123455237487L;
    long modulID = 12L;
    Student student = new Student(null, null, null, matrikelnummer, modulID, null, null);
    String frist = fristInZukunft();
    Modul modul = new Modul(12L, "ProPra2", "orga", frist, true, 140L);

    when(modulService.findById(12L)).thenReturn(Optional.of(modul));

    when(quittungService.findPublicKey(any(),any())).thenReturn(getKey(publicKey));
    tokenverifikationService.verifikationToken(quittung);

    verify(studentService, times(1)).save(student);
  }

  @Test
  void test_tokenVerifikation_publicKeyIstInvalide_shouldReturnFalse() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidToken, InvalidFrist {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSTJBAMmHAssfzLds1MyR0LXFyetlKFPShZXBbsNFSJVmFe21YIMKmSZkmiaWgMudOFo0lCxiSF/w9SrymA9T6tFQ6KkCAwEAAQ==";
    String quittung = "WlPnhwXQlOsO4Ez3cosVsqaMVDK5IJMzTsfeRmlEwC2CCLLIDXTniqcuQHua5HKejcuY6SyAwjiQrfQY7iJAsQ==§MTIzNDU1MjM3NDg3§MTI=";

    String frist = fristInZukunft();
    Modul modul = new Modul(12L, "ProPra2", "orga", frist, true, 140L);

    when(modulService.findById(12L)).thenReturn(Optional.of(modul));

    when(quittungService.findPublicKey(any(),any())).thenReturn(getKey(publicKey));
    tokenverifikationService.verifikationToken(quittung);

    verify(studentService, times(0)).save(any());
  }

  @Test
  void test_tokenVerifikation_tokenIstInvalide_shouldReturnFalse() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidToken, InvalidFrist {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMmHAssfzLds1MyR0LXFyetlKFPShZXBbsNFSJVmFe21YIMKmSZkmiaWgMudOFo0lCxiSF/w9SrymA9T6tFQ6KkCAwEAAQ==";
    String quittung = "WlPnhwXQlOsO4Ez3cosVsqaMVDK5IJMzTsfeRmlEwC2CALLIDXTniqcuQHua5HKejcuY6SyAwjiQrfQY7iJAsQ==§MTIzNDU1MjM3NDg3§MTI=";

    String frist = fristInZukunft();
    Modul modul = new Modul(12L, "ProPra2", "orga", frist, true, 140L);

    when(modulService.findById(12L)).thenReturn(Optional.of(modul));
    when(quittungService.findPublicKey(any(),any())).thenReturn(getKey(publicKey));
    tokenverifikationService.verifikationToken(quittung);

    verify(studentService, times(0)).save(any());
  }

    @Test
    void tokenIstZuKurz() {
      String quittung = "IM";

      try {
        tokenverifikationService.verifikationToken(quittung);
        fail();
      } catch (Exception e) {
        assertThat(e.toString().contains("Token fehlerhaft"));
      }
    }



  @Test
  void test_tokenVerifikation_publicKeyIsNUll() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidToken, InvalidFrist {
    String quittung = "WlPnhwXQlOsO4Ez3cosVsqaMVDK5IJMzTsfeRmlEwC2CCLLIDXTniqcuQHua5HKejcuY6SyAwjiQrfQY7iJAsQ==§MTIzNDU1MjM3NDg3§MTI=";

    String frist = fristInZukunft();
    Modul modul = new Modul(13L, "ProPra2", "orga", frist, true, 140L);

    when(modulService.findById(any())).thenReturn(Optional.of(modul));
    when(quittungService.findPublicKey(any(),any())).thenReturn(null);
    tokenverifikationService.verifikationToken(quittung);

    verify(studentService, times(0)).save(any());
  }

  @Test
  void invalidFrist() throws NoPublicKeyInDatabaseException, ParseException {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMmHAssfzLds1MyR0LXFyetlKFPShZXBbsNFSJVmFe21YIMKmSZkmiaWgMudOFo0lCxiSF/w9SrymA9T6tFQ6KkCAwEAAQ==";
    String quittung = "WlPnhwXQlOsO4Ez3cosVsqaMVDK5IJMzTsfeRmlEwC2CCLLIDXTniqcuQHua5HKejcuY6SyAwjiQrfQY7iJAsQ==§MTIzNDU1MjM3NDg3§MTI=";
    long matrikelnummer = 123455237487L;
    long modulID = 12L;
    String frist = "03/03/2000 12:00";
    Modul modul = new Modul(12L, "ProPra2", "orga", frist, true, 140L);

    when(modulService.findById(12L)).thenReturn(Optional.of(modul));
    when(modulService.isFristAbgelaufen(any())).thenReturn(true);
    when(quittungService.findPublicKey(any(), any())).thenReturn(getKey(publicKey));

    try {
      tokenverifikationService.verifikationToken(quittung);
    } catch (Exception e) {
      assertThat(e.toString().contains("Frist ist abgelaufen!"));
    }
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

  private String fristInZukunft() {
    LocalDateTime now = LocalDateTime.now().withNano(0).withSecond(0);
    LocalDateTime future = now.plusYears(1);
    int year = future.getYear();
    int month = future.getMonthValue();
    int day = future.getDayOfMonth();
    return month + "/" + day + "/" + year;
  }
}
