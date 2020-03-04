package mops.klausurzulassung.Controller;

import mops.klausurzulassung.Domain.Account;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class MainController {

    public MainController(){

    }

    private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
        KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
        return new Account(
                principal.getName(),
                principal.getKeycloakSecurityContext().getIdToken().getEmail(),
                null,
                token.getAccount().getRoles());
    }

    @GetMapping("/")
    public String mainpage(KeycloakAuthenticationToken token ,Model model){
        if(token != null){
            model.addAttribute("account", createAccountFromPrincipal(token));
        }

        return "mainpage";
    }

    @GetMapping("/student")
    @Secured("ROLE_studentin")
    public String studentpage(KeycloakAuthenticationToken token,Model model){
        model.addAttribute("account", createAccountFromPrincipal(token));
        return "student";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) throws Exception {
        request.logout();
        return "redirect:/";
    }
}
