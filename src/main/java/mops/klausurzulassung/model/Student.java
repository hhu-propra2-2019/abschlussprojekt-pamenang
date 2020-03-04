package mops.klausurzulassung.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

@Entity
@Data
public class Student {

    @Id
    private String matno;
    private String name;
    private String surname;

    @ManyToMany(fetch=EAGER, cascade = ALL)
    private List<Modul> anmeldungen;

    @ManyToMany(fetch=EAGER, cascade = ALL)
    private List<Modul> zulassungen;
}
