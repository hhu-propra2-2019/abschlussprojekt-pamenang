package mops.klausurzulassung.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;
import java.util.UUID;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

@Entity
@Data
public class Modul {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String semester;

    @ManyToMany(fetch=EAGER, cascade = ALL)
    private List<Student> anmeldungen;

    @ManyToMany(fetch=EAGER, cascade = ALL)
    private List<Student> zulassungen;
}
