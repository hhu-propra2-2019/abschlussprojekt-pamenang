package mops.klausurzulassung.Entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name="moduls")
public class Modul {

    @Id
    @Column(name="id")
    private Long id;

    @Column(name="name")
    private String name;

}
