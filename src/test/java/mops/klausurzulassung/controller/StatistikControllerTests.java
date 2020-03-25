package mops.klausurzulassung.controller;

import com.c4_soft.springaddons.test.security.context.support.WithMockKeycloackAuth;
import mops.klausurzulassung.database_entity.Modul;
import mops.klausurzulassung.database_entity.ModulStatistiken;
import mops.klausurzulassung.services.ModulService;
import mops.klausurzulassung.services.StatistikService;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatistikControllerTests {

  private StatistikController statistikController;
  //private ModulService modulService;
  @MockBean
  private StatistikService statistikService;
  private Principal principal;

  @MockBean
  private ModulService modulService;

  @Autowired
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    //this.modulService = mock(ModulService.class);
    //this.statistikService = mock(StatistikService.class);
    this.statistikController = new StatistikController(modulService, statistikService);
    this.principal = mock(Principal.class);
  }

  @Test
  void iteratorToListForModulStatistiks() {
    List<ModulStatistiken> list = new ArrayList<>();
    ModulStatistiken modul1 = new ModulStatistiken(1L, 9L, "02/12/2020", 180L, 30L);
    ModulStatistiken modul2 = new ModulStatistiken(2L, 9L, "05/12/2021", 100L, 100L);

    list.add(modul1);
    list.add(modul2);

    Iterable<ModulStatistiken> modulStatistikens = list;

    List<ModulStatistiken> result = statistikController.iteratorToListForModulStatistiks(modulStatistikens);

    assertEquals(modul1, result.get(0));
    assertEquals(modul2, result.get(1));
  }

  @WithMockKeycloackAuth(name = "orga", roles = "orga")
  @Test
  void selectStatistik() throws Exception {

    Modul modul = new Modul(3L, "ProPra", "orga", "06/17/2022", true, 160L);

    List<ModulStatistiken> list = new ArrayList<>();
    ModulStatistiken modul1 = new ModulStatistiken(1L, 3L, "02/12/2020", 180L, 30L);
    ModulStatistiken modul2 = new ModulStatistiken(2L, 3L, "05/12/2021", 100L, 100L);

    list.add(modul1);
    list.add(modul2);

    Iterable<ModulStatistiken> modulStatistikens = list;

    when(modulService.findById(3L)).thenReturn(Optional.of(modul));
    when(statistikService.findModulStatistikensByModulId(3L)).thenReturn(modulStatistikens);

    mockMvc.perform(get("/zulassung1/modul/3/statistik"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("modulStatistiken", list));
  }
}
