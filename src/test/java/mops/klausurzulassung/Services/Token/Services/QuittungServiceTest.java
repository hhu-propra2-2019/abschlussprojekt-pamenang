package mops.klausurzulassung.Services.Token.Services;

import mops.klausurzulassung.Services.Token.Entities.Quittung;
import mops.klausurzulassung.Services.Token.Repositories.QuittungRepository;
import org.junit.jupiter.api.Test;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class QuittungServiceTest {

    @Test
    public void testFindPublicKeyByQuittung() throws NoSuchAlgorithmException {
        QuittungRepository quittungRepository = mock(QuittungRepository.class);
        QuittungService quittungService = new QuittungService(quittungRepository);
        String matr = "1234567";
        String fachID = "1111";

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        PublicKey pK = keyPairGenerator.generateKeyPair().getPublic();
        Quittung quittung = new Quittung(matr, fachID, pK, "1324235");

        Quittung[] quittungen = {quittung};
        when(quittungRepository.findAll()).thenReturn(Arrays.asList(quittungen));
        PublicKey publicKey = quittungService.findPublicKeyByQuittung(matr, fachID);

        assertEquals(pK, publicKey);

    }
}
