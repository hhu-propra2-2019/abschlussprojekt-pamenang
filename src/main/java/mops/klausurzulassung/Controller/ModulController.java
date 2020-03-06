package mops.klausurzulassung.Controller;

import com.opencsv.CSVWriter;
import mops.klausurzulassung.Domain.Account;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Services.CsvService;
import mops.klausurzulassung.organisatoren.Entities.Modul;
import mops.klausurzulassung.organisatoren.Services.AltzulassungService;
import mops.klausurzulassung.organisatoren.Services.ModulService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/zulassung1")
public class ModulController {

  private final ModulService modulService;
  private final CsvService csvService;
  private final AltzulassungService studentService;
  private String errorMessage;
  private String successMessage;
  private Modul currentModul = new Modul();

  public ModulController(ModulService modulService, CsvService csvService, AltzulassungService studentService) {
    this.modulService = modulService;
    this.csvService = csvService;
    this.studentService = studentService;
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
  @GetMapping("/modulHinzufuegen")
  public String index(Model model, KeycloakAuthenticationToken token, Principal principal) {
    System.out.println(principal.getName());
    model.addAttribute("account", createAccountFromPrincipal(token));
    model.addAttribute("moduls", modulService.findByOwner(principal.getName()));
    model.addAttribute("modul", currentModul);
    model.addAttribute("error", errorMessage);
    model.addAttribute("success", successMessage);
    return "modulAuswahl";
  }

  @Secured("ROLE_orga")
  @PostMapping("/modulHinzufuegen")
  public String newModul(
      @ModelAttribute @Valid Modul modul, Model model, KeycloakAuthenticationToken token, Principal principal) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    modul.setOwner(principal.getName());
    this.currentModul = modul;

    if (modulService.findById(modul.getId()).isPresent()) {
      setMessages("Diese Modul-ID existiert schon, bitte eine andere eingeben!", null);
    } else {
      modulService.save(modul);
      setMessages(null, "Neues Modul wurde erfolgreich hinzugefügt!");
      this.currentModul = new Modul();
    }
    return "redirect:/zulassung1/modulHinzufuegen";
  }

  @Secured("ROLE_orga")
  @PostMapping("/modul/{id}/delete")
  public String deleteModul(Model model, @PathVariable Long id, KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    Optional<Modul> modul = modulService.findById(id);
    if (modul.isPresent()) {
      modulService.delete(modul.get());
      setMessages(null, "Successfully deleted modul!");
    } else {
      setMessages("Modul could not be deleted, because it was not found in the database.", null);
    }
    return "redirect:/zulassung1/modulHinzufuegen";
  }

  @Secured("ROLE_orga")
  @GetMapping("/modul/{id}")
  public String selectModul(@PathVariable Long id, Model model, KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    Modul modul = modulService.findById(id).get();
    String name = modul.getName();
    model.addAttribute("modul", name);
    model.addAttribute("id", id);

    return "modulAnsicht";
  }

  @Secured("ROLE_orga")
  @PostMapping("/upload/{id}")
  public String uploadListe(@PathVariable Long id, Model model, KeycloakAuthenticationToken token, MultipartFile file) throws IOException {
    model.addAttribute("account", createAccountFromPrincipal(token));
    List<Student> students = csvService.getStudentListFromInputFile(file, id);

    for (Student student : students) {
      System.out.println("Tokens werden generiert und verschickt!");
      // Student wird zu Token-Generierung geschickt (Email, Vorname, Nachname, MatrNr, Fach)
      // Token des Students wird zum Mailsender geschickt
    }

    File outputFile = new File("./klausurliste.csv");
    FileWriter fileWriter = new FileWriter(outputFile);
    CSVWriter writer = new CSVWriter(fileWriter);

    Iterable<Student> altzugelassene = studentService.findByRaumId(id);

    for (Student student : altzugelassene) {
      students.add(student);
    }

    for (Student student : students) {
      csvService.putStudentOntoList(writer, student);
      writer.flush();
    }
    writer.close();

    return "redirect:/zulassung1/upload" + "/" + id;
  }

  private void setMessages(String errorMessage, String successMessage) {
    this.errorMessage = errorMessage;
    this.successMessage = successMessage;
  }
}
