package mops.klausurzulassung.controller;

import mops.klausurzulassung.domain.Account;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.annotation.SessionScope;

@Controller
@SessionScope
@RequestMapping("/zulassung1")
public class MainController {

  private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    return new Account(
        principal.getName(),
        principal.getKeycloakSecurityContext().getIdToken().getEmail(),
        null,
        token.getAccount().getRoles());
  }

  @GetMapping("")
  public String mainpage(KeycloakAuthenticationToken token, Model model) {
    if (token != null) {
      Account account = createAccountFromPrincipal(token);
      model.addAttribute("account", account);
      if(account.getRoles().contains("orga")){
        return "redirect:/zulassung1/modulHinzufuegen";
      }else if(account.getRoles().contains("studentin")){
        return "redirect:/zulassung1/student";
      }
    }

    return "mainpage";
  }

}
