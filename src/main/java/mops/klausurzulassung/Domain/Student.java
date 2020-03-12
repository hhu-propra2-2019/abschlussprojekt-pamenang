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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "students")
public class Student {

  @Column(name="Vorname")
  @NotNull
  @NotEmpty
  private String vorname;

  @NotNull
  @NotEmpty
  @Column(name="Nachname")
  private String nachname;

  @NotNull
  @NotEmpty
  @Column(name="Email")
  private String email;

  @NotNull
  @Id
  @Column(name="Matrikelnummer")
  private Long matrikelnummer;

  @NotNull
  @Column(name="ModulID")
  private Long modulId;

  @NotEmpty
  @NotNull
  @Column(name="Fachname")
  private String fachname;

  @NotNull
  @NotEmpty
  @Lob
  @Column(name="Token")
  private String token;

}
