package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Repositories.ModulRepository;
import org.bouncycastle.math.raw.Mod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

public class ModulServiceTest {

    private ModulRepository modulRepository;
    private CsvService csvService;
    private StudentService studentService;
    private TokengenerierungService tokengenerierungService;
    private EmailService emailService;
    private QuittungService quittungService;

    @BeforeEach
    public void initilize(){
        modulRepository = mock(ModulRepository.class);
        csvService = mock(CsvService.class);
        studentService = mock(StudentService.class);
        tokengenerierungService = mock(TokengenerierungService.class);
        emailService = mock(EmailService.class);
        quittungService = mock(QuittungService.class);
    }


    @Test
    void testStudentIsEmptyIsEmpty(){
        Student student = new Student();
        student.setVorname(anyString());
        student.setNachname(anyString());
        student.setEmail(null);
        student.setMatrikelnummer(anyLong());
        ModulService modulService = new ModulService(modulRepository,csvService,studentService, tokengenerierungService,emailService,quittungService);
        boolean b = modulService.studentIsEmpty(student);
        assertTrue(b);
    }
    @Test
    void testStudentIsEmptyIsNotEmpty(){
        ModulService modulService = new ModulService(modulRepository,csvService,studentService, tokengenerierungService,emailService,quittungService);
        Student student = new Student();
        student.setVorname(anyString());
        student.setNachname(anyString());
        student.setEmail(anyString());
        student.setMatrikelnummer(anyLong());
        boolean b = modulService.studentIsEmpty(student);
        assertFalse(b);
    }
}
