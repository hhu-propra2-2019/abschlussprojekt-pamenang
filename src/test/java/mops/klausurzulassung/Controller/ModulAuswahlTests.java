package mops.klausurzulassung.Controller;

import com.c4_soft.springaddons.test.security.context.support.WithMockKeycloackAuth;
import mops.klausurzulassung.Domain.AltzulassungStudentDto;
import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Domain.StudentDto;
import mops.klausurzulassung.Services.ModulService;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.http.HttpRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

  @Test
  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  void test_ModulBearbeiten() throws Exception {

    Modul modul = new Modul(1L,"Propra","Max mustermann","2020-12-15 15:00",true);
    when(modulservice.findById(1L)).thenReturn(java.util.Optional.of(modul));

    mockMvc
            .perform(get("/zulassung1/modulBearbeiten/1"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("id",1L))
            .andExpect(model().attribute("modul",modul))
            .andDo(print());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  public void test_ModeulBearbeiten_Postmodul() throws Exception {
    String modulName = "testen";
    String modulFrist = "2020-12-15 15:00";

    Modul modul = new Modul(1L,"testen",null,"2020-12-15 15:00",true);
    when(modulservice.findById(1L)).thenReturn(java.util.Optional.of(modul));

    mockMvc.perform(post("/zulassung1/modulBearbeiten/1")
            .param("name", modulName)
            .param("frist", modulFrist))
            .andExpect(status().is3xxRedirection());
    verify(modulservice, times(1)).save(modul);
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  public void test_deleteModulWirdAufgerufen() throws Exception {
    String [] temp = {"message1","message2"};
    when(modulservice.deleteStudentsFromModul(1L)).thenReturn(temp);
    mockMvc.perform(post("/zulassung1/modul/1/delete"))
            .andExpect(status().is3xxRedirection());
    verify(modulservice, times(1)).deleteStudentsFromModul(1L);
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  public void test_Modul_Select_Get() throws Exception {
    Modul modul = new Modul(1L,"testen",null,"2020-12-15 15:00",true);
    when(modulservice.findById(1L)).thenReturn(java.util.Optional.of(modul));

    mockMvc.perform(get("/zulassung1/modul/1"))
            .andExpect(model().attribute("modul", modul.getName()))
            .andExpect(model().attribute("id", modul.getId()))
            .andExpect(model().attribute("frist", modul.getFrist()))
            .andExpect(status().isOk());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  public void test_Modul_Select_Post()throws Exception {

    MockMultipartFile multipartFile = new MockMultipartFile("datei","test.csv","text/csv","test;test;test".getBytes());
    String [] temp = {"message1","message2"};
    when(modulservice.verarbeiteUploadliste(1L,multipartFile)).thenReturn(temp);
    mockMvc.perform(MockMvcRequestBuilders.multipart("/zulassung1/modul/1").file(multipartFile))
            .andExpect(status().is3xxRedirection());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  public void test_downloadListeWirdAufgerufen() throws Exception {
    mockMvc
            .perform(get("/zulassung1/modul/1/klausurliste"))
            .andExpect(status().isOk());
    verify(modulservice, times(1)).download(eq(1L), Matchers.any(HttpServletResponse.class));
  }


  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  public void test_altKlausurZulassungHinzufuegenLeer() throws Exception {
    mockMvc
            .perform(post("/zulassung1/1/altzulassungHinzufuegen"))
            .andExpect(status().is3xxRedirection());
    verify(modulservice, times(0)).altzulassungVerarbeiten(any(AltzulassungStudentDto.class),anyBoolean(),anyLong());
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  public void test_altKlausurZulassungHinzufuegenMitVollemObject() throws Exception {

    AltzulassungStudentDto studentDto = new AltzulassungStudentDto("vorname","nachname","test@test.de",1234567L,1L);
    String [] temp = {"message1","message2"};
    when(modulservice.altzulassungVerarbeiten(any(AltzulassungStudentDto.class),anyBoolean(),anyLong())).thenReturn(temp);
    mockMvc
            .perform(post("/zulassung1/1/altzulassungHinzufuegen")
            .param("vorname",studentDto.getVorname())
            .param("nachname", studentDto.getNachname())
            .param("email", studentDto.getEmail())
            .param("matrikelnummer", studentDto.getMatrikelnummer().toString())
            .param("modulId", "1")
            .param("papierzulassung",String.valueOf(true))).andExpect(status().is3xxRedirection());
    verify(modulservice, times(1)).altzulassungVerarbeiten(any(AltzulassungStudentDto.class),anyBoolean(),anyLong());
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
