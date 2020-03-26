package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.domain.EmailError;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class EmailErrorService {

    ArrayList<EmailError> emailErrors = new ArrayList<>();

  /**
   * This method adds an EmailErrorObject to the emailErrors-List
   * @param emailError contains a StudentObject, when the Email was not send
   */
    public void addEmailErrorToList(EmailError emailError){
      this.emailErrors.add(emailError);
    }


  /**
   * This method just returns the EmailErrorList
   * @return emailError-List
   */
    public ArrayList<EmailError> getEmailErrors(){
      return this.emailErrors;
    }

  /**
   * This method deletes EmailError form List based on a specific Student
   * @param student contains the Student-Information
   */
    public void deleteEmailErrorFromListWithStudent(Student student){
      EmailError emailError = findEmailErrorWithStudent(student);
      emailErrors.remove(emailError);
    }

  /**
   * This method finds an EmailError by Student and returns this EmailError.
   * If there is no Student in Email Error it returns a new StudentObject
   * @param student contains the Student-Information
   * @return EmailError-Object
   */
    EmailError findEmailErrorWithStudent(Student student) {
    for (EmailError emailError : emailErrors) {
        Student studentFromList = emailError.getStudent();
        if (studentFromList.equals(student)){
          return emailError;
        }
    }
    return new EmailError(new Student());
  }

  /**
   * This method finds a Student in EmailErrorList by Modul-ID and ID-Number.
   * If there is no Student in Email Error it returns a new StudentObject
   * @param modulId contains the Modul-ID
   * @param matrikelNummer contains the ID-Number
   * @return new Student-Object if there is no Student in EmailError
   */
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
