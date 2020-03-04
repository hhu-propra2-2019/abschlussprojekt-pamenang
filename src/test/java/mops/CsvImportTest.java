package mops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CsvImportTest {

    @Autowired
    private MockMvc mockMvc;



    @Test
    public void shouldReturnCsvImportTemplate() throws Exception {

        mockMvc.perform(get("/csvimport").with(user("root").password("test")))
        .andExpect(status().isOk());

    }

    @Test
    public void shouldUploadFileIntoContext() throws Exception {
    MockMultipartFile mockMultipartFile =
        new MockMultipartFile("datei", "data.csv", "text/csv", "Boris,Tenelsen,4252152,ja\n".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/csvimport").file(mockMultipartFile)).andExpect(status().is3xxRedirection());


    }


}
