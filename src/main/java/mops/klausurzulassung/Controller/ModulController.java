package mops.klausurzulassung.Controller;

import mops.klausurzulassung.Services.ModulService;
import mops.klausurzulassung.organisatoren.Entities.Modul;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/zulassung1")
public class ModulController {

  private final ModulService modulService;
  private String errorMessage;
  private String successMessage;
  private Modul currentModul = new Modul();

  public ModulController(ModulService modulService) {
    this.modulService = modulService;
  }

  @GetMapping("/modulHinzufuegen")
  public String index(Model model) {
    System.out.println(modulService.allModuls());
    model.addAttribute("moduls", modulService.allModuls());
    model.addAttribute("modul", currentModul);
    model.addAttribute("error", errorMessage);
    model.addAttribute("success", successMessage);
    return "modulAuswahl";
  }

  @PostMapping("/modulHinzufuegen")
  public String newModul(
      @ModelAttribute @Valid Modul modul, BindingResult bindingResult, Model model) {
    this.currentModul = modul;

    if (modulService.findById(modul.getId()).isPresent()) {
      setMessages("Diese Modul-ID existiert schon, bitte eine andere eingeben!", null);
    } else {
      modulService.save(modul);
      setMessages(null, "Neues Modul wurde erfolgreich hinzugefügt!");
      this.currentModul = new Modul();
    }
    // return "redirect:/";
    return "redirect:/zulassung1/modulHinzufuegen";
  }

  @PostMapping("/modul/{id}/delete")
  public String deleteModul(@PathVariable Long id) {
    Optional<Modul> modul = modulService.findById(id);
    if (modul.isPresent()) {
      modulService.delete(modul.get());
      setMessages(null, "Successfully deleted modul!");
    } else {
      setMessages("Modul could not be deleted, because it was not found in the database.", null);
    }
    return "redirect:/zulassung1/modulHinzufuegen";
  }

  @GetMapping("/modul/{id}")
  public String selectModul(@PathVariable Long id, Model model) {
    Modul modul = modulService.findById(id).get();
    String name = modul.getName();
    System.out.println(name);
    model.addAttribute("modul", name);
    model.addAttribute("id", id);

    return "modulAnsicht";
  }

  private void setMessages(String errorMessage, String successMessage) {
    this.errorMessage = errorMessage;
    this.successMessage = successMessage;
  }
}
