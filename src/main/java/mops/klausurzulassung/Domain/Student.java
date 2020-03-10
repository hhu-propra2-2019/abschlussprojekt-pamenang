package mops.klausurzulassung.Domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Data
@Entity(name = "students")
public class Student {

  private String vorname;
  private String nachname;
  private String email;
  @Id private Long matrikelnummer;
  private Long modulId;
  private String fachname;
  @Lob private String token;

  public Student() {}

  public Student(
      String vorname,
      String nachname,
      String email,
      Long matrikelnummer,
      Long modulId,
      String fachname,
      String token) {
    this.vorname = vorname;
    this.nachname = nachname;
    this.email = email;
    this.matrikelnummer = matrikelnummer;
    this.modulId = modulId;
    this.fachname = fachname;
    this.token = token;
  }
}
