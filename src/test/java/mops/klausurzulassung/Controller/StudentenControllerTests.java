package mops.klausurzulassung.Controller;

import com.c4_soft.springaddons.test.security.context.support.WithMockKeycloackAuth;
import com.c4_soft.springaddons.test.security.web.servlet.request.ServletUnitTestingSupport;
import mops.Application;
import mops.klausurzulassung.Config.KeycloakConfig;
import mops.klausurzulassung.Config.SecurityConfig;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class Studententest{

  @Autowired
  private MockMvc mockMvc;

  @Test
  void fuerAltzulassungAnmelden() throws Exception {
    mockMvc.perform(get("/student")).andExpect(status().is3xxRedirection()).andDo(print());
  }

  
  @Test
  @WithMockKeycloackAuth(name = "test_dödel", roles = "studentin")
  void anmeldung_als_Student() throws Exception {
    mockMvc.perform(get("/zulassung1/student")).andExpect(status().isOk());
  }
}
