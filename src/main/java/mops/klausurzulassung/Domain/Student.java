package mops.klausurzulassung.Domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Student {

    private String vorname;
    private String nachname;
    private String email;
    private long matrikelnummer;
    private int raumId;
    private String fachname;
    private String token;

}

