package mops.klausurzulassung.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@Controller
public class LogoutController {


  @GetMapping("/logout")
  public String logout(HttpServletRequest httpRequest) throws ServletException {
    httpRequest.logout();
    return "redirect:/zulassung1";
  }
}
