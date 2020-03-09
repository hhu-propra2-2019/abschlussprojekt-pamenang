package mops.klausurzulassung.Services.Token.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.security.PublicKey;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuittungDto {
    private String matrikelnummer;
    private String fachID;
    private PublicKey publicKey;
    private String token;

}
