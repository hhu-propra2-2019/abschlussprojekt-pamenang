package mops.klausurzulassung.Controller;

import com.c4_soft.springaddons.test.security.context.support.WithMockKeycloackAuth;
import com.c4_soft.springaddons.test.security.web.servlet.request.ServletUnitTestingSupport;
import mops.Application;
import mops.klausurzulassung.Config.KeycloakConfig;
import mops.klausurzulassung.Config.SecurityConfig;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Domain.StudentDto;
import mops.klausurzulassung.Services.StudentService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class Studententest{

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  StudentService studentService;

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
    String fachLink = "12";
    String matrikelnummerLink = "1234455";
    String vornameLink = "testVorname";
    String nachnameLink = "testNachname";
    when(studentService.findByToken(tokenLink)).thenReturn(java.util.Optional.of(new Student(vornameLink, nachnameLink,"testEmail", Long.parseLong(matrikelnummerLink), Long.parseLong(fachLink), "Propra2", tokenLink)));


    mockMvc.perform(get("/zulassung1/student/" + tokenLink + "/"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("token", not(nullValue())));

  }

  @Test
  @WithMockKeycloackAuth(name = "test", roles = "studentin")
  public void test_postMapping_empfangeDaten_checkForRedirect() throws Exception {
    mockMvc.perform(post("/zulassung1/student")
            .param("token", "testToken")
            .contentType("application/x-www-form-urlencoded"))
            .andExpect(status().is3xxRedirection());
  }
}
