package mops.klausurzulassung.Domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Student {

  private String vorname;
  private String nachname;
  private String email;
  private Long matrikelnummer;
  private Long raumId;
  private String fachname;
  private String token;

}

