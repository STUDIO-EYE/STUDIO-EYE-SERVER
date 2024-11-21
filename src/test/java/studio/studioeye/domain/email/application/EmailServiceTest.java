package studio.studioeye.domain.email.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import studio.studioeye.domain.email.service.EmailService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @InjectMocks
    private EmailService emailService; // EmailService를 올바르게 주입

    @Mock
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("이메일 전송 성공 테스트")
    public void sendEmailSuccess() {
        // given
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Body";
        // stub
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));
        // when
        boolean result = emailService.sendEmail(to, subject, text);
        // then
        assertTrue(result, "이메일이 정상적으로 전송되어야 합니다.");
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("이메일 전송 실패 테스트 - 크기 초과")
    public void sendEmailFailDueToSize() {
        // given
        String to = "test@example.com";
        String subject = "This is a very long subject that will exceed the limit";
        String text = "A".repeat(25 * 1024 * 1024); // 25MB 이상의 텍스트
        // when
        boolean result = emailService.sendEmail(to, subject, text);
        // then
        assertFalse(result, "이메일 크기가 초과된 경우 전송에 실패해야 합니다.");
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("이메일 전송 실패 테스트 - 수신자 없음")
    public void sendEmailFailNoRecipient() {
        // given
        String to = null;
        String subject = "Test Subject";
        String text = "Test Body";
        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendEmail(to, subject, text);
        });
        // then
        assertEquals("Invalid recipient address", exception.getMessage());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }
}
