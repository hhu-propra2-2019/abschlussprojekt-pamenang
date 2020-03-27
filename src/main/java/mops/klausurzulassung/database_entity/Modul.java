package mops.klausurzulassung.database_entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "moduls")
@AllArgsConstructor
@NoArgsConstructor
public class Modul {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "owner")
  private String owner;

  @Column(name = "frist")
  @DateTimeFormat(pattern = "MM/dd/yyyy hh:mm")
  private String frist;

  @Column(name = "active")
  private Boolean active;

  @Column(name = "teilnehmer")
  private Long teilnehmer;
}
