package studio.studioeye.domain.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private static final int MAX_EMAIL_SIZE = 25 * 1024 * 1024; // 25MB

    public boolean sendEmail(String to, String subject, String text) {
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("Invalid recipient address");
        }
        int emailSize = calculateEmailSize(subject, text);
        if(emailSize > MAX_EMAIL_SIZE) {
            System.out.println("over size");
            return false;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
        return true;
    }

    private int calculateEmailSize(String subject, String text) {
        int subjectSize = subject.getBytes().length;
        int textSize = text.getBytes().length;
        System.out.println("size = "+(((subjectSize+textSize) / 1024) / 1024)+", "+subjectSize+", "+textSize);
        return subjectSize + textSize;
    }
}