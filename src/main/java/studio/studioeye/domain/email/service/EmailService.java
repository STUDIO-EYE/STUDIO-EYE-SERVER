package studio.studioeye.domain.email.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class); // Logger 추가
    private static final int MAX_EMAIL_SIZE = 25 * 1024 * 1024; // 25MB

    public boolean sendEmail(String to, String subject, String text) {
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("Invalid recipient address");
        }
        int emailSize = calculateEmailSize(subject, text);
        if (emailSize > MAX_EMAIL_SIZE) {
            logger.warn("Email size exceeds the maximum limit: {} bytes", emailSize); // System.out을 logger로 변경
            return false;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
        logger.info("Email successfully sent to {}", to); // 이메일 성공 로그 추가
        return true;
    }

    private int calculateEmailSize(String subject, String text) {
        int subjectSize = subject.getBytes().length;
        int textSize = text.getBytes().length;
        logger.debug("Calculated email size: {} MB (Subject: {} bytes, Text: {} bytes)",
                ((subjectSize + textSize) / 1024) / 1024, subjectSize, textSize); // System.out을 logger로 변경
        return subjectSize + textSize;
    }
}
