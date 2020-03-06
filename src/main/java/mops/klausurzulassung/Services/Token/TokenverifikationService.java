package mops.klausurzulassung.Services.Token;

import mops.klausurzulassung.Services.Token.Entities.Quittung;
import mops.klausurzulassung.Services.Token.Services.QuittungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.*;

@Service
public class TokenverifikationService {

    private final QuittungService quittungService;

    @Autowired
    public TokenverifikationService(QuittungService quittungService) {
        this.quittungService = quittungService;
    }

    public boolean verifikationToken(String matr, String fachID, String token) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        String HashValue = matr+fachID;
        PublicKey publicKey = quittungService.findQuittung(matr, fachID);
        if(publicKey == null){
            return false;
        }

        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(publicKey);
        byte[] hashValueBytes = HashValue.getBytes(StandardCharsets.UTF_8);
        sign.update(hashValueBytes);

        byte[] tokenByte = hexStringToByteArray(token);
        return sign.verify(tokenByte);
    }

    byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
