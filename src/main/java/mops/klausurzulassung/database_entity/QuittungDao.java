package mops.klausurzulassung.database_entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
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

  @Column(name = "ModuId")
  private String modulId;

  @Column(name = "PublicKey")
  @Lob
  private PublicKey publicKey;

  @Column(name = "Quittung")
  @Lob
  private String quittung;

  @Id
  @GeneratedValue
  private int id;
}
