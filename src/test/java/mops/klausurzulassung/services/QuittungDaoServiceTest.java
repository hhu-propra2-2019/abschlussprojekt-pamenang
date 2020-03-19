package mops.klausurzulassung.services;

import mops.klausurzulassung.exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.database_entity.QuittungDao;
import mops.klausurzulassung.exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.repositories.QuittungRepository;
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
        String modulID = "1111";

        //Erstellung des KeyPairs
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        //Einsetzen des PublicKeys in die Quittung
        PublicKey pK = keyPairGenerator.generateKeyPair().getPublic();
        QuittungDao quittungDao = new QuittungDao(matr, modulID, pK, "1324235",1);

        //Suchen des PublicKeys in QuittungRepository
        when(quittungRepository.findByMatrikelnummerAndModulId(matr,modulID)).thenReturn(quittungDao);
        PublicKey publicKey = quittungService.findPublicKey(matr, modulID);

        assertEquals(pK, publicKey);

    }

    @Test
    public void testFindTokenByQuittung() throws NoSuchAlgorithmException, NoTokenInDatabaseException {

        QuittungRepository quittungRepository = mock(QuittungRepository.class);
        QuittungService quittungService = new QuittungService(quittungRepository);
        String matr = "1234567";
        String modulID = "1111";


        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        PublicKey pK = keyPairGenerator.generateKeyPair().getPublic();
        QuittungDao quittungDao = new QuittungDao(matr, modulID, pK, "1324235",1);

        when(quittungRepository.findByMatrikelnummerAndModulId(matr,modulID)).thenReturn(quittungDao);
        String quittung = quittungService.findQuittung(matr, modulID);

        assertEquals("1324235", quittung);


    }
}
