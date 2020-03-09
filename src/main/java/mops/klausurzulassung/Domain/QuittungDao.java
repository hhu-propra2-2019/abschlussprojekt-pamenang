package mops.klausurzulassung.Domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.security.PublicKey;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "quittungen")
public class QuittungDao {

    @Column(name = "Matrikelnummer")
    private String matrikelnummer;

    @Column(name = "FachID")
    private String fachID;

    @Column(name = "PublicKey")
    private PublicKey publicKey;


    @Column(name = "Token")
    private String token;

    @Id
    @GeneratedValue
    private int id;
}
