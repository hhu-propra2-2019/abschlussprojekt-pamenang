package mops.klausurzulassung.Domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.index.qual.LowerBoundBottom;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@Entity(name = "students")
@NoArgsConstructor
public class Student {

  @Column(name="Vorname")
  private String vorname;

  @Column(name="Nachname")
  private String nachname;

  @Column(name="Email")
  private String email;

  @Id
  @Column(name="Matrikelnummer")
  private Long matrikelnummer;

  @Column(name="ModulID")
  private Long modulId;

  @Column(name="Fachname")
  private String fachname;

  @Lob
  @Column(name="Token")
  private String token;

}

