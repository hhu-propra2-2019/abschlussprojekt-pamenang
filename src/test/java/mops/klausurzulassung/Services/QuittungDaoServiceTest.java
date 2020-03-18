package mops.klausurzulassung.Services;

import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Domain.QuittungDao;
import mops.klausurzulassung.Exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.Repositories.QuittungRepository;
import org.junit.jupiter.api.Test;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuittungDaoServiceTest {

    @Test
    public void testFindPublicKeyByQuittung() throws NoSuchAlgorithmException, NoPublicKeyInDatabaseException {
        QuittungRepository quittungRepository = mock(QuittungRepository.class);
        QuittungService quittungService = new QuittungService(quittungRepository);
        String matr = "1234567";
        String fachID = "1111";

        //Erstellung des KeyPairs
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        //Einsetzen des PublicKeys in die Quittung
        PublicKey pK = keyPairGenerator.generateKeyPair().getPublic();
        QuittungDao quittungDao = new QuittungDao(matr, fachID, pK, "1324235",1);

        //Suchen des PublicKeys in QuittungRepository




        when(quittungRepository.findByMatrikelnummerAndModulId(matr,fachID)).thenReturn(quittungDao);
        PublicKey publicKey = quittungService.findPublicKey(matr, fachID);

        assertEquals(pK, publicKey);

    }

    @Test
    public void testFindTokenByQuittung() throws NoSuchAlgorithmException, NoTokenInDatabaseException {

        QuittungRepository quittungRepository = mock(QuittungRepository.class);
        QuittungService quittungService = new QuittungService(quittungRepository);
        String matr = "1234567";
        String fachID = "1111";


        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        PublicKey pK = keyPairGenerator.generateKeyPair().getPublic();
        QuittungDao quittungDao = new QuittungDao(matr, fachID, pK, "1324235",1);

        when(quittungRepository.findByMatrikelnummerAndModulId(matr,fachID)).thenReturn(quittungDao);
        String token = quittungService.findQuittung(matr, fachID);

        assertEquals("1324235", token);


    }
}
