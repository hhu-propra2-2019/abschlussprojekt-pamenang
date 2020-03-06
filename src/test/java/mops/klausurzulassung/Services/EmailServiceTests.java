package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Student;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


import static org.mockito.Mockito.*;

@SpringBootTest
public class EmailServiceTests {

  static EmailService emailService;
  static JavaMailSender javaMailSender;



  @BeforeAll
  public static void beforeAllTests() {
    javaMailSender = mock(JavaMailSender.class);
    emailService = new EmailService(javaMailSender);
  }

  @Test
  public void test_sendMail_checkForMethodCalls() {
    // Arrange
    Student student = new Student("t1", "t2", "t3", 1234, 1, "t4", "token");
    // Act
    emailService.sendMail(student);
    // Assert
    verify(javaMailSender, times(1)).send((SimpleMailMessage) any());
  }

  @Test
  public void test_generateValidToken_CheckIfSuccessfullyGeneratedLink(){
    Student student = new Student("t1", "t2", "t3", 1234, 1, "t4", "token");
    String link = emailService.generateValidTokenLink(student);
    System.out.println(link);
    Assertions.assertThat(link).contains("token/t4/1234");
  }

}
