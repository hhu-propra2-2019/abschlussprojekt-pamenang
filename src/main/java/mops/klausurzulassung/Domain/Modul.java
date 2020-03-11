package mops.klausurzulassung.Domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "moduls")
@AllArgsConstructor
@NoArgsConstructor
public class Modul {

  @Id
  @Column(name = "id")
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "owner")
  private String owner;

  @Column(name="frist")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date frist;
}
