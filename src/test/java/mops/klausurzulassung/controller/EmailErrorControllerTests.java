package mops.klausurzulassung.controller;

import com.c4_soft.springaddons.test.security.context.support.WithMockKeycloackAuth;
import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.domain.EmailError;
import mops.klausurzulassung.services.EmailErrorService;
import mops.klausurzulassung.services.EmailService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EmailErrorControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private EmailErrorService emailErrorService;
  @MockBean
  private EmailService emailService;

  @Test
  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  void test_emailErrorAnzeige() throws Exception {
    mockMvc
            .perform(get("/zulassung1/emailError/2"))
            .andExpect(status().isOk())
            .andDo(print());
  }

  @Test
  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  void test_emailNeuVersendenAnStudentenSuccessful() throws Exception {
    Student student = new Student("vorname","nachname","email",1234567L,2L,"fachname","token");

    when(emailErrorService.findStudentInListOfErrorEmails(2L,1234567L)).thenReturn(student);
    when(emailService.resendEmail(student)).thenReturn(true);
    mockMvc
            .perform(get("/zulassung1/emailError/2/1234567"))
            .andExpect(status().is3xxRedirection())
            .andDo(print());

    verify(emailErrorService,times(1)).deleteEmailErrorFromListWithStudent(student);
  }

  @Test
  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  void test_emailNeuVersendenAnStudentenNotSuccessful() throws Exception {
    Student student = new Student("vorname","nachname","email",1234567L,2L,"fachname","token");

    when(emailErrorService.findStudentInListOfErrorEmails(2L,1234567L)).thenReturn(student);
    when(emailService.resendEmail(student)).thenReturn(false);
    mockMvc
            .perform(get("/zulassung1/emailError/2/1234567"))
            .andExpect(status().is3xxRedirection())
            .andDo(print());

    verify(emailErrorService,times(0)).deleteEmailErrorFromListWithStudent(student);
  }

  @Test
  void test_filterAllEmailErrors(){
    ArrayList<EmailError> filterList = new ArrayList<>();
    ArrayList<EmailError> emailErrorList = new ArrayList<>();

    Student student = new Student("vorname","nachname","email",1234567L,2L,"fachname","token");
    Student student1 = new Student("vorname1","nachname1","email1",2234567L,2L,"fachname","token1");
    Student student2 = new Student("vorname2","nachname2","email2",3234567L,2L,"fachname","token2");

    emailErrorList.add(new EmailError(student));
    emailErrorList.add(new EmailError(student1));
    emailErrorList.add(new EmailError(student2));

    EmailErrorController emailErrorController = new EmailErrorController(emailErrorService,emailService);
    filterList = emailErrorController.filterAllEmailErrors(emailErrorList,2L);
    assertThat(filterList.size(),is(3));

  }

  @Test
  void test_filterAllEmailErrorsMitAndererModulId(){
    ArrayList<EmailError> filterList = new ArrayList<>();
    ArrayList<EmailError> emailErrorList = new ArrayList<>();

    Student student = new Student("vorname","nachname","email",1234567L,2L,"fachname","token");
    Student student1 = new Student("vorname1","nachname1","email1",2234567L,2L,"fachname","token1");
    Student student2 = new Student("vorname2","nachname2","email2",3234567L,1L,"fachname","token2");

    emailErrorList.add(new EmailError(student));
    emailErrorList.add(new EmailError(student1));
    emailErrorList.add(new EmailError(student2));

    EmailErrorController emailErrorController = new EmailErrorController(emailErrorService,emailService);
    filterList = emailErrorController.filterAllEmailErrors(emailErrorList,2L);
    assertThat(filterList.size(),is(2));

  }

}
