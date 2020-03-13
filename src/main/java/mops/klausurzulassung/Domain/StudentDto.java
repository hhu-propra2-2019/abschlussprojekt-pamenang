package mops.klausurzulassung.Domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDto {

    @NotNull
    @NotEmpty
    @Column(name="Vorname")
    private String vorname;

    @NotNull
    @NotEmpty
    private String nachname;

    @NotNull
    @NotEmpty
    private String email;

    @Id
    @NotNull
    private Long matrikelnummer;

    @NotNull
    private Long modulId;

    @NotNull
    @NotEmpty
    private String fachname;

    @NotNull
    @NotEmpty
    private String token;
}
