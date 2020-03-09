package mops.klausurzulassung.Domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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
