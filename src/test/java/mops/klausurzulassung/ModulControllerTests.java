package mops.klausurzulassung;

import mops.klausurzulassung.organisatoren.Entities.Modul;
import mops.klausurzulassung.organisatoren.Services.ModulService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class ModulControllerTests {

  @Autowired
  private MockMvc mockMvc;

  private ModulService modulService;

  @BeforeEach
  public void setUp() {
    this.modulService = mock(ModulService.class);
  }

  @Test
  public void getMappingModulHinzufuegen() throws Exception {
    Modul propra1 = new Modul(1L, "ProPra1", "orga");
    Modul bwl = new Modul(3L, "BWL", "orga");

    List<Modul> moduls = new ArrayList<>();
    moduls.add(bwl);
    moduls.add(propra1);

    System.out.println(moduls.toString());

    when(modulService.findByOwner("orga")).thenReturn(moduls);

    mockMvc.perform(get("/modulHinzufuegen")
        .with(user("orga").password("orga")))
        .andExpect(status().isOk())
        .andExpect(view().name("modulAuswahl"))
        .andExpect(model().attribute("moduls", hasSize(2)))
        .andExpect(model().attribute("moduls", hasItem(
            allOf(
                hasProperty("id", is(1L)),
                hasProperty("name", is("ProPra1")),
                hasProperty("owner", is("orga"))
            )
        )))
        .andExpect(model().attribute("moduls", hasItem(
            allOf(
                hasProperty("id", is(3L)),
                hasProperty("name", is("BWL")),
                hasProperty("owner", is("orga"))
            )
        )));
    verify(modulService, times(1)).findByOwner("orga");
    verifyNoMoreInteractions(modulService);

  }
}
