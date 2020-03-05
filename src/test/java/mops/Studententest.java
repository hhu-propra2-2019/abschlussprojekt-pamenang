package mops;


import mops.klausurzulassung.Controller.student.StundentenController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class Studententest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    StundentenController student;


    @Test
    public void fuerAltzulassungAnmelden() throws Exception {
        String expect = "Hi";
        mockMvc.perform(get("/student")).andExpect(status().is3xxRedirection()).andDo(print());
    }
}
