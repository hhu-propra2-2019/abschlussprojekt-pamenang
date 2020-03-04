package mops.klausurzulassung.Controller.student;

import mops.klausurzulassung.Domain.Account;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import static mops.klausurzulassung.Token.Tokenverifikation.verifikation;

@Controller
public class StundentenController {

  private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    return new Account(
        principal.getName(),
        principal.getKeycloakSecurityContext().getIdToken().getEmail(),
        null,
        token.getAccount().getRoles());
  }

  @GetMapping("/student")
  @Secured("ROLE_studentin")
  public String studentansicht(Model model, KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    model.addAttribute("meldung", false);
    return "student";
  }

  @PostMapping("/student")
  @Secured("ROLE_studentin")
  public String empfangeDaten(
      KeycloakAuthenticationToken keycloakAuthenticationToken,
      Model model,
      String matrikelnummer,
      String token,
      String fach) {

    boolean value =  verifikation(matrikelnummer,fach,token);
    model.addAttribute("account", createAccountFromPrincipal(keycloakAuthenticationToken));
    model.addAttribute("success", value);
    model.addAttribute("meldung", true);
    return "student";
  }
}
