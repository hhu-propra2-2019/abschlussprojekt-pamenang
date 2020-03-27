package mops.klausurzulassung.database_entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "students")
public class Student {

  @Column(name = "Vorname")
  private String vorname;


  @Column(name = "Nachname")
  private String nachname;


  @Column(name = "Email")
  private String email;


  @Id
  @Column(name = "Matrikelnummer")
  private Long matrikelnummer;


  @Column(name = "ModulID")
  private Long modulId;

  @Column(name = "Fachname")
  private String fachname;


  @Lob
  @Column(name = "Token")
  private String token;

}
