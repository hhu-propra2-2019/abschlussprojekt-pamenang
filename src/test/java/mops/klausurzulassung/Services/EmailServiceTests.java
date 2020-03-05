package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Student;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

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
}
