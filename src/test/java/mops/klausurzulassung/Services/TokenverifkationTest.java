package mops.klausurzulassung.Services;


import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenverifkationTest {

  @Test
  public void test_tokenVerifikation_shouldReturnTrue() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIrNUbrWeAP+EmUPMR7Qz3OQXrtAjFAfeQTkKNmjztuNKmqiO9dCEY1ZFWshbu6RbrCRZsx4SZetQteMYzDIGTkCAwEAAQ==";
    String token = "IMeApu@TCn1Tnl+eob1jCG@lG4LmeVdzVTZF1mJ6KgtB@65JY1r9mHUthrNRgBW43YOr+iUXhAPyJ7bv4i2siw==";
    String matrikelnummer = "123455237487";
    String fachId = "12";
    QuittungService quittungService = mock(QuittungService.class);
    when(quittungService.findPublicKeyByQuittung(any(),any())).thenReturn(getKey(publicKey));
    TokenverifikationService tokenverifikationService = new TokenverifikationService(quittungService);

    boolean validToken = tokenverifikationService.verifikationToken(matrikelnummer,fachId,token);

    Assertions.assertThat(validToken).isTrue();
  }

  @Test
  public void test_tokenVerifikation_publicKeyIstInvalide_shouldReturnFalse() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIrNUbrWeAP+EmUPMR7Qz3OQXrtAjFBfeQTkKNmjztuNKmqiO9dCEY1ZFWshbu6RbrCRZsx4SZetQteMYzDIGTkCAwEAAQ==";
    String token = "IMeApu@TCn1Tnl+eob1jCG@lG4LmeVdzVTZF1mJ6KgtB@65JY1r9mHUthrNRgBW43YOr+iUXhAPyJ7bv4i2siw==";
    String matrikelnummer = "123455237487";
    String fachId = "12";
    QuittungService quittungService = mock(QuittungService.class);
    when(quittungService.findPublicKeyByQuittung(any(),any())).thenReturn(getKey(publicKey));
    TokenverifikationService tokenverifikationService = new TokenverifikationService(quittungService);

    boolean validToken = tokenverifikationService.verifikationToken(matrikelnummer,fachId,token);

    Assertions.assertThat(validToken).isFalse();
  }

  @Test
  public void test_tokenVerifikation_tokenIstInvalide_shouldReturnFalse() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIrNUbrWeAP+EmUPMR7Qz3OQXrtAjFAfeQTkKNmjztuNKmqiO9dCEY1ZFWshbu6RbrCRZsx4SZetQteMYzDIGTkCAwEAAQ==";
    String token = "IMeApu@TCn1Tnl+eob1jCG@lG4LmeVdzVTZF1mJ6KgtB@65JZ1r9mHUthrNRgBW43YOr+iUXhAPyJ7bv4i2siw==";
    String matrikelnummer = "123455237487";
    String fachId = "12";
    QuittungService quittungService = mock(QuittungService.class);
    when(quittungService.findPublicKeyByQuittung(any(),any())).thenReturn(getKey(publicKey));
    TokenverifikationService tokenverifikationService = new TokenverifikationService(quittungService);

    boolean validToken = tokenverifikationService.verifikationToken(matrikelnummer,fachId,token);

    Assertions.assertThat(validToken).isFalse();
  }

  @Test
  public void test_tokenVerifikation_matrikelnummerUndfachIdIstInvalide_shouldReturnFalse() throws NoPublicKeyInDatabaseException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIrNUbrWeAP+EmUPMR7Qz3OQXrtAjFAfeQTkKNmjztuNKmqiO9dCEY1ZFWshbu6RbrCRZsx4SZetQteMYzDIGTkCAwEAAQ==";
    String token = "IMeApu@TCn1Tnl+eob1jCG@lG4LmeVdzVTZF1mJ6KgtB@65JY1r9mHUthrNRgBW43YOr+iUXhAPyJ7bv4i2siw==";
    String matrikelnummer = "12345523747";
    String fachId = "13";
    QuittungService quittungService = mock(QuittungService.class);
    when(quittungService.findPublicKeyByQuittung(any(),any())).thenReturn(getKey(publicKey));
    TokenverifikationService tokenverifikationService = new TokenverifikationService(quittungService);

    boolean validToken = tokenverifikationService.verifikationToken(matrikelnummer,fachId,token);

    Assertions.assertThat(validToken).isFalse();
  }

  public static PublicKey getKey(String key){
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
