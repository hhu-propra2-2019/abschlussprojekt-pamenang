package mops.klausurzulassung.Services.Token.Entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "quittungen")
public class Quittung {

    @Column(name = "Matrikelnummer")
    private String matrikelnummer;

    @Column(name = "FachID")
    private String fachID;

    @Column(name = "PublicKey")
    private String publicKey;

    @Column(name = "Token")
    private String token;
}
