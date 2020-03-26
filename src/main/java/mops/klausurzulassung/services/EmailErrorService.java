package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.domain.EmailError;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class EmailErrorService {

    ArrayList<EmailError> emailErrors = new ArrayList<>();

    public void addEmailErrorToList(EmailError emailError){
      this.emailErrors.add(emailError);
    }

    public ArrayList<EmailError> getEmailErrors(){
      return this.emailErrors;
    }

    public void deleteEmailErrorFromListWithStudent(Student student){
      EmailError emailError = findEmailErrorWithStudent(student);
      emailErrors.remove(emailError);
    }

   EmailError findEmailErrorWithStudent(Student student) {
    for (EmailError emailError : emailErrors) {
        Student studentFromList = emailError.getStudent();
        if (studentFromList.equals(student)){
          return emailError;
        }
    }
    return new EmailError(new Student());
  }

  public Student findStudentInListOfErrorEmails(Long modulId, Long matrikelNummer) {
    for (EmailError emailError : this.emailErrors) {
      Student student = emailError.getStudent();
      if (student.getModulId().equals(modulId) && student.getMatrikelnummer().equals(matrikelNummer)){
        return student;
      }
    }
    return new Student();
  }
}
