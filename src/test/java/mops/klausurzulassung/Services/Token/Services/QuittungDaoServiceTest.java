package mops.klausurzulassung.Services.Token.Services;

import mops.klausurzulassung.Services.Token.Entities.QuittungDao;
import mops.klausurzulassung.Services.Token.Repositories.QuittungRepository;
import org.junit.jupiter.api.Test;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuittungDaoServiceTest {

    @Test
    public void testFindPublicKeyByQuittung() throws NoSuchAlgorithmException {
        QuittungRepository quittungRepository = mock(QuittungRepository.class);
        QuittungService quittungService = new QuittungService(quittungRepository);
        String matr = "1234567";
        String fachID = "1111";

        //Erstellung des KeyPairs
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        //Einsetzen des PublicKeys in die Quittung
        PublicKey pK = keyPairGenerator.generateKeyPair().getPublic();
        QuittungDao quittungDao = new QuittungDao(matr, fachID, pK, "1324235");

        //Suchen des PublicKeys in QuittungRepository
        QuittungDao[] quittungen = {quittungDao};
        when(quittungRepository.findAll()).thenReturn(Arrays.asList(quittungen));
        PublicKey publicKey = quittungService.findPublicKeyByQuittung(matr, fachID);

        assertEquals(pK, publicKey);

    }
}
