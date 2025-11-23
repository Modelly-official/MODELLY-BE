package modelly.modelly_be.domain.infra.mail;

public interface MailSender {
    void sendHtml(String to, String subject, String htmlContent);
}
