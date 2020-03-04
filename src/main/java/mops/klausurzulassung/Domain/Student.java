package mops.klausurzulassung.Domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {

    private String vorname;
    private String nachname;
    private long matrikelnummer;
    private boolean zulassung;
}
