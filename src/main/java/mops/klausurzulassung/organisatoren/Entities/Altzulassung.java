package mops.klausurzulassung.organisatoren.Entities;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "altzulassung")
public class Altzulassung {
  @Id
  private Long matrikelnummer;
  private String vorname;
  private String nachname;
  private Long modulId;
}
