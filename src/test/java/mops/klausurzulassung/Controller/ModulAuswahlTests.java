package mops.klausurzulassung.Controller;

import com.c4_soft.springaddons.test.security.context.support.WithMockKeycloackAuth;
import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Services.ModulService;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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

  // Get Mapping - Falscher Benuztzer Tests

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

  // Post Mapping - Falscher Benutzer Tests

  @Test
  @WithMockKeycloackAuth(name = "studentin", roles = "studentin")
  void test_fuerModulFalscherBenutzer_neuesModulHinzufuegen() throws Exception {
    mockMvc
            .perform(post("/zulassung1/neuesModulHinzufuegen"))
            .andExpect(status().isForbidden())
            .andDo(print());
  }

  @Test
  @WithMockKeycloackAuth(name = "studentin", roles = "studentin")
  void test_fuerModulFalscherBenutzer_post_modulBearbeiten() throws Exception {
    mockMvc
            .perform(post("/zulassung1/modulBearbeiten/" + 5))
            .andExpect(status().isForbidden())
            .andDo(print());
  }

  @Test
  @WithMockKeycloackAuth(name = "studentin", roles = "studentin")
  void test_fuerModulFalscherBenutzer_post_deleteModul() throws Exception {
    mockMvc
            .perform(post("/zulassung1/modul/" + 5 + "/delete"))
            .andExpect(status().isForbidden())
            .andDo(print());
  }

  @Test
  @WithMockKeycloackAuth(name = "studentin", roles = "studentin")
  void test_fuerModulFalscherBenutzer_post_modulListeUpload() throws Exception {
    mockMvc
            .perform(post("/zulassung1/modul" + 5)
                    .requestAttr("file", mock(MultipartFile.class)))
            .andExpect(status().isNotFound())
            .andDo(print());
  }

  @Test
  @WithMockKeycloackAuth(name = "studentin", roles = "studentin")
  void test_fuerModulFalscherBenutzer_post_altZulassungHinzufuegen() throws Exception {
    mockMvc
            .perform(post("/zulassung1/5/altzulassungHinzufuegen"))
            .andExpect(status().isForbidden())
            .andDo(print());
  }

  // Model Tests

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void test_modulHinzufuegen_modelHatRichtigeModule() throws Exception {

    Principal principal = mock(Principal.class);

    List<Modul> module = new ArrayList<>();
    module.add(mock(Modul.class));
    when(principal.getName()).thenReturn("orga");

    when(modulservice.findByActive(false)).thenReturn(module);
    mockMvc.perform(get("/zulassung1/modulHinzufuegen"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("moduls", module));
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  public void test_modulAuswahl_modelHatRichtigeModule() throws Exception {

    Principal principal = mock(Principal.class);

    List<Modul> module = new ArrayList<>();
    module.add(mock(Modul.class));
    when(principal.getName()).thenReturn("orga");

    when(modulservice.findByOwnerAndActive(principal.getName(), true)).thenReturn(module);
    mockMvc.perform(get("/zulassung1/modulAuswahl"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("moduls", module));
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  public void test_neuesModulHinzufuegen_modulWirdGespeichert() throws Exception {
    String modulName = "testen";
    String modulFrist = "2020-12-15 15:00";

    mockMvc.perform(post("/zulassung1/neuesModulHinzufuegen")
            .param("name", modulName)
            .param("frist", modulFrist))
            .andExpect(status().isOk());
    verify(modulservice, times(1)).save(new Modul(any(), modulName, "orga", modulFrist, true));
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
