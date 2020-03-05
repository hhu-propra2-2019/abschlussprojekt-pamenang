package mops.klausurzulassung;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  @Autowired JavaMailSender javaMailSender;

  public void sendMail() {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom("pamenang@web.de");
    msg.setTo("hendrik.schmitt.mail@web.de");
    msg.setSubject("Testen des Klausurzulassung MOPS Mailsender");
    msg.setText("Hallo du Nudel!");
    javaMailSender.send(msg);
  }
}
