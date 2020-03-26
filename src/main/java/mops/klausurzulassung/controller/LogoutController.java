package mops.klausurzulassung.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@Controller
public class LogoutController {

  /**
   * This method is called for a GET request to /logout.
   *
   * @return Redirects to view zulassung1
   */
  @GetMapping("/logout")
  public String logout(HttpServletRequest httpRequest) throws ServletException {
    httpRequest.logout();
    return "redirect:/zulassung1";
  }
}
