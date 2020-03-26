package mops.klausurzulassung.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import mops.klausurzulassung.database_entity.Student;

@Data
@AllArgsConstructor
public class EmailError {

  private Student student;
}
