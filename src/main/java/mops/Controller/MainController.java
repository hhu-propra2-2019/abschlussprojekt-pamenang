package mops.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    public MainController(){

    }

    @GetMapping("/")
    public String mainpage(Model model){
        return "mainpage";
    }
}
