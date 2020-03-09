package mops.klausurzulassung.Domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@Entity(name = "students")
@NoArgsConstructor
public class Student {

  private String vorname;
  private String nachname;
  private String email;
  @Id
  private Long matrikelnummer;
  private Long modulId;
  private String fachname;
  private String token;

}

