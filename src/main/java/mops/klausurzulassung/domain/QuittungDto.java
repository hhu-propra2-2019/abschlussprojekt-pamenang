package mops.klausurzulassung.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.security.PublicKey;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuittungDto {
    private String matrikelnummer;
    private String modulId;
    private PublicKey publicKey;
    private String quittung;

}
