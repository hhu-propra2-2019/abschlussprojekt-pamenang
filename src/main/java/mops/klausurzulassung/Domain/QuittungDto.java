package mops.klausurzulassung.Domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.security.PublicKey;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuittungDto {
    private PublicKey publicKey;
    private String quittung;

}
