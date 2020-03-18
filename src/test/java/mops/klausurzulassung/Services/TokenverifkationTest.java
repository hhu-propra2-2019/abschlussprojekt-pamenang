package mops.klausurzulassung.Services;


import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenverifkationTest {

  private QuittungService quittungService;
  private TokenverifikationService tokenverifikationService;

  @BeforeEach
  void setUp() {
    this.quittungService = mock(QuittungService.class);
    this.tokenverifikationService = new TokenverifikationService(quittungService);
  }

  @Test
  void test_tokenVerifikation_shouldReturnTrue() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMmHAssfzLds1MyR0LXFyetlKFPShZXBbsNFSJVmFe21YIMKmSZkmiaWgMudOFo0lCxiSF/w9SrymA9T6tFQ6KkCAwEAAQ==";
    String quittung = "WlPnhwXQlOsO4Ez3cosVsqaMVDK5IJMzTsfeRmlEwC2CCLLIDXTniqcuQHua5HKejcuY6SyAwjiQrfQY7iJAsQ==§MTIzNDU1MjM3NDg3§MTI=";
    String matrikelnummer = "123455237487";
    String fachId = "12";
    boolean valid = false;
    when(quittungService.findPublicKeyByQuittung(any(),any())).thenReturn(getKey(publicKey));

    long[] validToken = tokenverifikationService.verifikationToken(quittung);
    if(validToken[0]>0){
      valid = true;
    }

    assertTrue(valid);
  }

  @Test
  void test_tokenVerifikation_publicKeyIstInvalide_shouldReturnFalse() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSTJBAMmHAssfzLds1MyR0LXFyetlKFPShZXBbsNFSJVmFe21YIMKmSZkmiaWgMudOFo0lCxiSF/w9SrymA9T6tFQ6KkCAwEAAQ==";
    String quittung = "WlPnhwXQlOsO4Ez3cosVsqaMVDK5IJMzTsfeRmlEwC2CCLLIDXTniqcuQHua5HKejcuY6SyAwjiQrfQY7iJAsQ==§MTIzNDU1MjM3NDg3§MTI=";
    String matrikelnummer = "123455237487";
    String fachId = "12";
    boolean valid = false;
    when(quittungService.findPublicKeyByQuittung(any(),any())).thenReturn(getKey(publicKey));

    long[] validToken = tokenverifikationService.verifikationToken(quittung);
    if(validToken[0] > 0){
      valid = true;
    }

    assertFalse(valid);
  }

  @Test
  void test_tokenVerifikation_tokenIstInvalide_shouldReturnFalse() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMmHAssfzLds1MyR0LXFyetlKFPShZXBbsNFSJVmFe21YIMKmSZkmiaWgMudOFo0lCxiSF/w9SrymA9T6tFQ6KkCAwEAAQ==";
    String quittung = "WlPnhwXQlOsO4Ez3cosVsqaMVDK5IJMzTsfeRmlEwC2CALLIDXTniqcuQHua5HKejcuY6SyAwjiQrfQY7iJAsQ==§MTIzNDU1MjM3NDg3§MTI=";
    String matrikelnummer = "123455237487";
    String fachId = "12";
    boolean valid = false;
    when(quittungService.findPublicKeyByQuittung(any(),any())).thenReturn(getKey(publicKey));

    long[] validToken = tokenverifikationService.verifikationToken(quittung);
    if(validToken[0]>0){
      valid = true;
    }
    assertFalse(valid);
  }

    @Test
    void tokenIstZuKurz() throws NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, SignatureException {
        String token = "IM";
        String matrikelnummer = "1234567";
        String fachId = "17";

    //    boolean validToken = tokenverifikationService.verifikationToken(matrikelnummer, fachId, token);

    //    assertFalse(validToken);
    }

  @Test
  void test_tokenVerifikation_publicKeyIsNUll() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
    String quittung = "WlPnhwXQlOsO4Ez3cosVsqaMVDK5IJMzTsfeRmlEwC2CCLLIDXTniqcuQHua5HKejcuY6SyAwjiQrfQY7iJAsQ==§MTIzNDU1MjM3NDg3§MTI=";
    String matrikelnummer = "12345523747";
    String fachId = "13";
    boolean valid = false;
    when(quittungService.findPublicKeyByQuittung(any(),any())).thenReturn(null);

    long[] validToken = tokenverifikationService.verifikationToken(quittung);
    if(validToken[0]>0){
      valid = true;
    }
    assertFalse(valid);
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
