package mops.klausurzulassung.controller;

import com.c4_soft.springaddons.test.security.context.support.WithMockKeycloackAuth;
import mops.klausurzulassung.database_entity.Modul;
import mops.klausurzulassung.domain.AltzulassungStudentDto;
import mops.klausurzulassung.domain.FrontendMessage;
import mops.klausurzulassung.services.ModulService;
import mops.klausurzulassung.services.StatistikService;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

  @MockBean
  private StatistikService statistikService;

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
        .perform(get("/zulassung1/modulBearbeiten/" + 5))
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
  void test_modulAuswahl_modelHatRichtigeModule() throws Exception {

    Principal principal = mock(Principal.class);

    List<Modul> module = new ArrayList<>();
    module.add(mock(Modul.class));
    when(principal.getName()).thenReturn("orga");

    when(modulservice.findByOwnerAndActive(principal.getName(), true)).thenReturn(module);
    mockMvc.perform(get("/zulassung1/modulAuswahl"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("moduls", module));
  }



  @Test
  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  void test_ModulBearbeiten() throws Exception {

    Modul modul = new Modul(1L, "Propra", "Max mustermann", "2020-12-15 15:00", true, 0L);
    when(modulservice.findById(1L)).thenReturn(java.util.Optional.of(modul));

    mockMvc
        .perform(get("/zulassung1/modulBearbeiten/1"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("id", 1L))
        .andExpect(model().attribute("modul", modul))
        .andDo(print());
  }



  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void test_deleteModulWirdAufgerufen() throws Exception {
    FrontendMessage message = new FrontendMessage("error", "success");
    when(modulservice.deleteStudentsFromModul(1L)).thenReturn(message);
    mockMvc.perform(post("/zulassung1/modul/1/delete"))
        .andExpect(status().is3xxRedirection());
    verify(modulservice, times(1)).deleteStudentsFromModul(1L);
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void test_Modul_Select_Get() throws Exception {
    Modul modul = new Modul(1L, "testen", null, "2020-12-15 15:00", true, 0L);
    when(modulservice.findById(1L)).thenReturn(java.util.Optional.of(modul));

    mockMvc.perform(get("/zulassung1/modul/1"))
        .andExpect(model().attribute("modul", modul.getName()))
        .andExpect(model().attribute("id", modul.getId()))
        .andExpect(model().attribute("frist", modul.getFrist()))
        .andExpect(status().isOk());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void test_Modul_Select_Post() throws Exception {

    MockMultipartFile multipartFile = new MockMultipartFile("datei", "test.csv", "text/csv", "test;test;test".getBytes());
    FrontendMessage message = new FrontendMessage("error", "success");

    when(modulservice.verarbeiteUploadliste(1L, multipartFile)).thenReturn(message);
    mockMvc.perform(MockMvcRequestBuilders.multipart("/zulassung1/modul/1").file(multipartFile))
        .andExpect(status().is3xxRedirection());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void test_downloadListeWirdAufgerufen() throws Exception {
    mockMvc
        .perform(get("/zulassung1/modul/1/klausurliste"))
        .andExpect(status().isOk());
    verify(modulservice, times(1)).download(eq(1L), Matchers.any(HttpServletResponse.class));
  }


  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void test_altKlausurZulassungHinzufuegenLeer() throws Exception {
    mockMvc
        .perform(post("/zulassung1/1/altzulassungHinzufuegen"))
        .andExpect(status().is3xxRedirection());
    verify(modulservice, times(0)).altzulassungVerarbeiten(any(AltzulassungStudentDto.class), anyBoolean(), anyLong());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void test_altKlausurZulassungHinzufuegenMitVollemObject() throws Exception {

    AltzulassungStudentDto studentDto = new AltzulassungStudentDto("vorname", "nachname", "test@test.de", 1234567L, 1L);
    FrontendMessage message = new FrontendMessage("error", "success");

    when(modulservice.altzulassungVerarbeiten(any(AltzulassungStudentDto.class), anyBoolean(), anyLong())).thenReturn(message);
    mockMvc
        .perform(post("/zulassung1/1/altzulassungHinzufuegen")
            .param("vorname", studentDto.getVorname())
            .param("nachname", studentDto.getNachname())
            .param("email", studentDto.getEmail())
            .param("matrikelnummer", studentDto.getMatrikelnummer().toString())
            .param("modulId", "1")
            .param("papierzulassung", String.valueOf(true))).andExpect(status().is3xxRedirection());
    verify(modulservice, times(1)).altzulassungVerarbeiten(any(AltzulassungStudentDto.class), anyBoolean(), anyLong());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void modulAbschickenWithMissingAttribute() throws Exception {
    Modul modul = new Modul(1L, "", "testorga", "01/02/2021", true, 0L);

    when(modulservice.missingAttributeInModul(modul)).thenReturn(true);

    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.post("/zulassung1/modulBearbeiten/1")
            .param("id", modul.getId().toString())
            .param("name", modul.getName())
            .param("owner", modul.getOwner())
            .param("frist", modul.getFrist())
            .param("active", "true");

    mockMvc.perform(builder)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.redirectedUrl
            ("/zulassung1/modulBearbeiten/1"))
        .andExpect(status().is3xxRedirection());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void modulAbschickenMitfalscherFrist() throws Exception {
    Modul modul = new Modul(2L, "name", "testorga", "50/02/2000", true, 0L);

    when(modulservice.missingAttributeInModul(modul)).thenReturn(false);
    when(modulservice.fristIsDate(modul.getFrist())).thenReturn(false);

    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.post("/zulassung1/modulBearbeiten/2")
            .param("id", modul.getId().toString())
            .param("name", modul.getName())
            .param("owner", modul.getOwner())
            .param("frist", modul.getFrist())
            .param("active", "true");

    mockMvc.perform(builder)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.redirectedUrl
            ("/zulassung1/modulBearbeiten/2"))
        .andExpect(status().is3xxRedirection());

  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void modulAbschickenMitAbgelaufenerFrist() throws Exception {
    Modul modul = new Modul(3L, "name", "testorga", "01/02/2000", true, 0L);

    when(modulservice.missingAttributeInModul(modul)).thenReturn(false);
    when(modulservice.fristIsDate(modul.getFrist())).thenReturn(true);
    when(modulservice.isFristAbgelaufen(modul)).thenReturn(true);

    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.post("/zulassung1/modulBearbeiten/3")
            .param("id", modul.getId().toString())
            .param("name", modul.getName())
            .param("owner", modul.getOwner())
            .param("frist", modul.getFrist())
            .param("active", "true")
            .param("teilnehmer", "0");

    mockMvc.perform(builder)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.redirectedUrl
            ("/zulassung1/modulBearbeiten/3"))
        .andExpect(status().is3xxRedirection());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void modulAbschicken() throws Exception {

    String frist = fristInZukunft();

    Modul modul = new Modul(null, "name", null, frist, true, 0L);
    Modul vorhandenesModul = new Modul(4L, "ProPra", null, null, false, 0L);

    when(modulservice.findById(4L)).thenReturn(Optional.of(vorhandenesModul));
    when(modulservice.missingAttributeInModul(modul)).thenReturn(false);
    when(modulservice.fristIsDate(modul.getFrist())).thenReturn(true);
    when(modulservice.isFristAbgelaufen(modul)).thenReturn(false);

    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.post("/zulassung1/modulBearbeiten/4")
            .param("id", "4")
            .param("name", modul.getName())
            .param("frist", modul.getFrist());

    mockMvc.perform(builder)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.redirectedUrl
            ("/zulassung1/modulAuswahl"))
        .andExpect(status().is3xxRedirection());

    verify(modulservice, times(1)).save(vorhandenesModul);

    assertEquals("name", vorhandenesModul.getName());
    assertEquals("orga", vorhandenesModul.getOwner());
    assertEquals(true, vorhandenesModul.getActive());
    assertEquals(frist + " 12:00", vorhandenesModul.getFrist());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void backToModulAuswahlWithMissingAttribute() throws Exception {
    Modul modul = new Modul(null, "", null, "01/02/2021", null, 0L);

    when(modulservice.missingAttributeInModul(modul)).thenReturn(true);

    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.post("/zulassung1/neuesModulHinzufuegen")
            .param("name", modul.getName())
            .param("frist", modul.getFrist());

    mockMvc.perform(builder)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.redirectedUrl
            ("/zulassung1/modulHinzufuegen"))
        .andExpect(status().is3xxRedirection());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void backToModulAuswahlFristIstUngueltig() throws Exception {
    Modul modul = new Modul(null, "ProPra", null, "50/02/2021", null, 0L);

    when(modulservice.missingAttributeInModul(modul)).thenReturn(false);
    when(modulservice.fristIsDate(modul.getFrist())).thenReturn(false);

    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.post("/zulassung1/neuesModulHinzufuegen")
            .param("name", modul.getName())
            .param("frist", modul.getFrist());

    mockMvc.perform(builder)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.redirectedUrl
            ("/zulassung1/modulHinzufuegen"))
        .andExpect(status().is3xxRedirection());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void backToModulAuswahlFristIstAbgelaufen() throws Exception {
    Modul modul = new Modul(null, "ProPra", null, "01/02/2000", null, 0L);

    when(modulservice.missingAttributeInModul(modul)).thenReturn(false);
    when(modulservice.fristIsDate(modul.getFrist())).thenReturn(true);
    when(modulservice.isFristAbgelaufen(modul)).thenReturn(true);

    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.post("/zulassung1/neuesModulHinzufuegen")
            .param("name", modul.getName())
            .param("frist", modul.getFrist());

    mockMvc.perform(builder)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.redirectedUrl
            ("/zulassung1/modulAuswahl"))
        .andExpect(status().is3xxRedirection());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void backToModulAuswahl() throws Exception {
    String frist = fristInZukunft();

    Modul modul = new Modul(null, "ProPra", null, frist, null, 0L);

    when(modulservice.missingAttributeInModul(modul)).thenReturn(false);
    when(modulservice.fristIsDate(modul.getFrist())).thenReturn(true);
    when(modulservice.isFristAbgelaufen(modul)).thenReturn(false);

    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.post("/zulassung1/neuesModulHinzufuegen")
            .param("name", modul.getName())
            .param("frist", modul.getFrist());

    mockMvc.perform(builder)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.redirectedUrl("/zulassung1/modulAuswahl"))
        .andExpect(status().is3xxRedirection());

    verify(modulservice, times(1)).save(any());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void modulTeilnehmerHinzufuegen() throws Exception {
    String frist = "01/01/2100";
    Modul modul = new Modul(2L, "ProPra", null, frist, null, 100L);

    when(modulservice.findById(2L)).thenReturn(Optional.of(modul));
    when(statistikService.modulInDatabase(frist, 2L)).thenReturn(15L);

    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.post("/zulassung1/modul/teilnehmerHinzufuegen/2")
            .param("teilnehmerAnzahl", "100");

    mockMvc.perform(builder)
        .andExpect(MockMvcResultMatchers.redirectedUrl("/zulassung1/modul/2"))
        .andExpect(status().is3xxRedirection());

    verify(modulservice, times(1)).saveGesamtTeilnehmerzahlForModul(any(), any());
    verify(statistikService, times(1)).save(any());

  }


  private String fristInZukunft() {
    LocalDateTime now = LocalDateTime.now().withNano(0).withSecond(0);
    LocalDateTime future = now.plusYears(1);
    int year = future.getYear();
    int month = future.getMonthValue();
    int day = future.getDayOfMonth();
    return month + "/" + day + "/" + year;
  }
}
