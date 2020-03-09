package mops.klausurzulassung.Controller.student;

import mops.klausurzulassung.Domain.Account;
import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Services.Token.TokenverifikationService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

@RequestMapping("/zulassung1")
@Controller
public class StudentenController {

  @Autowired TokenverifikationService tokenverifikation;

  private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    return new Account(
        principal.getName(),
        principal.getKeycloakSecurityContext().getIdToken().getEmail(),
        null,
        token.getAccount().getRoles());
  }

  @GetMapping("/student/{zulassungToken}/{fachName}/{matrikelnr}")
  @Secured("ROLE_studentin")
  public String studentansichtMitToken(
      @PathVariable String zulassungToken,
      @PathVariable String fachName,
      @PathVariable long matrikelnr,
      Model model,
      KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    model.addAttribute("meldung", false);
    model.addAttribute("zulassungToken", zulassungToken);
    model.addAttribute("matrikelnr", matrikelnr);
    model.addAttribute("fachName", fachName);

    return "student";
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
      String fach)
          throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, NoPublicKeyInDatabaseException {

    boolean value = tokenverifikation.verifikationToken(matrikelnummer, fach, token);
    model.addAttribute("account", createAccountFromPrincipal(keycloakAuthenticationToken));
    model.addAttribute("success", value);
    model.addAttribute("meldung", true);
    return "student";
  }
}
