package mops.klausurzulassung.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AltzulassungStudentDto {

  @NotNull
  @NotEmpty
  private String vorname;

  @NotNull
  @NotEmpty
  private String nachname;

  @NotNull
  @NotEmpty
  private String email;

  @NotNull
  private Long matrikelnummer;

  private Long modulId;
}
