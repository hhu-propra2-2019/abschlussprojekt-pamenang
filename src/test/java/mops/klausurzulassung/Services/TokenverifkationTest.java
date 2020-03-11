package mops.klausurzulassung.Services;

import mops.klausurzulassung.Repositories.QuittungRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;


class TokenverifkationTest {

    @Test
    void testAtToSlash(){
        String at = "@";
        QuittungRepository quittungRepository = mock(QuittungRepository.class);
        QuittungService quittungService = new QuittungService(quittungRepository);
        TokenverifikationService tokenverifikationService = new TokenverifikationService(quittungService);

        String slash = tokenverifikationService.atToSlash(at);

        assertEquals("/",slash);

    }

}
