package mops.klausurzulassung.controller;

import mops.klausurzulassung.database_entity.Modul;
import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.domain.Account;
import mops.klausurzulassung.domain.AltzulassungStudentDto;
import mops.klausurzulassung.domain.FrontendMessage;
import mops.klausurzulassung.services.ModulService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;

@Controller
@SessionScope
@RequestMapping("/zulassung1")
public class ModulController {

  private final ModulService modulService;
  private Modul currentModul = new Modul();

  private FrontendMessage message = new FrontendMessage();

  public ModulController(ModulService modulService) {
    this.modulService = modulService;
  }

  private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    return new Account(
        principal.getName(),
        principal.getKeycloakSecurityContext().getIdToken().getEmail(),
        null,
        token.getAccount().getRoles());
  }

  @Secured("ROLE_orga")
  @GetMapping("/modulAuswahl")
  public String index(Model model, KeycloakAuthenticationToken token, Principal principal) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    Iterable<Modul> moduls = modulService.findByOwnerAndActive(principal.getName(), true);
    model.addAttribute("moduls", moduls);

    model.addAttribute("errorMessage",message.getErrorMessage());
    model.addAttribute("successMessage",message.getSuccessMessage());

    message.resetMessage();
    return "modulAuswahl";
  }

  @Secured("ROLE_orga")
  @PostMapping("/neuesModulHinzufuegen")
  public String backToModulAuswahl(@ModelAttribute @Valid Modul modul, Model model, KeycloakAuthenticationToken token, Principal principal) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    String orga = principal.getName();
    String page = "redirect:/zulassung1/modulHinzufuegen";
    if (!modulService.missingAttributeInModul(modul)) {
      if (!modulService.isFristAbgelaufen(modul)) {
        modul.setFrist(modul.getFrist() + " 12:00");
        modul.setOwner(orga);
        modul.setActive(true);
        modulService.save(modul);
        page= "modulAuswahl";
      } else {
        message.setErrorMessage( "Die Frist muss in der Zukunft liegen!");
      }
    } else {
      message.setErrorMessage("Bitte beide Felder ausfüllen!");
    }

    Iterable<Modul> moduls = modulService.findByOwnerAndActive(orga, true);
    model.addAttribute("moduls", moduls);
    model.addAttribute("errorMessage", message.getErrorMessage());
    model.addAttribute("successMessage", message.getSuccessMessage());


    return page;
  }

  @Secured("ROLE_orga")
  @GetMapping("/modulBearbeiten/{id}")
  public String modulBearbeiten(@PathVariable Long id, Model model, KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    Modul modul = modulService.findById(id).get();
    model.addAttribute("id", id);
    model.addAttribute("modul", modul);
    model.addAttribute("errorMessage", message.getErrorMessage());
    model.addAttribute("successMessage", message.getSuccessMessage());
    message.resetMessage();

    return "modulBearbeiten";
  }

  @Secured("ROLE_orga")
  @PostMapping("/modulBearbeiten/{id}")
  public String modulAbschicken(@ModelAttribute @Valid Modul modul, @PathVariable Long id, Model model, KeycloakAuthenticationToken token, Principal principal) {
    model.addAttribute("account", createAccountFromPrincipal(token));


    String page = "redirect:/zulassung1/modulBearbeiten/" + id;
    if (!modulService.missingAttributeInModul(modul)) {
      if (!modulService.isFristAbgelaufen(modul)) {
        Modul vorhandenesModul = modulService.findById(id).get();
        vorhandenesModul.setName(modul.getName());
        vorhandenesModul.setFrist(modul.getFrist() + " 12:00");
        vorhandenesModul.setOwner(principal.getName());
        vorhandenesModul.setActive(true);
        modulService.save(vorhandenesModul);
        page = "redirect:/zulassung1/modulAuswahl";
      } else {
        message.setErrorMessage("Die Frist muss in der Zukunft liegen!");
      }
    } else {
      message.setErrorMessage("Beide Felder müssen ausgefüllt sein!");
    }

    return page;
  }

  @Secured("ROLE_orga")
  @GetMapping("/modulHinzufuegen")
  public String newModul(
      Model model,
      KeycloakAuthenticationToken token) {

    model.addAttribute("account", createAccountFromPrincipal(token));

    Iterable<Modul> moduls = modulService.findByActive(false);
    model.addAttribute("moduls", moduls);


    model.addAttribute("modul", currentModul);
    model.addAttribute("bearbeitetesModul", currentModul);

    model.addAttribute("errorMessage", message.getErrorMessage());
    model.addAttribute("successMessage", message.getSuccessMessage());
    return "modulHinzufuegen";

  }


  @Secured("ROLE_orga")
  @PostMapping("/modul/{id}/delete")
  public String deleteModul(Model model, @PathVariable Long id, KeycloakAuthenticationToken token) {

    model.addAttribute("account", createAccountFromPrincipal(token));

     message = modulService.deleteStudentsFromModul(id);
    return "redirect:/zulassung1/modulAuswahl";
  }

  @Secured("ROLE_orga")
  @GetMapping("/modul/{id}")
  public String selectModul(@PathVariable Long id, Model model, KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    Modul modul = modulService.findById(id).get();
    String name = modul.getName();
    String frist = modul.getFrist();
    model.addAttribute("modul", name);
    model.addAttribute("id", id);
    model.addAttribute("frist", frist);
    model.addAttribute("student", new Student("", "", "", null, id, null, null));
    model.addAttribute("papierZulassung", false);

    model.addAttribute("errorMessage",message.getErrorMessage());
    model.addAttribute("successMessage",message.getSuccessMessage());
    message.resetMessage();

    return "modulAnsicht";
  }

  @Secured("ROLE_orga")
  @PostMapping("/modul/{id}")
  public String uploadListe(@PathVariable Long id, Model model, KeycloakAuthenticationToken token, @RequestParam("datei") MultipartFile file) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    message= modulService.verarbeiteUploadliste(id, file);
    return "redirect:/zulassung1/modul" + "/" + id;
  }

  @Secured("ROLE_orga")
  @GetMapping(value ="/modul/{id}/klausurliste", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ResponseBody
  public void downloadListe(@PathVariable Long id, Model model, KeycloakAuthenticationToken token, HttpServletResponse response) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    modulService.download(id, response);
  }


  @Secured("ROLE_orga")
  @PostMapping("/{id}/altzulassungHinzufuegen")
  public String altzulassungHinzufuegen(@ModelAttribute("studentDto") @Valid AltzulassungStudentDto studentDto, BindingResult bindingResult, boolean papierZulassung, @PathVariable Long id, Model model, KeycloakAuthenticationToken token) {
    
    if(bindingResult.hasErrors()){
      message.setErrorMessage("Alle Felder im Formular müssen befüllt werden!");
      return "redirect:/zulassung1/modul/" + id;
    }
    model.addAttribute("account", createAccountFromPrincipal(token));

    message = modulService.altzulassungVerarbeiten(studentDto, papierZulassung, id);
    return "redirect:/zulassung1/modul/" + id;
  }

}