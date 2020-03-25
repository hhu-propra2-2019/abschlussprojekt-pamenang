package mops.klausurzulassung.controller;

import com.c4_soft.springaddons.test.security.context.support.WithMockKeycloackAuth;
import mops.klausurzulassung.exceptions.InvalidToken;
import mops.klausurzulassung.services.StudentService;
import mops.klausurzulassung.services.TokenverifikationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class StudentenControllerTests {

  @MockBean
  StudentService studentService;
  @MockBean
  TokenverifikationService tokenverifikation;
  @Autowired
  private MockMvc mockMvc;

  @Test
  @WithMockKeycloackAuth(name = "test", roles = "studentin")
  void anmeldung_als_Student() throws Exception {
    mockMvc.perform(get("/zulassung1/student")).andExpect(status().isOk());
  }

  @Test
  @WithMockKeycloackAuth(name = "test", roles = "orga")
  void anmeldung_als_Organisator() throws Exception {
    mockMvc.perform(get("/zulassung1/student")).andExpect(status().isOk());
  }

  @Test
  @WithMockKeycloackAuth(name = "test", roles = "studentin")
  void test_getMappingLink() throws Exception {
    String tokenLink = "testToken";

    mockMvc.perform(get("/zulassung1/student/" + tokenLink + "/"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("token", not(nullValue())));

  }

  @Test
  @WithMockKeycloackAuth(name = "test", roles = "studentin")
  void test_postMapping_empfangeDaten_checkForRedirect() throws Exception {

    doThrow(new InvalidToken("Invalid Token")).when(tokenverifikation).verifikationToken(any());

    mockMvc.perform(post("/zulassung1/student")
            .param("token", "testToken")
            .contentType("application/x-www-form-urlencoded"))
            .andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockKeycloackAuth(name = "test", roles = "studentin")
  void postMappingValidToken() throws Exception {

    doNothing().when(tokenverifikation).verifikationToken(any());

    mockMvc.perform(post("/zulassung1/student/")
        .param("token", "testtoken"))
        .andExpect(MockMvcResultMatchers.redirectedUrl("/zulassung1/student"))
        .andExpect(status().is3xxRedirection())
        .andExpect(model().attribute("token", nullValue()));


  }
}
