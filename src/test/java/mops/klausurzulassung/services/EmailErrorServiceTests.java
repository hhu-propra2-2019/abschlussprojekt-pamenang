package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.domain.EmailError;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EmailErrorServiceTests {

  @Autowired
  private MockMvc mockMvc;



  @Test
  void test_findEmailErrorWithStudent(){
    EmailErrorService emailErrorService= new EmailErrorService();
    Student student = new Student("vorname","nachname","email",1234567L,2L,"fachname","token");

    emailErrorService.addEmailErrorToList(new EmailError(student));
    EmailError emailError =emailErrorService.findEmailErrorWithStudent(student);
    assertThat(emailError.getStudent().getMatrikelnummer(),is(1234567L));

  }

  @Test
  void test_findEmailErrorWithStudentNotSuccessful(){
    EmailErrorService emailErrorService= new EmailErrorService();
    Student student = new Student("vorname","nachname","email",1234567L,2L,"fachname","token");
    Student student1 = new Student("vorname1","nachname1","email1",2234567L,2L,"fachname","token1");


    emailErrorService.addEmailErrorToList(new EmailError(student));
    EmailError emailError =emailErrorService.findEmailErrorWithStudent(student1);
    assertThat(emailError.getStudent().getMatrikelnummer(),is(nullValue()));

  }

  @Test
  void test_findStudentInListOfErrorEmailsSuccessful(){
    EmailErrorService emailErrorService= new EmailErrorService();
    Student student = new Student("vorname","nachname","email",1234567L,2L,"fachname","token");


    emailErrorService.addEmailErrorToList(new EmailError(student));
    Student student1 =emailErrorService.findStudentInListOfErrorEmails(2L,1234567L);
    assertThat(student1.getNachname(),is(student.getNachname()));

  }

  @Test
  void test_findStudentInListOfErrorEmailsNotSuccessful(){
    EmailErrorService emailErrorService= new EmailErrorService();
    Student student = new Student("vorname","nachname","email",1234567L,2L,"fachname","token");


    emailErrorService.addEmailErrorToList(new EmailError(student));
    Student student1 =emailErrorService.findStudentInListOfErrorEmails(2L,2234567L);
    assertThat(student1.getNachname(),is(nullValue()));

  }

}
