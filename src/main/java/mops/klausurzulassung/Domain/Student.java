package mops.klausurzulassung.Domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "students")
public class Student {

  private String vorname;
  private String nachname;
  private String email;
  @Id private Long matrikelnummer;
  private Long modulId;
  private String fachname;
  @Lob private String token;
}
