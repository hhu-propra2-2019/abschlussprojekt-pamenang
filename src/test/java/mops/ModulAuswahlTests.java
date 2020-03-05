package mops;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ModulAuswahlTests {
    @Autowired private MockMvc mockMvc;

    @Test
    public void fuerModulAuswahlAnmelden() throws Exception {
        mockMvc
                .perform(get("/zulassung1/ModulHinzufuegen"))
                .andExpect(status().is3xxRedirection())
                .andDo(print());
    }

    @WithMockUser(username = "niemand", password = "nichts")
    @Test
    public void anmeldungFailed() throws Exception {
        mockMvc.perform(get("/student")).andExpect(status().isForbidden());
    }
/*
    @WithMockUser(username = "orga", password = "orga")
    @Test
    public void anmeldungSuccessful() throws Exception {
        mockMvc.perform(get("/student")).andExpect(status().isOk());
    }*/
}
