package mops.klausurzulassung.Services.Token.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.security.PublicKey;

@Data
@AllArgsConstructor
@Entity
@Table(name = "quittungen")
public class Quittung {

    @Column(name = "Matrikelnummer")
    private String matrikelnummer;

    @Column(name = "FachID")
    private String fachID;

    @Column(name = "PublicKey")
    private PublicKey publicKey;

    @Column(name = "Token")
    private String token;
}
