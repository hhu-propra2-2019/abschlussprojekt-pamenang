package mops.klausurzulassung.Controller;

import mops.klausurzulassung.Controller.StudentenController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class Studententest {

  @MockBean
  StudentenController student;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private WebApplicationContext context;

  @Test
  public void fuerAltzulassungAnmelden() throws Exception {
    mockMvc.perform(get("/student")).andExpect(status().is3xxRedirection()).andDo(print());
  }

  @WithMockUser(username = "niemand", password = "nichts")
  @Test
  public void anmeldung() throws Exception {
    mockMvc.perform(get("/student")).andExpect(status().isForbidden());
  }
}