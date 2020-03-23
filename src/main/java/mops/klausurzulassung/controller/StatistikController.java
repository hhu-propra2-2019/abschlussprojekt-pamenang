package mops.klausurzulassung.controller;

import mops.klausurzulassung.database_entity.Modul;
import mops.klausurzulassung.domain.Account;
import mops.klausurzulassung.services.ModulService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Optional;

@RequestMapping("/zulassung1")
@SessionScope
@Controller
public class StatistikController {

  private final ModulService modulService;
  private Logger logger = LoggerFactory.getLogger(StatistikController.class);

  public StatistikController(ModulService modulService) {
    this.modulService = modulService;
  }

  private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    return new Account(principal.getName(),
        principal.getKeycloakSecurityContext().getIdToken().getEmail(),
        null,
        token.getAccount().getRoles());
  }

  @Secured("ROLE_orga")
  @GetMapping("/modul/{id}/statistik")
  public String selectStatistik(@PathVariable Long id, Model model, KeycloakAuthenticationToken token) {
    Optional<Modul> modul = modulService.findById(id);
    model.addAttribute("modul", modul.get());
    return "statistik";
  }
}
