package mops.klausurzulassung.Controller;

import com.c4_soft.springaddons.test.security.context.support.WithMockKeycloackAuth;
import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Services.ModulService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ModulAuswahlTests {

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private ModulService modulservice;

  @Test
  void fuerModulAuswahlAnmelden() throws Exception {
    mockMvc
        .perform(get("/zulassung1/modulHinzufuegen"))
        .andExpect(status().is3xxRedirection())
        .andDo(print());
  }

  //Falscher Benuztzer Tests

  @Test
  @WithMockKeycloackAuth(name = "studentin", roles = "studentin")
  void test_fuerModulFalscherBenutzer_modulHinzufuegen() throws Exception {
    mockMvc
        .perform(get("/zulassung1/modulHinzufuegen"))
        .andExpect(status().isForbidden())
        .andDo(print());
  }

  @Test
  @WithMockKeycloackAuth(name = "studentin", roles = "studentin")
  void test_fuerModulFalscherBenutzer_modulAuswahl() throws Exception {
    mockMvc
            .perform(get("/zulassung1/modulAuswahl"))
            .andExpect(status().isForbidden())
            .andDo(print());
  }

  @Test
  @WithMockKeycloackAuth(name = "studentin", roles = "studentin")
  void test_fuerModulFalscherBenutzer_modulBearbeiten() throws Exception {
    mockMvc
            .perform(get("/zulassung1/modulBearbeiten/" + 5 ))
            .andExpect(status().isForbidden())
            .andDo(print());
  }

  @Test
  @WithMockKeycloackAuth(name = "studentin", roles = "studentin")
  void test_fuerModulFalscherBenutzer_modulUndId() throws Exception {
    mockMvc
            .perform(get("/zulassung1/modul/" + 5))
            .andExpect(status().isForbidden())
            .andDo(print());
  }

  @Test
  @WithMockKeycloackAuth(name = "studentin", roles = "studentin")
  void test_fuerModulFalscherBenutzer_KlausurListeDownload() throws Exception {
    mockMvc
            .perform(get("/zulassung1/modul/" + 5 + "/klausurliste"))
            .andExpect(status().isForbidden())
            .andDo(print());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void fuerModulAuswahlIstAnmelden() throws Exception {

    Principal principal = mock(Principal.class);

    List<Modul> module = new ArrayList<>();
    when(principal.getName()).thenReturn("orga");

    when(modulservice.findByOwnerAndActive(principal.getName(), true)).thenReturn(module);
    mockMvc
        .perform(get("/zulassung1/modulHinzufuegen"))
        .andExpect(status().isOk());
  }

/*
  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  public void newModule() throws Exception {

    Principal principal = mock(Principal.class);

    List<Modul> module = new ArrayList<>();
    when(principal.getName()).thenReturn("orga");

    when(modulservice.findByOwner(any())).thenReturn(module);
    mockMvc
        .perform(post("/zulassung1/modulHinzufuegen"))
        .andExpect(status().is3xxRedirection());
  }*/
}
