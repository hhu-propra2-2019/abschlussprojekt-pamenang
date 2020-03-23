package mops.klausurzulassung.controller;

import mops.klausurzulassung.services.ModulService;
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

@RequestMapping("/zulassung1")
@SessionScope
@Controller
public class StatistikController {

  private Logger logger = LoggerFactory.getLogger(ModulService.class);

  @Secured("ROLE_orga")
  @GetMapping("/modul/{id}/statistik")
  public String selectStatistik(@PathVariable Long id, Model model, KeycloakAuthenticationToken token) {

    return "statistik";
  }
}
