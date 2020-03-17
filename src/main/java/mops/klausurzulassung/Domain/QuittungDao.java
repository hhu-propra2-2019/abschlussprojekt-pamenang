package mops.klausurzulassung.Domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import java.security.PublicKey;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "quittungen")
public class QuittungDao {

  @Column(name = "PublicKey")
  @Lob
  private PublicKey publicKey;

  @Column(name = "Quittung")
  @Lob
  private String quittung;

  @Id @GeneratedValue private int id;
}
