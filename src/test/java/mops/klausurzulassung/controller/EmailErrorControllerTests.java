package mops.klausurzulassung.controller;

import com.c4_soft.springaddons.test.security.context.support.WithMockKeycloackAuth;
import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.services.EmailErrorService;
import mops.klausurzulassung.services.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
  
}
