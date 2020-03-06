package mops.klausurzulassung.Controller;

import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/zulassung1")
public class EmailController {

    @Autowired
    EmailService emailService;


    @GetMapping("/generateEmail")
    public String generateTestMail(Model model){
        String link = emailService.generateValidTokenLink(new Student("max","mustermann","test@test.de",123456,1,"Propra2","alsd3q11osamd224amsl"));
        model.addAttribute("link",link);
        return "emailSuccess";
    }
}
