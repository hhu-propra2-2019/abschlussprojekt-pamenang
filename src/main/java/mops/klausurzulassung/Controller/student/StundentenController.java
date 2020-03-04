package mops.klausurzulassung.Controller.student;


        import org.springframework.stereotype.Controller;
        import org.springframework.ui.Model;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class StundentenController {
    @GetMapping("/student")
    public String studentansicht(Model model) {
        model.addAttribute("meldung", false);
        return "student";
    }

    @PostMapping("/student")
    public String empfangeDaten(Model model,String matrikelnummer, String token, String fach){
        System.out.println(matrikelnummer + " " + token + " " + fach);
        boolean value = true;
        model.addAttribute("success", value);
        model.addAttribute("meldung", true);
        return "student";
    }
}